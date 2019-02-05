package semantic

import interpreter.*
import semantic.types.*
import syntax.lexer.*
import syntax.tree.*
import text.skipDigits

class TypeChecker(translationUnit: TranslationUnit) {
    private val symbolTable = MutableSymbolTable()
    private val stringLiterals = LinkedHashSet<String>()
    fun getStringLiterals(): Set<String> = stringLiterals

    private var staticOffset = Int.MIN_VALUE
    private var currentReturnType: Type = Later
    private var currentStackFrameSymbols = ArrayList<Symbol>()

    init {
        declare(fakeIdentifier("putchar"), FunctionType.unary(SignedIntType, VoidType))
        declare(fakeIdentifier("getchar"), FunctionType.nullary(SignedIntType))
        declare(fakeIdentifier("malloc"), FunctionType.unary(UnsignedIntType, VoidPointerType))
        declare(fakeIdentifier("free"), FunctionType.unary(VoidPointerType, VoidType))
        declare(fakeIdentifier("realloc"), FunctionType.binary(VoidPointerType, UnsignedIntType, VoidPointerType))
        val predicate = FunctionType.binary(ConstVoidPointerType, ConstVoidPointerType, SignedIntType).pointer()
        declare(fakeIdentifier("qsort"), FunctionType(listOf(VoidPointerType, UnsignedIntType, UnsignedIntType, predicate), VoidType))
        declare(fakeIdentifier("bsearch"), FunctionType(listOf(ConstVoidPointerType, ConstVoidPointerType, UnsignedIntType, UnsignedIntType, predicate), VoidPointerType))

        translationUnit.externalDeclarations.forEach {
            when (it) {
                is FunctionDefinition -> it.typeCheck()
                is Declaration -> it.typeCheck()
            }
        }
    }

    private var currentDeclarationIsStatic = false

    private fun declare(name: Token, type: Type) {
        if (symbolTable.atGlobalScope()) {
            declareStatic(name, type)
        } else {
            declareAutomatic(name, type)
        }
    }

    private fun declareStatic(name: Token, type: Type) {
        currentDeclarationIsStatic = true
        symbolTable.declare(name, type, staticOffset)
        staticOffset += type.count()
    }

    private fun declareAutomatic(name: Token, type: Type) {
        currentDeclarationIsStatic = false
        symbolTable.declare(name, type, currentStackFrameSymbols.nextOffset())
        val symbol = symbolTable.lastSymbol()
        if (symbol.type.requiresStorage()) {
            currentStackFrameSymbols.add(symbol)
        }
    }

    private fun ArrayList<Symbol>.nextOffset(): Int {
        if (isEmpty()) return 0
        with(last()) {
            return offset + type.count()
        }
    }

    private fun declareOutside(name: Token, type: Type) {
        assert(type is FunctionType)
        symbolTable.declareOutside(name, type, staticOffset)
    }

    private fun DeclarationSpecifiers.typeCheckNoStorageClass(): Type {
        typeCheck()
        if (storageClass != VOID) root().error("no storage class allowed in this context")
        return type
    }

    private fun DeclarationSpecifiers.typeCheck(): Type {
        val bitset = computeBitset()
        determineStorageClass(bitset)
        determineType(bitset)
        applyQualifiers(bitset)
        return type
    }

    private fun DeclarationSpecifiers.computeBitset(): Int {
        var bitset = 0
        for (specifier in list) {
            val bitmask = 1.shl(specifier.kind().toInt())
            if (bitset.and(bitmask) != 0) {
                specifier.root().error("duplicate declaration specifier")
            }
            if (bitmask.and(STORAGE_CLASS_BITMASK) != 0 && bitset.and(STORAGE_CLASS_BITMASK) != 0) {
                specifier.root().error("multiple storage class specifiers")
            }
            bitset = bitset.or(bitmask)
        }
        return bitset
    }

    private fun DeclarationSpecifiers.determineStorageClass(bitset: Int) {
        storageClass = when (bitset.and(STORAGE_CLASS_BITMASK)) {
            0x00000000 -> VOID
            0x00000002 -> AUTO
            0x00001000 -> EXTERN
            0x00080000 -> REGISTER
            0x01000000 -> STATIC
            0x08000000 -> TYPEDEF

            else -> error("non-exhaustive switch")
        }
    }

    private fun DeclarationSpecifiers.determineType(bitset: Int) {
        type = when (bitset.and(TYPE_SPECIFIER_BITMASK)) {
            0x40000000 -> VoidType

            0x00000010,
            0x00400010 -> SignedCharType

            0x20000010 -> UnsignedCharType

            0x00200000,
            0x00220000,
            0x00600000,
            0x00620000 -> SignedShortType

            0x20200000,
            0x20220000 -> UnsignedShortType

            0x00020000,
            0x00400000,
            0x00420000 -> SignedIntType

            0x20000000,
            0x20020000 -> UnsignedIntType

        // fake SignedLongType with SignedIntType
            0x00040000,
            0x00060000,
            0x00440000,
            0x00460000 -> SignedIntType

        // fake UnsignedLongType with UnsignedIntType
            0x20040000,
            0x20060000 -> UnsignedIntType

            0x00002000 -> FloatType

            0x00000200 -> DoubleType

            0x00000800 -> list.mapNotNull { it.enumType() }.first()

            0x02000000 -> list.mapNotNull { it.structType() }.first()

            0x00100000 -> {
                val specifier = list.find { it.kind() == IDENTIFIER }
                val primitive = specifier as DeclarationSpecifier.Primitive
                val identifier = primitive.token
                val symbol = symbolTable.lookup(identifier)!!
                val alias = symbol.type as Typedef
                alias.aliased
            }

            else -> root().error("illegal combination of declaration specifiers")
        }
    }

    private fun DeclarationSpecifiers.applyQualifiers(bitset: Int) {
        if (bitset.and(CONST_QUALIFIER_BITMASK) != 0) {
            type = type.addConst()
        }
    }

    private fun DeclarationSpecifier.enumType(): Type? {
        return when (this) {
            is DeclarationSpecifier.EnumDef -> {
                if (name.wasProvided()) declare(name, TypedefSignedIntType)
                var counter = 0
                for (enumerator in body) {
                    with(enumerator) {
                        if (init != null) {
                            val type = init.typeCheck()
                            if (type !is ArithmeticType || !type.isIntegral()) {
                                init.root().error("enumeration constant must be an integral number")
                            }
                            val value = init.value
                            if (value == null) init.root().error("enumeration constant must be a compile-time constant")

                            counter = (value as ArithmeticValue).value.toInt()
                        }
                        declare(name, EnumerationConstant(Value.signedInt(counter)))
                        ++counter
                    }
                }
                SignedIntType
            }

            is DeclarationSpecifier.EnumRef -> SignedIntType

            else -> null
        }
    }

    private fun DeclarationSpecifier.structType(): Type? {
        return when (this) {
            is DeclarationSpecifier.StructDef -> {
                val members = ArrayList<Symbol>()
                val structType = StructType(name, members)
                if (name.wasProvided()) declare(name, StructTag(structType))
                var offset = 0
                for (structDeclaration in body) {
                    val specifierType = structDeclaration.specifiers.typeCheckNoStorageClass()
                    for (namedDeclarator in structDeclaration.declarators) {
                        val type = namedDeclarator.typeCheck(specifierType)
                        validateType(namedDeclarator.name, type)
                        members.add(Symbol(namedDeclarator.name, type, offset))
                        offset += type.count()
                    }
                }
                structType.makeComplete()
            }

            is DeclarationSpecifier.StructRef -> {
                val temp = symbolTable.lookup(name)
                if (temp == null) name.error("undefined struct $name")
                (temp.type as StructTag).structType
            }

            else -> null
        }
    }

    private fun NamedDeclarator.typeCheck(specifierType: Type): Type {
        type = declarator.type(specifierType)
        return type
    }

    private fun FunctionParameter.typeCheck(): Type {
        with(namedDeclarator) {
            typeCheck(specifiers.typeCheckNoStorageClass())
            type = type.decayed()
            return type
        }
    }

    private fun Declarator.type(from: Type): Type {
        return when (this) {
            is Declarator.Identity -> from
            is Declarator.Pointer -> previous.type(from).pointer().let { if (qualifiers.isEmpty()) it else it.addConst() }
            is Declarator.Array -> ArrayType(determineLength(), previous.type(from))
            is Declarator.Function -> FunctionType(parameters.map { it.typeCheck() }, previous.type(from).unqualified())
            is Declarator.Initialized -> declarator.type(from)
        }
    }

    private fun Declarator.Array.determineLength(): Int {
        if (length == null) return 0

        val type = length.typeCheck()
        if (type !is ArithmeticType || !type.isIntegral()) length.root().error("array length must be an integral number")

        val value = length.value
        if (value == null) length.root().error("array length must be a compile-time constant")

        val len = (value as ArithmeticValue).value.toInt()
        if (len < 1) length.root().error("non-positive array length $len")

        return len
    }

    private fun FunctionDefinition.typeCheck() {
        val functionType = namedDeclarator.typeCheck(specifiers.typeCheckNoStorageClass()) as FunctionType
        functionType.defined = true
        currentReturnType = functionType.returnType
        currentStackFrameSymbols = ArrayList<Symbol>()
        symbolTable.scoped {
            for (parameter in parameters) {
                with(parameter) {
                    if (!name.wasProvided()) name.error("missing name for parameter in function definition")
                    validateType(name, type)
                    declare(name, type)
                    offset = symbolTable.lastSymbol().offset
                }
            }
            declareOutside(namedDeclarator.name, functionType)
            body.typeCheck()
        }
        if (currentStackFrameSymbols.isEmpty()) {
            currentStackFrameSymbols.add(Symbol(missingIdentifier, SignedCharType, 0))
        }
        stackFrameType = StructType(namedDeclarator.name, currentStackFrameSymbols)
    }

    private fun List<Statement>.typeCheck() {
        return forEach { it.typeCheck() }
    }

    private fun Statement.typeCheck() {
        when (this) {
            is Declaration -> {
                typeCheck()
            }
            is Block -> {
                symbolTable.scoped { statements.typeCheck() }
            }
            is ExpressionStatement -> {
                expression.typeCheck()
            }
            is IfThenElse -> {
                condition.typeCheck()
                th3n.typeCheck()
                e1se?.typeCheck()
            }
            is Switch -> {
                control.typeCheck().let {
                    if (it !is ArithmeticType || !it.isIntegral()) {
                        switch.error("switch control expression must be of integral type")
                    }
                }
                body.typeCheck()
            }
            is Case -> {
                choice.typeCheck().let {
                    if (it !is ArithmeticType || !it.isIntegral()) {
                        case.error("case label must be an integral constant")
                    }
                    val value = choice.value
                    if (value == null) {
                        case.error("case label must be a compile-time constant")
                    }
                }
                body.typeCheck()
            }
            is Default -> {
                body.typeCheck()
            }
            is While -> {
                condition.typeCheck()
                body.typeCheck()
            }
            is Do -> {
                body.typeCheck()
                condition.typeCheck()
            }
            is For -> {
                init?.run { typeCheck() }
                condition?.run { typeCheck() }
                update?.run { typeCheck() }
                body.typeCheck()
            }
            is LabeledStatement -> {
                statement.typeCheck()
            }
            is Goto -> {
            }
            is Continue -> {
            }
            is Break -> {
            }
            is Return -> {
                checkAssignmentCompatible(currentReturnType, result.root(), result.typeCheck())
            }
            is Assert -> {
                if (condition is Assignment) condition.root().error("= is assignment, did you mean == instead?")
                condition.typeCheck()
            }
            else -> error("no typeCheck for $this")
        }
    }

    private fun Declaration.typeCheck() {
        val specifierType = specifiers.typeCheck()
        for (namedDeclarator in namedDeclarators) {
            val name = namedDeclarator.name
            val type = namedDeclarator.typeCheck(specifierType)
            when (specifiers.storageClass) {
                TYPEDEF -> declare(name, Typedef(type))
                STATIC -> declareStatic(name, type)
                else -> declare(name, type)
            }
            namedDeclarator.offset = symbolTable.lastSymbol().offset
            val declarator = namedDeclarator.declarator
            if (declarator !is Declarator.Initialized) {
                if (type is ArrayType && type.length == 0) {
                    if (type.elementType === SignedCharType) {
                        name.error("arrays of unknown size must be initialized with braces or string literals")
                    } else {
                        name.error("arrays of unknown size must be initialized with braces")
                    }
                }
                if (type !is FunctionType) validateType(name, type)
            } else {
                val init = declarator.init
                if (type is ArrayType && type.length == 0) {
                    if (init is InitializerList) {
                        type.length = init.list.size
                    } else if (init is ExpressionInitializer && init.expression is StringLiteral) {
                        type.length = init.expression.literal.text.length + 1
                    }
                    if (currentDeclarationIsStatic) staticOffset += type.count()
                }
                typeCheck(type, init)
            }
        }
    }

    private fun typeCheck(qualified: Type, init: Initializer) {
        val type = qualified.unqualified()
        when (init) {
            is ExpressionInitializer -> {
                if (init.expression is StringLiteral && type is ArrayType && type.elementType == SignedCharType) {
                    if (type.length <= init.expression.literal.text.length) {
                        init.expression.root().error("string literal too long")
                    }
                } else {
                    init.expression.typeCheck()
                    if (currentDeclarationIsStatic && init.expression.value == null && init.expression !is StringLiteral) {
                        init.expression.root().error("static variables can only be initialized with compile-time constants")
                    }
                    checkAssignmentCompatible(type, init.expression.root(), init.expression.type)
                }
            }
            is InitializerList -> {
                if (type is ArrayType) {
                    val length = type.length
                    if (length < init.list.size) init.list[length].root().error("too many initializers for $type")
                    if (length > init.list.size) init.list.last().root().error("not enough initializers for $type")
                    init.list.forEach { typeCheck(type.elementType, it) }
                } else if (type is StructType) {
                    val length = type.members.size
                    if (length < init.list.size) init.list[length].root().error("too many initializers for $type")
                    if (length > init.list.size) init.list.last().root().error("not enough initializers for $type")
                    for ((member, initializer) in type.members.zip(init.list)) {
                        typeCheck(member.type, initializer)
                    }
                } else {
                    init.openBrace.error("cannot initialize $type with braces")
                }
            }
        }
    }

    private var sizeofNesting = 0

    private inline fun sizeofContext(f: () -> Unit) {
        ++sizeofNesting
        try {
            f()
        } finally {
            --sizeofNesting
        }
    }

    private inline fun <T : Unary> T.determineValue(f: (Value) -> Value) {
        if (sizeofNesting == 0) {
            val v = operand.value
            if (v != null) {
                value = f(v)
            }
        }
    }

    private inline fun <T : Binary> T.determineValue(f: (Value, Value) -> Value) {
        if (sizeofNesting == 0) {
            val v = left.value
            if (v != null) {
                val w = right.value
                if (w != null) {
                    value = f(v, w)
                }
            }
        }
    }

    private fun Token.integer(): ArithmeticValue {
        if (text.length <= 18) {
            val x = java.lang.Long.decode(text)
            if (x <= 0x7fffffff) return Value.signedInt(x.toInt())
            if (x <= 0xffffffff) return Value.unsignedInt(x.toInt())
        }
        error("integer literal $text is too large, allowed maximum is 4294967295")
    }

    private fun Expression.typeCheck(): Type {
        type = when (this) {
            is Constant -> {
                val constant = constant
                val temp = when (constant.kind) {
                    DOUBLE_CONSTANT -> Value.double(constant.text.toDouble())
                    FLOAT_CONSTANT -> Value.float(constant.text.toFloat())
                    INTEGER_CONSTANT -> constant.integer()
                    CHARACTER_CONSTANT -> Value.signedChar(constant.text[0].toInt())
                    else -> error("no value for $this")
                }
                value = temp
                temp.type
            }
            is StringLiteral -> {
                isLocator = true
                stringLiterals.add(literal.text)
                ArrayType(literal.text.length + 1, SignedCharType)
            }
            is Identifier -> {
                val temp = symbolTable.lookup(name)
                if (temp == null) name.error("undeclared symbol $name")

                symbol = temp
                if (temp.type is EnumerationConstant) {
                    value = temp.type.value
                    SignedIntType
                } else {
                    isLocator = (temp.type !is FunctionType)
                    temp.type
                }
            }
            is PrintfCall -> {
                checkPrintfFormatString()
                SignedIntType
            }
            is ScanfCall -> {
                arguments.forEach {
                    val type = it.typeCheck().decayed()
                    if (type !is PointerType) it.root().error("Did you forget the & before the variable?")
                    if (type.referencedType is ArrayType && type.referencedType.elementType === SignedCharType) {
                        it.root().error("Strings do not need the & before the variable.")
                    }
                }
                SignedIntType
            }
            is Postfix -> {
                val operandType = operand.typeCheck()
                if (operandType.isConst()) operator.error("assignment to const")
                checkLocator(operator, operand)
                if ((operandType is ArithmeticType) || (operandType is PointerType)) {
                    operandType
                } else {
                    operator.error("needs arithmetic or pointer operand")
                }
            }
            is Subscript -> {
                isLocator = true
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is PointerType) && (rightType is ArithmeticType)) {
                    leftType.referencedType
                } else if ((leftType is ArithmeticType) && (rightType is PointerType)) {
                    rightType.referencedType
                } else {
                    operator.error("needs pointer and arithmetic operands")
                }
            }
            is FunctionCall -> {
                val functionPointerType = function.typeCheck().decayed()
                if (functionPointerType !is PointerType) function.root().error("not a function")
                val functionType = functionPointerType.referencedType
                if (functionType !is FunctionType) function.root().error("not a function")

                val parameterTypes = functionType.parameters
                val nParameters = parameterTypes.size
                val argumentTypes = arguments.map { it.typeCheck() }
                val nArguments = argumentTypes.size
                if (nParameters != nArguments) function.root().error("function takes $nParameters arguments, not $nArguments")
                for ((parameterType, argumentType) in parameterTypes.zip(argumentTypes)) {
                    checkAssignmentCompatible(parameterType, function.root(), argumentType)
                }
                functionType.returnType
            }
            is DirectMemberAccess -> {
                isLocator = true
                val leftType = left.typeCheck()
                val structType = leftType.unqualified()

                if (structType is StructType) {
                    val member = structType.member(right)
                    if (member == null) right.error("$right is not a member of $leftType")
                    member.type.addQualifiersFrom(leftType)
                } else if (leftType is PointerType && leftType.referencedType.unqualified() is StructType) {
                    dot.error("Use -> instead of . for indirect member access")
                } else {
                    dot.error("$leftType is not a struct type")
                }
            }
            is IndirectMemberAccess -> {
                isLocator = true
                val leftPointerType = left.typeCheck().decayed()
                if (leftPointerType is StructType) arrow.error("Use . instead of -> for direct member access")
                if (leftPointerType !is PointerType) arrow.error("$leftPointerType is not a pointer type")
                val leftType = leftPointerType.referencedType
                val structType = leftType.unqualified()

                if (structType is StructType) {
                    val member = structType.member(right)
                    if (member == null) right.error("$right is not a member of $leftType")
                    member.type.addQualifiersFrom(leftType)
                } else {
                    arrow.error("$leftType is not a struct type")
                }
            }
            is Prefix -> {
                val operandType = operand.typeCheck()
                if (operandType.isConst()) operator.error("assignment to const")
                checkLocator(operator, operand)
                if ((operandType is ArithmeticType) || (operandType is PointerType)) {
                    operandType
                } else {
                    operator.error("needs arithmetic or pointer operand")
                }
            }
            is Reference -> {
                val operandType = operand.typeCheck()
                if (operandType !is FunctionType) {
                    checkLocator(operator, operand)
                } else if (operand is Identifier) {
                    value = FunctionDesignator(operand.name, operandType).decayed()
                }
                PointerType(operandType)
            }
            is Dereference -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is PointerType) operator.error("needs pointer operand")
                isLocator = (operandType.referencedType !is FunctionType)
                operandType.referencedType
            }
            is UnaryPlus -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType) operator.error("needs arithmetic operand")
                this.determineValue(::unaryPlus)
                SignedIntType.max(operandType)
            }
            is UnaryMinus -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType) operator.error("needs arithmetic operand")
                this.determineValue(::unaryMinus)
                SignedIntType.max(operandType)
            }
            is BitwiseNot -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType || !operandType.isIntegral()) operator.error("needs integral operand")
                this.determineValue(::bitwiseNot)
                SignedIntType.max(operandType)
            }
            is LogicalNot -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType) operator.error("needs arithmetic operand")
                this.determineValue(::logicalNot)
                SignedIntType
            }
            is SizeofType -> {
                operandType = declarator.type(specifiers.typeCheckNoStorageClass())
                value = Value.unsignedInt(operandType.sizeof())
                UnsignedIntType
            }
            is SizeofExpression -> {
                sizeofContext { operand.typeCheck() }
                value = Value.unsignedInt(operand.type.sizeof())
                UnsignedIntType
            }
            is Multiplicative -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue { a, b -> multiplicative(a, operator, b) }
                    leftType.usualArithmeticConversions(rightType)
                } else {
                    operator.error("needs arithmetic operands")
                }
            }
            is Plus -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is PointerType) && (rightType is ArithmeticType)) {
                    leftType
                } else if ((leftType is ArithmeticType) && (rightType is PointerType)) {
                    rightType
                } else if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue(::plus)
                    leftType.usualArithmeticConversions(rightType)
                } else {
                    operator.error("needs arithmetic operands or pointer and arithmetic operands")
                }
            }
            is Minus -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is PointerType) && (rightType is ArithmeticType)) {
                    leftType
                } else if ((leftType is PointerType) && (rightType is PointerType)) {
                    SignedIntType
                } else if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue(::minus)
                    leftType.usualArithmeticConversions(rightType)
                } else {
                    operator.error("needs pointer and/or arithmetic operands")
                }
            }
            is Shift -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ArithmeticType && leftType.isIntegral()) && (rightType is ArithmeticType && rightType.isIntegral())) {
                    this.determineValue { a, b -> shift(a, operator, b) }
                    leftType.integralPromotions()
                } else {
                    operator.error("needs integral operands")
                }
            }
            is RelationalEquality -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ComparablePointerType) && (rightType is ComparablePointerType)) {
                    SignedIntType
                } else if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue { a, b -> relationalEquality(a as ArithmeticValue, operator, b as ArithmeticValue) }
                    SignedIntType
                } else {
                    operator.error("needs arithmetic or pointer operands")
                }
            }
            is Bitwise -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ArithmeticType && leftType.isIntegral()) && (rightType is ArithmeticType && rightType.isIntegral())) {
                    val typ = leftType.usualArithmeticConversions(rightType)
                    this.determineValue { a, b -> bitwise(a, operator, b, typ) }
                    typ
                } else {
                    operator.error("needs integral operands")
                }
            }
            is Logical -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue { a, b ->
                        when (operator.kind) {
                            AMPERSAND_AMPERSAND -> {
                                if ((a as ArithmeticValue).isFalse()) Value.ZERO
                                else (b as ArithmeticValue).normalizeBool()
                            }
                            BAR_BAR -> {
                                if ((a as ArithmeticValue).isTrue()) Value.ONE
                                else (b as ArithmeticValue).normalizeBool()
                            }
                            else -> error("no evaluate for $operator")
                        }
                    }
                    SignedIntType
                } else {
                    operator.error("needs arithmetic operands")
                }
            }
            is Conditional -> {
                condition.typeCheck().decayed()
                val a = th3n.typeCheck().decayed()
                val b = e1se.typeCheck().decayed()
                if (a is ArithmeticType && b is ArithmeticType) {
                    a.usualArithmeticConversions(b)
                } else if (a is VoidType && b is VoidType) {
                    VoidType
                } else if (a is PointerType && b is PointerType) {
                    if (a.referencedType == b.referencedType) {
                        a
                    } else {
                        colon.error("$a and $b refer to different types")
                    }
                } else if (a is ComparablePointerType && b is ComparablePointerType) {
                    // one or more void pointers
                    VoidPointerType
                } else {
                    colon.error("$a and $b have no common supertype")
                }
            }
            is Assignment -> {
                val leftType = left.typeCheck()
                if (leftType.isConst()) operator.error("assignment to const")
                checkLocator(operator, left)
                val rightType = right.typeCheck()
                checkAssignmentCompatible(leftType, operator, rightType)
                leftType
            }
            is PlusAssignment -> {
                typeCheckPlusMinusAssignment()
            }
            is MinusAssignment -> {
                typeCheckPlusMinusAssignment()
            }
            is Comma -> {
                left.typeCheck()
                right.typeCheck().decayed()
            }
            else -> error("no typeCheck for $this")
        }
        return type
    }

    private fun Binary.typeCheckPlusMinusAssignment(): Type {
        val leftType = left.typeCheck()
        if (leftType.isConst()) operator.error("assignment to const")
        checkLocator(operator, left)
        val rightType = right.typeCheck()
        if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
            return leftType
        } else if ((leftType is PointerType) && (rightType is ArithmeticType)) {
            return leftType
        } else {
            operator.error("needs pointer and/or arithmetic operands")
        }
    }

    private fun PrintfCall.checkPrintfFormatString() {
        try {
            val args = arguments.iterator()
            val fmt = format.text
            var k = fmt.indexOf('%')
            while (k != -1) {
                if (fmt[++k] != '%') {
                    k = fmt.skipDigits(k)
                    val specifier = fmt[k]
                    if (!args.hasNext()) format.error("missing argument for %$specifier")
                    val arg = args.next()
                    checkPrintfConversionSpecifier(specifier, arg.typeCheck().decayed(), arg.root())
                }
                k = fmt.indexOf('%', k + 1)
            }
            if (args.hasNext()) args.next().root().error("missing conversion specifier")
        } catch (ex: StringIndexOutOfBoundsException) {
            format.error("incomplete conversion specifier")
        }
    }

    private fun checkPrintfConversionSpecifier(specifier: Char, type: Type, where: Token) {
        when (specifier) {
            'c', 'd', 'u', 'x' -> if (type !is ArithmeticType || !type.isIntegral()) {
                where.error("%$specifier expects integral type, not $type")
            }
            'f' -> if (type !is ArithmeticType || type.isIntegral()) {
                where.error("%$specifier expects floating type, not $type")
            }
            's' -> if (type !is PointerType || type.referencedType.unqualified() !== SignedCharType) {
                where.error("%$specifier expects string, not $type")
            }
            'i', 'o', 'X', 'e', 'E', 'g', 'G', 'p', 'n' -> where.error("%$specifier not implemented yet")
            else -> where.error("illegal conversion specifier %$specifier")
        }
    }

    private fun checkLocator(operator: Token, operand: Expression) {
        if (!operand.isLocator) operator.error("$operand is not a locator")
    }

    private fun checkAssignmentCompatible(left: Type, operator: Token, right: Type) {
        if (!left.canCastFrom(right)) {
            operator.error("$right cannot be converted to $left")
        }
    }

    fun validateType(name: Token, type: Type) {
        if (!type.isComplete()) name.error("incomplete type $type")
        when (type) {
            is ArrayType -> validateType(name, type.elementType)
            is PointerType -> {
                val t = type.referencedType
                if (t !is StructType && t !is FunctionType) validateType(name, t)
            }
        }
    }
}

const val STORAGE_CLASS_BITMASK = 0x09081002
const val TYPE_SPECIFIER_BITMASK = 0x72762a10
const val CONST_QUALIFIER_BITMASK = 0x00000020
