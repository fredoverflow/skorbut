package semantic

import common.Diagnostic
import freditor.Levenshtein
import interpreter.*
import semantic.types.*
import syntax.lexer.Token
import syntax.lexer.TokenKind.*
import syntax.lexer.fakeIdentifier
import syntax.lexer.missingIdentifier
import syntax.tree.*
import text.skipDigits

class TypeChecker(translationUnit: TranslationUnit) {
    private val functionTokens: Map<String, Token> =
        translationUnit.functions.map { it.namedDeclarator.name }.associateBy(Token::text)

    private val symbolTable = SymbolTable()
    val stringLiterals = LinkedHashSet<String>()

    private var staticOffset = Int.MIN_VALUE
    private var currentReturnType: Type = Later
    private var currentStackFrameSymbols = ArrayList<Symbol>()

    init {
        declare(fakeIdentifier("pow"), FunctionType(DoubleType, DoubleType, DoubleType))
        declare(fakeIdentifier("time"), FunctionType(UnsignedIntType, SignedIntType))

        val constCharPointer = PointerType(Const(SignedCharType))
        declare(fakeIdentifier("puts"), FunctionType(VoidType, constCharPointer))
        declare(fakeIdentifier("putchar"), FunctionType(VoidType, SignedIntType))
        declare(fakeIdentifier("getchar"), FunctionType(SignedIntType))

        declare(fakeIdentifier("malloc"), FunctionType(VoidPointerType, UnsignedIntType))
        declare(fakeIdentifier("free"), FunctionType(VoidType, VoidPointerType))
        declare(fakeIdentifier("realloc"), FunctionType(VoidPointerType, VoidPointerType, UnsignedIntType))

        declare(fakeIdentifier("memswap"), FunctionType(VoidType, VoidPointerType, VoidPointerType, UnsignedIntType))

        val predicate = FunctionType(SignedIntType, ConstVoidPointerType, ConstVoidPointerType).pointer()
        declare(
            fakeIdentifier("qsort"),
            FunctionType(VoidType, VoidPointerType, UnsignedIntType, UnsignedIntType, predicate)
        )
        declare(
            fakeIdentifier("bsearch"),
            FunctionType(
                VoidPointerType, ConstVoidPointerType, ConstVoidPointerType, UnsignedIntType, UnsignedIntType, predicate
            )
        )

        declare(fakeIdentifier("strlen"), FunctionType(UnsignedIntType, constCharPointer))
        declare(fakeIdentifier("strcmp"), FunctionType(SignedIntType, constCharPointer, constCharPointer))

        translationUnit.externalDeclarations.forEach {
            when (it) {
                is FunctionDefinition -> it.typeCheck()
                is Declaration -> it.typeCheck()
            }
        }
    }

    private var currentDeclarationIsStatic = false

    private fun declare(name: Token, type: Type): Symbol {
        return if (symbolTable.atGlobalScope()) {
            declareStatic(name, type)
        } else {
            declareAutomatic(name, type)
        }
    }

    private fun declareStatic(name: Token, type: Type): Symbol {
        currentDeclarationIsStatic = true
        val symbol = symbolTable.declare(name, type, staticOffset)
        staticOffset += type.count()
        return symbol
    }

    private fun declareAutomatic(name: Token, type: Type): Symbol {
        currentDeclarationIsStatic = false
        val symbol = symbolTable.declare(name, type, currentStackFrameSymbols.nextOffset())
        if (symbol.type.requiresStorage()) {
            currentStackFrameSymbols.add(symbol)
        }
        return symbol
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

    fun symbolAt(position: Int): Symbol? {
        return symbolTable.symbolAt(position)
    }

    private fun DeclarationSpecifiers.typeCheckNoStorageClass(): Type {
        typeCheck()
        if (storageClass != VOID) root().error("no storage class allowed in this context")
        return type
    }

    private fun DeclarationSpecifiers.typeCheck(): Type {
        determineType()
        applyQualifiers()
        return type
    }

    private fun DeclarationSpecifiers.determineType() {
        type = typeSpecifiers.get(typeTokens)
        if (type == Later) {
            type = when (typeTokens.first()) {
                ENUM -> list.firstNotNullOf { it.enumType() }

                STRUCT -> list.firstNotNullOf { it.structType() }

                IDENTIFIER -> {
                    val specifier = list.find { it.kind() == IDENTIFIER }
                    val primitive = specifier as DeclarationSpecifier.Primitive
                    val identifier = primitive.token
                    val symbol = symbolTable.lookup(identifier)!!
                    val alias = symbol.type as Typedef
                    alias.aliased
                }

                else -> root().error("no determineType for ${typeTokens.first()}")
            }
        }
    }

    private fun DeclarationSpecifiers.applyQualifiers() {
        if (qualifiers.contains(CONST)) {
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
                            val value =
                                init.value ?: init.root().error("enumeration constant must be a compile-time constant")

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
                val temp = symbolTable.lookup(name) ?: name.error("undefined struct $name")
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
            // retain const on function parameters
            type = type.applyQualifiersTo(type.decayed())
            return type
        }
    }

    private fun Declarator.type(from: Type): Type {
        return when (this) {
            is Declarator.Identity -> from

            is Declarator.Pointer -> child.type(from.pointer().let { if (qualifiers.isEmpty()) it else it.addConst() })

            is Declarator.Array -> child.type(ArrayType(determineSize(), from))

            is Declarator.Function -> child.type(
                // ignore top-level const in function types
                FunctionType(from.unqualified(), parameters.map { it.typeCheck().unqualified() })
            )

            is Declarator.Initialized -> declarator.type(from)
        }
    }

    private fun Declarator.Array.determineSize(): Int {
        if (size == null) return 0

        val type = size.typeCheck()
        if (type !is ArithmeticType || !type.isIntegral()) size.root().error("array size must be an integral number")

        val value = size.value ?: size.root().error("array size must be a compile-time constant")

        val size = (value as ArithmeticValue).value.toInt()
        if (size < 1) this.size.root().error("non-positive array size $size")

        return size
    }

    private fun FunctionDefinition.typeCheck() {
        val functionType = namedDeclarator.typeCheck(specifiers.typeCheckNoStorageClass()) as FunctionType
        functionType.defined = true
        currentReturnType = functionType.returnType
        if (currentReturnType is StructType) {
            // What is the lifetime of a returned struct?
            namedDeclarator.name.error("cannot return structs yet")
        }
        currentStackFrameSymbols = ArrayList()
        symbolTable.scoped {
            for (parameter in parameters) {
                with(parameter) {
                    if (!name.wasProvided()) name.error("missing parameter name in function definition")
                    validateType(name, type)
                    val symbol = declare(name, type)
                    offset = symbol.offset
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
                control.typeCheck().unqualified().let {
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
                    choice.value ?: case.error("case label must be a compile-time constant")
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
                symbolTable.scoped {
                    init?.typeCheck()
                    condition?.typeCheck()
                    update?.typeCheck()
                    body.typeCheck()
                }
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
                if (result == null) {
                    if (currentReturnType !== VoidType) r3turn.error("missing return value")
                } else {
                    checkAssignmentCompatibility(currentReturnType, result.root(), result.typeCheck())
                }
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
            namedDeclarator.offset = symbolTable.lookup(namedDeclarator.name)!!.offset
            val declarator = namedDeclarator.declarator
            if (declarator !is Declarator.Initialized) {
                if (type is ArrayType && type.size == 0) {
                    name.error("missing array size or initializer")
                }
                if (type !is FunctionType) validateType(name, type)
            } else {
                val init = declarator.init
                if (type is ArrayType && type.size == 0) {
                    if (init is InitializerList) {
                        type.size = init.list.size
                    } else if (init is ExpressionInitializer && init.expression is StringLiteral) {
                        type.size = init.expression.literal.text.length + 1
                        init.expression.type = type
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
                    if (type.size <= init.expression.literal.text.length) {
                        init.expression.root().error("string literal too long")
                    }
                } else {
                    init.expression.typeCheck()
                    if (currentDeclarationIsStatic && init.expression.value == null && init.expression !is StringLiteral) {
                        init.expression.root().error("static initializers must be compile-time constants")
                    }
                    checkAssignmentCompatibility(type, init.expression.root(), init.expression.type)
                }
            }

            is InitializerList -> when (type) {
                is ArrayType -> {
                    val size = type.size
                    if (size < init.list.size) init.list[size].root().error("too many initializers for $type")
                    if (size > init.list.size) init.list.last().root().error("not enough initializers for $type")
                    init.list.forEach { typeCheck(type.elementType, it) }
                }

                is StructType -> {
                    val size = type.members.size
                    if (size < init.list.size) init.list[size].root().error("too many initializers for $type")
                    if (size > init.list.size) init.list.last().root().error("not enough initializers for $type")
                    for ((member, initializer) in type.members.zip(init.list)) {
                        typeCheck(member.type, initializer)
                    }
                }

                else -> init.openBrace.error("cannot initialize $type with braces")
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
        var radix = 10
        var start = 0
        if (text[0] == '0' && text.length >= 2) {
            when (text[1]) {
                'x', 'X' -> {
                    radix = 16
                    start = 2
                }

                'b', 'B' -> {
                    radix = 2
                    start = 2
                }

                else -> {
                    radix = 8
                    start = 1
                }
            }
        }
        try {
            val x = text.substring(start).toLong(radix)
            if (x <= 0x7fffffff) return Value.signedInt(x.toInt())
            if (x <= 0xffffffff) return Value.unsignedInt(x.toInt())
        } catch (_: NumberFormatException) {
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

                    CHARACTER_CONSTANT -> Value.signedInt(constant.text[0].code.toByte().toInt())

                    else -> error("no value for $this")
                }
                value = temp
                temp.type
            }

            is StringLiteral -> {
                isLocator = true
                if (sizeofNesting == 0) {
                    stringLiterals.add(literal.text)
                }
                ArrayType(literal.text.length + 1, SignedCharType)
            }

            is Identifier -> {
                val symbol = symbolTable.lookup(name)
                if (symbol == null) {
                    val symbol = symbolTable.lookupInClosedScopes(name)
                    if (symbol != null) {
                        name.error("symbol $name no longer in scope", symbol.name)
                    }
                    val functionToken = functionTokens[name.text]
                    if (functionToken != null && functionToken.start > name.start) {
                        name.error("must declare function before use", functionToken)
                    }
                    val bestMatches = Levenshtein.bestMatches(name.text, symbolTable.names().asIterable())
                    if (bestMatches.size == 1) {
                        val bestMatch = bestMatches.first()
                        val prefix = bestMatch.commonPrefixWith(name.text)
                        name.error("undeclared symbol $name, did you mean $bestMatch?", prefix.length)
                    } else {
                        val commaSeparated = bestMatches.joinToString(", ")
                        name.error("undeclared symbol $name, best matches: $commaSeparated")
                    }
                }

                this.symbol = symbol
                symbol.usages.add(this)

                if (symbol.type is EnumerationConstant) {
                    value = symbol.type.value
                    SignedIntType
                } else {
                    isLocator = (symbol.type !is FunctionType)
                    symbol.type
                }
            }

            is PrintfCall -> {
                checkPrintfFormatString()
                SignedIntType
            }

            is ScanfCall -> {
                arguments.forEach {
                    val type = it.typeCheck().decayed()
                    if (type !is PointerType) it.root().error("missing &")
                    if (type.referencedType is ArrayType && type.referencedType.elementType === SignedCharType) {
                        it.root().error("redundant &")
                    }
                }
                SignedIntType
            }

            is Postfix -> {
                val operandType = operand.typeCheck()
                if (operandType.isConst()) operator.error("const", "$operator")
                if (!operand.isLocator) operator.error("value", "$operator")
                if ((operandType is ArithmeticType) || (operandType is PointerType)) {
                    operandType
                } else {
                    operator.error("$operandType", "$operator")
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
                    operator.error("$leftType", "[$rightType]")
                }
            }

            is FunctionCall -> {
                val functionPointerType = function.typeCheck().decayed()
                if (functionPointerType !is PointerType) function.root().error("$functionPointerType is not a function")
                val functionType = functionPointerType.referencedType
                if (functionType !is FunctionType) function.root().error("$functionType is not a function")

                val parameterTypes = functionType.parameters
                val nParameters = parameterTypes.size
                val nArguments = arguments.size
                if (nParameters != nArguments) function.root()
                    .error("function takes $nParameters arguments, not $nArguments")
                for ((parameterType, argument) in parameterTypes.zip(arguments)) {
                    checkAssignmentCompatibility(parameterType, argument.root(), argument.typeCheck())
                }
                functionType.returnType
            }

            is DirectMemberAccess -> {
                isLocator = true
                val leftType = left.typeCheck()
                val structType = leftType.unqualified()

                if (structType is StructType) {
                    val member = structType.member(right) ?: right.error("$right is not a member of $structType")
                    leftType.applyQualifiersTo(member.type)
                } else if (leftType is PointerType && leftType.referencedType.unqualified() is StructType) {
                    dot.error("replace . with -> for indirect member access")
                } else {
                    left.root().error("$structType is not a struct")
                }
            }

            is IndirectMemberAccess -> {
                isLocator = true
                val leftPointerType = left.typeCheck().decayed()
                if (leftPointerType is StructType) arrow.error("replace -> with . for direct member access")
                if (leftPointerType !is PointerType) left.root().error("$leftPointerType is not a struct pointer")
                val leftType = leftPointerType.referencedType
                val structType = leftType.unqualified()

                if (structType is StructType) {
                    val member = structType.member(right) ?: right.error("$right is not a member of $structType")
                    leftType.applyQualifiersTo(member.type)
                } else {
                    left.root().error("$structType is not a struct")
                }
            }

            is Prefix -> {
                val operandType = operand.typeCheck()
                if (operandType.isConst()) operator.error("${operator}const")
                if (!operand.isLocator) operator.error("${operator}value")
                if ((operandType is ArithmeticType) || (operandType is PointerType)) {
                    operandType
                } else {
                    operator.error("${operator}$operandType")
                }
            }

            is Reference -> {
                val operandType = operand.typeCheck()
                if (operandType !is FunctionType) {
                    if (!operand.isLocator) operator.error("${operator}value")
                } else if (operand is Identifier) {
                    value = FunctionDesignator(operand.name, operandType).decayed()
                }
                PointerType(operandType)
            }

            is Dereference -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is PointerType) operator.error("${operator}$operandType")
                isLocator = (operandType.referencedType !is FunctionType)
                operandType.referencedType
            }

            is UnaryPlus -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType) operator.error("${operator}$operandType")
                this.determineValue(::unaryPlus)
                SignedIntType.max(operandType)
            }

            is UnaryMinus -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType) operator.error("${operator}$operandType")
                this.determineValue(::unaryMinus)
                SignedIntType.max(operandType)
            }

            is BitwiseNot -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType || !operandType.isIntegral()) operator.error("${operator}$operandType")
                this.determineValue(::bitwiseNot)
                SignedIntType.max(operandType)
            }

            is LogicalNot -> {
                val operandType = operand.typeCheck().decayed()
                if (operandType !is ArithmeticType) operator.error("${operator}$operandType")
                this.determineValue(::logicalNot)
                SignedIntType
            }

            is SizeofType -> {
                operandType = declarator.type(specifiers.typeCheckNoStorageClass())
                val size = operandType.sizeof()
                if (size == 0) operator.error("sizeof requires object type")
                value = Value.unsignedInt(size)
                UnsignedIntType
            }

            is SizeofExpression -> {
                sizeofContext { operand.typeCheck() }
                val size = operand.type.sizeof()
                if (size == 0) operator.error("sizeof requires object type")
                value = Value.unsignedInt(size)
                UnsignedIntType
            }

            is Multiplicative -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue { a, b -> multiplicative(a, operator, b) }
                    leftType.usualArithmeticConversions(rightType)
                } else {
                    operator.error("$leftType ", "$operator $rightType")
                }
            }

            is Plus -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ComparablePointerType) && (rightType is ArithmeticType)) {
                    leftType
                } else if ((leftType is ArithmeticType) && (rightType is ComparablePointerType)) {
                    rightType
                } else if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue(::plus)
                    leftType.usualArithmeticConversions(rightType)
                } else {
                    operator.error("$leftType ", "$operator $rightType")
                }
            }

            is Minus -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ComparablePointerType) && (rightType is ArithmeticType)) {
                    leftType
                } else if ((leftType is ComparablePointerType) && (rightType is ComparablePointerType)) {
                    SignedIntType
                } else if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue(::minus)
                    leftType.usualArithmeticConversions(rightType)
                } else {
                    operator.error("$leftType ", "$operator $rightType")
                }
            }

            is Shift -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ArithmeticType && leftType.isIntegral()) && (rightType is ArithmeticType && rightType.isIntegral())) {
                    val typ = leftType.integralPromotions()
                    this.determineValue { a, b -> shift(a, operator, b, typ) }
                    typ
                } else {
                    operator.error("$leftType ", "$operator $rightType")
                }
            }

            is RelationalEquality -> {
                val leftType = left.typeCheck().decayed()
                val rightType = right.typeCheck().decayed()
                if ((leftType is ComparablePointerType) && (rightType is ComparablePointerType)) {
                    SignedIntType
                } else if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
                    this.determineValue { a, b ->
                        relationalEquality(a as ArithmeticValue, operator, b as ArithmeticValue)
                    }
                    SignedIntType
                } else {
                    operator.error("$leftType ", "$operator $rightType")
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
                    operator.error("$leftType ", "$operator $rightType")
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
                    operator.error("$leftType ", "$operator $rightType")
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
                        colon.error("$a ", ": $b")
                    }
                } else if (a is ComparablePointerType && b is ComparablePointerType) {
                    // one or more void pointers
                    VoidPointerType
                } else {
                    colon.error("$a ", ": $b")
                }
            }

            is Cast -> {
                val targetType = declarator.type(specifiers.typeCheckNoStorageClass()).unqualified()
                val sourceType = operand.typeCheck()
                checkAssignmentCompatibility(targetType, operator, sourceType)
                this.determineValue { targetType.cast(it) }
                targetType
            }

            is Assignment -> {
                val leftType = left.typeCheck()
                if (leftType.isConst()) operator.error("const ", "$operator ")
                if (!left.isLocator) operator.error("value ", "$operator ")
                val rightType = right.typeCheck()
                checkAssignmentCompatibility(leftType, operator, rightType)
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
        if (leftType.isConst()) operator.error("const ", "$operator ")
        if (!left.isLocator) operator.error("value ", "$operator ")
        val rightType = right.typeCheck().decayed()
        if ((leftType is ArithmeticType) && (rightType is ArithmeticType)) {
            return leftType
        } else if ((leftType is ComparablePointerType) && (rightType is ArithmeticType)) {
            return leftType
        } else {
            operator.error("$leftType ", "$operator $rightType")
        }
    }

    private fun PrintfCall.checkPrintfFormatString() {
        try {
            val args = arguments.iterator()
            val fmt = format.text
            var k = fmt.indexOf('%')
            while (k != -1) {
                if (fmt[++k] != '%') {
                    k = fmt.skipDigits(k) // width
                    if (fmt[k] == '.') {
                        val dot = k
                        k = fmt.skipDigits(k + 1) // precision
                        if (fmt[k] !in "eEfgGs") format.stringErrorAt(dot, ". only works inside %e %E %f %g %G %s")
                    }
                    if (fmt[k] !in "ciudoxXeEfgGspn") format.stringErrorAt(k, "illegal conversion specifier")
                    if (!args.hasNext()) format.stringErrorAt(k, "missing argument after format string")
                    val arg = args.next()
                    checkPrintfConversionSpecifier(fmt[k], arg.typeCheck().decayed(), arg.root())
                }
                k = fmt.indexOf('%', k + 1)
            }
            if (args.hasNext()) args.next().root().error("missing conversion specifier in format string")
        } catch (ex: StringIndexOutOfBoundsException) {
            throw Diagnostic(format.end - 1, "incomplete conversion specifier")
        }
    }

    private fun checkPrintfConversionSpecifier(specifier: Char, type: Type, where: Token) {
        when (specifier) {
            'c', 'i', 'u', 'd', 'o', 'x', 'X' -> if (type !is ArithmeticType || !type.isIntegral()) {
                where.error("%$specifier expects integral type, not $type")
            }

            'e', 'E', 'f', 'g', 'G' -> if (type !is ArithmeticType || type.isIntegral()) {
                where.error("%$specifier expects floating type, not $type")
            }

            's' -> if (type !is PointerType || type.referencedType.unqualified() !== SignedCharType) {
                where.error("%$specifier expects string, not $type")
            }

            'p' -> if (type !is ComparablePointerType) {
                where.error("%$specifier expects pointer, not $type")
            }

            'n' -> where.error("%$specifier not implemented yet")

            else -> where.error("illegal conversion specifier %$specifier")
        }
    }

    private fun checkAssignmentCompatibility(left: Type, operator: Token, right: Type) {
        if (!left.canCastFrom(right)) {
            operator.error("$right\n cannot be converted to \n$left")
        }
    }

    private fun validateType(name: Token, type: Type) {
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
