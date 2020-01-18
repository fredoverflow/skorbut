package interpreter

import common.Diagnostic
import semantic.TypeChecker
import semantic.types.*
import syntax.lexer.Lexer
import syntax.lexer.Token
import syntax.lexer.TokenKind.*
import syntax.parser.Parser
import syntax.parser.translationUnit
import syntax.tree.*
import kotlin.math.floor

fun FunctionDefinition.returnType(): Type = (namedDeclarator.type as FunctionType).returnType

class Interpreter(program: String) {
    val translationUnit = Parser(Lexer(program)).translationUnit()
    val typeChecker = TypeChecker(translationUnit)

    val functions = LinkedHashMap<String, FunctionDefinition>()
    val variables = translationUnit.declarations.filter { it.specifiers.storageClass != TYPEDEF }
            .flatMap { it.namedDeclarators }.filter { it.offset < 0 && it.type.requiresStorage() }

    val memory = Memory(typeChecker.getStringLiterals(), variables)
    val console = Console()

    var stackDepth = 0

    var targetType: Type = VoidPointerType

    init {
        for (function in translationUnit.functions) {
            BuildControlFlowGraph(function)
            functions[function.name()] = function
        }

        for (namedDeclarator in variables) {
            with(namedDeclarator) {
                if (declarator is Declarator.Initialized) {
                    initialize(type, declarator.init, memory.staticVariables, offset + Int.MIN_VALUE)
                } else {
                    defaultInitialize(type, memory.staticVariables, offset + Int.MIN_VALUE)
                }
            }
        }
    }

    var before: Function1<Int, Unit>? = null
    var after: Function0<Unit>? = null

    fun run() {
        val main = translationUnit.functions.firstOrNull { it.name() == "main" }
        if (main == null) throw Diagnostic(0, "no main function found")
        if (!main.parameters.isEmpty()) main.root().error("main cannot have parameters")
        if (main.returnType() !== SignedIntType) main.root().error("main must return int")

        val result = main.execute(emptyList())
        val exitCode = (result as ArithmeticValue).value.toInt()
        console.print("\nmain finished with exit code $exitCode")
        console.update?.invoke()
        if (!memory.heap.isEmpty()) {
            main.closingBrace.error("You forgot to free ${memory.heap.size} heap blocks")
        }
    }

    private fun FunctionDefinition.execute(arguments: List<Value>): Value {
        ++stackDepth
        try {
            memory.functionScoped(stackFrameType) {
                for ((param, arg) in parameters.zip(arguments)) {
                    memory.currentStackFrame()[param.offset] = param.type.cast(arg)
                }
                after?.invoke()

                var basicBlock = controlFlowGraph["0"]!!.getStatements()
                var pc = 0
                while (pc != basicBlock.size) {
                    with(basicBlock[pc++]) {
                        when (this) {
                            is Jump -> {
                                basicBlock = controlFlowGraph[target]!!.getStatements()
                                pc = 0
                            }
                            is JumpIf -> {
                                basicBlock = controlFlowGraph[if (condition.delayedCondition()) th3n else e1se]!!.getStatements()
                                pc = 0
                            }
                            is HashSwitch -> {
                                basicBlock = controlFlowGraph[cases[control.delayed()] ?: default]!!.getStatements()
                                pc = 0
                            }
                            is FlatDeclaration -> {
                                for (namedDeclarator in namedDeclarators) {
                                    with(namedDeclarator) {
                                        if (declarator is Declarator.Initialized && offset >= 0) {
                                            before?.invoke(name.start)
                                            initialize(type, declarator.init, memory.currentStackFrame(), offset)
                                            after?.invoke()
                                        }
                                    }
                                }
                            }
                            is FlatExpressionStatement -> {
                                expression.delayed()
                            }
                            is FlatReturn -> {
                                return result.delayed()
                            }
                            is FlatAssert -> {
                                if (!condition.delayedCondition()) condition.root().error("assertion failed")
                            }
                            else -> error("no execute for $this")
                        }
                        {
                            // Without this empty block, the return type of "with" is deduced incorrectly
                        }
                    }
                }

                before?.invoke(closingBrace.start)

                if (returnType() !== VoidType) {
                    throw Diagnostic(closingBrace.start, "missing return statement")
                }
            }
            return VoidValue
        } finally {
            --stackDepth
        }
    }

    private fun initialize(qualified: Type, init: Initializer, segment: Segment, start: Int): Int {
        val type = qualified.unqualified()
        when (init) {
            is ExpressionInitializer -> {
                return if (init.expression is StringLiteral && type is ArrayType && type.elementType == SignedCharType) {
                    val str = init.expression.literal.text
                    for ((i, c) in str.withIndex()) {
                        segment[start + i] = Value.signedChar(c.toInt())
                    }
                    for (i in str.length until type.length) {
                        segment[start + i] = Value.NUL
                    }
                    start + type.length
                } else {
                    targetType = type
                    val value = type.cast(init.expression.evaluate())
                    segment[start] = value
                    start + 1
                }
            }
            is InitializerList -> {
                when (type) {
                    is ArrayType -> return init.list.fold(start) { offset, initializer ->
                        initialize(type.elementType, initializer, segment, offset)
                    }
                    is StructType -> return type.members.zip(init.list).fold(start) { offset, memberInitializer ->
                        initialize(memberInitializer.first.type, memberInitializer.second, segment, offset)
                    }
                }
            }
        }
        error("no init for $init")
    }

    private fun defaultInitialize(qualified: Type, segment: Segment, start: Int): Int {
        val type = qualified.unqualified()
        when (type) {
            is ArithmeticType -> {
                segment[start] = type.defaultValue
            }
            is ArrayType -> {
                (0 until type.length).fold(start) { offset, _ ->
                    defaultInitialize(type.elementType, segment, offset)
                }
            }
            is StructType -> {
                type.members.fold(start) { offset, member ->
                    defaultInitialize(member.type, segment, offset)
                }
            }
        }
        return start + type.count()
    }

    private fun Expression.delayed(): Value {
        before?.invoke(root().start)
        val result = evaluate()
        after?.invoke()
        return result
    }

    private fun Expression.delayedCondition(): Boolean {
        return (delayed() as ArithmeticValue).isTrue()
    }

    private fun Expression.locate(): Object {
        return when (this) {
            is StringLiteral -> {
                memory.stringObjects[literal.text]!!
            }
            is Identifier -> {
                memory.makeObject(symbol)
            }
            is Subscript -> {
                val left = left.evaluate()
                val right = right.evaluate()
                if (left is PointerValue && right is ArithmeticValue) {
                    left.referenced.checkReferable() + right.value.toInt()
                } else if (left is ArithmeticValue && right is PointerValue) {
                    right.referenced.checkReferable() + left.value.toInt()
                } else {
                    error("no locate for $this")
                }
            }
            is DirectMemberAccess -> {
                val struct = left.locate()

                val type = struct.type.unqualified() as StructType
                val member = type.member(right)
                Object(struct.segment, struct.offset + member!!.offset, member.type, 0, 1)
            }
            is IndirectMemberAccess -> {
                val pointer = left.evaluate() as PointerValue
                val struct = pointer.referenced

                val type = struct.type.unqualified() as StructType
                val member = type.member(right)
                Object(struct.segment, struct.offset + member!!.offset, member.type, 0, 1)
            }
            is Dereference -> {
                val pointer = operand.evaluate() as PointerValue
                pointer.referenced.checkReferable()
            }
            else -> error("no locate for $this")
        }
    }

    private fun Expression.evaluate(): Value {
        value?.let { return it }
        return when (this) {
            is Identifier -> {
                val symbolType = symbol.type
                if (symbolType is FunctionType) {
                    FunctionDesignator(symbol.name, symbolType)
                } else {
                    locate().evaluate()
                }
            }
            is PrintfCall -> {
                Value.signedInt(console.printf(format, arguments.map { it.evaluate() }))
            }
            is ScanfCall -> {
                Value.signedInt(console.scanf(format, arguments.map { it.evaluate() }, after))
            }
            is Postfix -> {
                val obj = operand.locate()
                val oldValue = obj.evaluate()
                val newValue = if (oldValue is ArithmeticValue) {
                    val result = if (operator.kind == PLUS_PLUS) oldValue + Value.ONE else oldValue - Value.ONE
                    oldValue.type.cast(result)
                } else if (oldValue is PointerValue) {
                    if (operator.kind == PLUS_PLUS) oldValue + 1 else oldValue - 1
                } else {
                    error("no evaluate for $this")
                }
                obj.assign(newValue)
                oldValue
            }
            is FunctionCall -> {
                val func = (function.evaluate().decayed() as FunctionPointerValue).designator
                val name = func.functionName
                val definition = functions[name.text]
                if (definition != null) {
                    val evaluatedArguments = definition.parameters.zip(arguments).map {
                        targetType = it.first.type
                        it.second.evaluate()
                    }
                    val result = definition.execute(evaluatedArguments)
                    return definition.returnType().cast(result)
                }
                when (name.text) {
                    "time" -> {
                        return ArithmeticValue(floor(System.currentTimeMillis() / 1000.0), UnsignedIntType)
                    }
                    "putchar" -> {
                        val arg = arguments[0].evaluate() as ArithmeticValue
                        console.putchar(arg.value.toLong().and(0xff).toChar())
                        return VoidValue
                    }
                    "getchar" -> {
                        return Value.signedInt(console.getchar().toShort().toInt())
                    }
                    "malloc" -> {
                        return allocate(function, arguments[0], { type -> memory.malloc(type) }, { type -> memory.malloc(type) })
                    }
                    "free" -> {
                        memory.free(arguments[0].evaluate() as PointerValue)
                        return VoidValue
                    }
                    "realloc" -> {
                        val pointer = arguments[0].evaluate() as PointerValue
                        return allocate(function, arguments[1], { type -> memory.realloc(pointer, type) }, { type -> memory.realloc(pointer, type) })
                    }
                    "qsort" -> {
                        val base = arguments[0].evaluate() as PointerValue
                        val count = (arguments[1].evaluate() as ArithmeticValue).value.toInt()
                        val maxCount = base.referenced.bound - base.referenced.index
                        if (count > maxCount) {
                            error("There are only $maxCount elements in the array, not $count")
                        }
                        val size = (arguments[2].evaluate() as ArithmeticValue).value.toInt()
                        val actualSize = base.referenced.type.sizeof()
                        if (size != actualSize) {
                            error("element type ${base.referenced.type} has size $actualSize, not $size")
                        }
                        val comp = arguments[3].evaluate().decayed() as FunctionPointerValue
                        qsort(base, count, functions[comp.designator.functionName.text]!!)
                        return VoidValue
                    }
                    "bsearch" -> {
                        val key = arguments[0].evaluate() as PointerValue
                        val base = arguments[1].evaluate() as PointerValue
                        val count = (arguments[2].evaluate() as ArithmeticValue).value.toInt()
                        val maxCount = base.referenced.bound - base.referenced.index
                        if (count > maxCount) {
                            error("There are only $maxCount elements in the array, not $count")
                        }
                        val size = (arguments[3].evaluate() as ArithmeticValue).value.toInt()
                        val actualSize = base.referenced.type.sizeof()
                        if (size != actualSize) {
                            error("element type ${base.referenced.type} has size $actualSize, not $size")
                        }
                        val comp = arguments[4].evaluate().decayed() as FunctionPointerValue
                        return bsearch(key, base, count, functions[comp.designator.functionName.text]!!)
                    }
                    else -> error("undefined function $name")
                }
            }
            is Prefix -> {
                val obj = operand.locate()
                val oldValue = obj.evaluate()
                val newValue = if (oldValue is ArithmeticValue) {
                    val result = if (operator.kind == PLUS_PLUS) oldValue + Value.ONE else oldValue - Value.ONE
                    oldValue.type.cast(result)
                } else if (oldValue is PointerValue) {
                    if (operator.kind == PLUS_PLUS) oldValue + 1 else oldValue - 1
                } else {
                    error("no evaluate for $this")
                }
                obj.assign(newValue)
                newValue
            }
            is Reference -> {
                if (operand.type is FunctionType) {
                    FunctionPointerValue(operand.evaluate() as FunctionDesignator)
                } else {
                    PointerValue(operand.locate())
                }
            }
            is Dereference -> {
                if ((operand.type.decayed() as PointerType).referencedType is FunctionType) {
                    (operand.evaluate().decayed() as FunctionPointerValue).designator
                } else {
                    locate().evaluate()
                }
            }
            is UnaryPlus -> {
                unaryPlus(operand.evaluate())
            }
            is UnaryMinus -> {
                unaryMinus(operand.evaluate())
            }
            is BitwiseNot -> {
                bitwiseNot(operand.evaluate())
            }
            is LogicalNot -> {
                logicalNot(operand.evaluate())
            }
            is Multiplicative -> {
                multiplicative(left.evaluate(), operator, right.evaluate())
            }
            is Plus -> {
                val left = left.evaluate()
                val right = right.evaluate()
                if (left is PointerValue && right is ArithmeticValue) {
                    left + right.value.toInt()
                } else if (left is ArithmeticValue && right is PointerValue) {
                    right + left.value.toInt()
                } else {
                    plus(left, right)
                }
            }
            is Minus -> {
                val left = left.evaluate()
                val right = right.evaluate()
                if (left is PointerValue && right is ArithmeticValue) {
                    left - right.value.toInt()
                } else if (left is PointerValue && right is PointerValue) {
                    Value.signedInt(left - right)
                } else {
                    minus(left, right)
                }
            }
            is Shift -> {
                shift(left.evaluate(), operator, right.evaluate(), type)
            }
            is RelationalEquality -> {
                val left = left.evaluate()
                val right = right.evaluate()
                if (left is PointerValue && right is PointerValue) {
                    left.referenced.checkReferable()
                    right.referenced.checkReferable()
                    val a = left.referenced.offset
                    val b = right.referenced.offset
                    Value.truth(when (operator.kind) {
                        LESS -> a < b
                        MORE -> a > b
                        LESS_EQUAL -> a <= b
                        MORE_EQUAL -> a >= b
                        EQUAL_EQUAL -> a == b
                        BANG_EQUAL -> a != b
                        else -> error("no evaluate for $this")
                    })
                } else {
                    relationalEquality(left as ArithmeticValue, operator, right as ArithmeticValue)
                }
            }
            is Bitwise -> {
                bitwise(left.evaluate(), operator, right.evaluate(), type)
            }
            is Logical -> {
                val left = left.evaluate() as ArithmeticValue
                when (operator.kind) {
                    AMPERSAND_AMPERSAND -> {
                        if (left.isFalse()) Value.ZERO
                        else (right.evaluate() as ArithmeticValue).normalizeBool()
                    }
                    BAR_BAR -> {
                        if (left.isTrue()) Value.ONE
                        else (right.evaluate() as ArithmeticValue).normalizeBool()
                    }
                    else -> error("no evaluate for $this")
                }
            }
            is Conditional -> {
                val condition = condition.evaluate() as ArithmeticValue
                val result = if (condition.isTrue()) th3n.delayed() else e1se.delayed()
                type.cast(result)
            }
            is Assignment -> {
                targetType = left.type
                val value = left.type.cast(right.evaluate())
                val obj = left.locate()
                obj.assign(value)
                value
            }
            is PlusAssignment -> {
                val target = left.locate()
                val left = target.evaluate()
                val right = right.evaluate() as ArithmeticValue
                val value = when (left) {
                    is ArithmeticValue -> target.type.cast(left + right)
                    is PointerValue -> left + right.value.toInt()
                    else -> error("no evaluate for $this")
                }
                target.assign(value)
                value
            }
            is MinusAssignment -> {
                val target = left.locate()
                val left = target.evaluate()
                val right = right.evaluate() as ArithmeticValue
                val value = when (left) {
                    is ArithmeticValue -> target.type.cast(left - right)
                    is PointerValue -> left - right.value.toInt()
                    else -> error("no evaluate for $this")
                }
                target.assign(value)
                value
            }
            is Comma -> {
                left.evaluate()
                right.evaluate()
            }
            else -> {
                if (!isLocator) error("no evaluate for $this")
                locate().evaluate()
            }
        }
    }

    private fun qsort(base: PointerValue, count: Int, comp: FunctionDefinition) {
        for (k in count - 1 downTo 1) {
            val q = base + k
            for (i in 0 until k) {
                val p = base + i
                val comparison = comp.execute(listOf(p, q))
                if ((comparison as ArithmeticValue).value > 0) {
                    memory.swap(p.referenced, q.referenced)
                }
            }
        }
    }

    private fun bsearch(key: PointerValue, base: PointerValue, count: Int, comp: FunctionDefinition): Value {
        var left = 0
        var right = count
        while (left < right) {
            val middle = (left + right).ushr(1)
            val comparison = (comp.execute(listOf(key, base + middle)) as ArithmeticValue).value
            when {
                comparison < 0 -> right = middle
                comparison > 0 -> left = middle + 1
                else -> return base + middle
            }
        }
        return base + count
    }

    fun allocate(function: Expression, size: Expression, one: (Type) -> PointerValue, many: (ArrayType) -> PointerValue): PointerValue {
        if (targetType === VoidPointerType) {
            function.root().error("cannot infer desired type to allocate via void*")
        }
        val elementType = (targetType as PointerType).referencedType
        val elementSize = elementType.sizeof()
        val requestedBytes = (size.evaluate() as ArithmeticValue).value.toInt()
        val arrayLength = requestedBytes / elementSize
        if (arrayLength * elementSize != requestedBytes) {
            size.root().error("$requestedBytes is not a multiple of $elementSize. Did you forget to multiply by sizeof(element type)?")
        }
        return when (arrayLength) {
            1 -> one(elementType)
            else -> many(ArrayType(arrayLength, elementType))
        }
    }
}

fun unaryPlus(x: Value): Value {
    val a = x as ArithmeticValue
    return Value.ZERO + a
}

fun unaryMinus(x: Value): Value {
    val a = x as ArithmeticValue
    return Value.ZERO - a
}

fun bitwiseNot(x: Value): Value {
    val a = x as ArithmeticValue
    return Value.MINUS_ONE - a
}

fun logicalNot(x: Value): Value {
    val a = x as ArithmeticValue
    return Value.truth(a.isFalse())
}

fun multiplicative(x: Value, operator: Token, y: Value): Value {
    val a = x as ArithmeticValue
    val b = y as ArithmeticValue
    return when (operator.kind) {
        ASTERISK -> a * b
        SLASH -> a / b
        PERCENT -> a % b
        else -> error("no evaluate for $operator")
    }
}

fun plus(x: Value, y: Value): Value {
    val a = x as ArithmeticValue
    val b = y as ArithmeticValue
    return a + b
}

fun minus(x: Value, y: Value): Value {
    val a = x as ArithmeticValue
    val b = y as ArithmeticValue
    return a - b
}

fun shift(x: Value, operator: Token, y: Value, type: Type): Value {
    val a = (x as ArithmeticValue).integralPromotions().value.toLong().toInt()
    val b = (y as ArithmeticValue).integralPromotions().value.toLong().toInt()
    val bits = when {
        operator.kind == LESS_LESS -> log(a, "<< ", b, a.shl(b))
        x.type === UnsignedIntType -> log(a, ">>>", b, a.ushr(b))
        else -> log(a, ">> ", b, a.shr(b))
    }
    return type.cast(Value.signedInt(bits))
}

fun relationalEquality(x: ArithmeticValue, operator: Token, y: ArithmeticValue): Value {
    val commonType = x.type.usualArithmeticConversions(y.type)
    val a = commonType.cast(x).value
    val b = commonType.cast(y).value
    return Value.truth(when (operator.kind) {
        LESS -> a < b
        MORE -> a > b
        LESS_EQUAL -> a <= b
        MORE_EQUAL -> a >= b
        EQUAL_EQUAL -> a == b
        BANG_EQUAL -> a != b
        else -> error("no evaluate for $operator")
    })
}

fun bitwise(x: Value, operator: Token, y: Value, type: Type): Value {
    val a = (x as ArithmeticValue).value.toLong().toInt()
    val b = (y as ArithmeticValue).value.toLong().toInt()
    val result = when (operator.kind) {
        AMPERSAND -> log(a, " & ", b, a.and(b))
        CARET -> log(a, " ^ ", b, a.xor(b))
        BAR -> log(a, " | ", b, a.or(b))
        else -> error("no evaluate for $operator")
    }
    return type.cast(Value.signedInt(result))
}

private fun log(x: Int, operator: String, y: Int, result: Int): Int {
    println()
    println("   " + x.toBinaryString32() + " " + x)
    println(operator + y.toBinaryString32() + " " + y)
    println(" = " + result.toBinaryString32() + " " + result)
    return result
}

private fun Int.toBinaryString32(): String {
    return nib(28) + nib(24) + " " + nib(20) + nib(16) + " " + nib(12) + nib(8) + " " + nib(4) + nib(0)
}

private fun Int.nib(pos: Int): String {
    return NIBBLES[this.ushr(pos).and(15)]
}

private val NIBBLES = arrayOf(
        "0000", "0001", "0010", "0011",
        "0100", "0101", "0110", "0111",
        "1000", "1001", "1010", "1011",
        "1100", "1101", "1110", "1111"
)
