package syntax

import semantic.MutableSymbolTable
import semantic.types.MarkerIsTypedefName
import semantic.types.MarkerNotTypedefName
import java.util.*

enum class DeclarationState {
    OPEN, PRIMITIVE, USER_DEFINED, NO_DECLARATOR_REQUIRED
}

class Parser(lexer: Lexer) : ParserBase(lexer) {
    private val symbolTable = MutableSymbolTable()

    private fun declare(name: Token, isTypedefName: Boolean) {
        symbolTable.declare(name, if (isTypedefName) MarkerIsTypedefName else MarkerNotTypedefName, 0)
    }

    private fun Token.isTypedefName(): Boolean {
        return symbolTable.lookup(this)?.type === MarkerIsTypedefName
    }

    private var declarationState = DeclarationState.OPEN

    private fun Token.isDeclarationSpecifier(): Boolean {
        declarationState = DeclarationState.OPEN
        return isAcceptableDeclarationSpecifier()
    }

    private fun Token.isAcceptableDeclarationSpecifier(): Boolean = when (kind) {
        TYPEDEF, EXTERN, STATIC, AUTO, REGISTER -> true

        VOID, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, SIGNED, UNSIGNED ->
            enterOrRemainInDeclarationState(DeclarationState.PRIMITIVE)

        STRUCT, UNION, ENUM -> enterDeclarationState(DeclarationState.USER_DEFINED)

        IDENTIFIER -> if (isTypedefName()) enterDeclarationState(DeclarationState.USER_DEFINED) else false

        CONST, VOLATILE -> true

        else -> false
    }

    private fun enterOrRemainInDeclarationState(desiredState: DeclarationState): Boolean {
        if (declarationState == DeclarationState.OPEN) {
            declarationState = desiredState
            return true
        }
        return declarationState == desiredState
    }

    private fun enterDeclarationState(desiredState: DeclarationState): Boolean {
        if (declarationState == DeclarationState.OPEN) {
            declarationState = desiredState
            return true
        }
        return false
    }

    // 3.3.1 Primary expressions
    private fun primaryExpression(): Expression = when (current) {
        IDENTIFIER -> primaryIdentifier()
        DOUBLE_CONSTANT,
        FLOAT_CONSTANT,
        INTEGER_CONSTANT,
        CHARACTER_CONSTANT -> Constant(consume(token))
        STRING_LITERAL -> StringLiteral(consume(token))
        OPEN_PAREN -> parenthesized { expression() }
        else -> illegalStartOf("expression")
    }

    private fun primaryIdentifier(): Expression {
        val name = expect(IDENTIFIER)
        return when (name.text) {
            "printf" -> parenthesized { printfCall(name.withTokenKind(PRINTF)) }
            "scanf" -> parenthesized { scanfCall(name.withTokenKind(SCANF)) }
            else -> Identifier(name)
        }
    }

    private fun printfCall(printf: Token): Expression {
        val format = expect(STRING_LITERAL)
        val arguments = if (current == COMMA) {
            next()
            commaSeparatedList1 { assignmentExpression() }
        } else {
            Collections.emptyList()
        }
        return PrintfCall(printf, format, arguments)
    }

    private fun scanfCall(scanf: Token): Expression {
        val format = expect(STRING_LITERAL)
        val arguments = if (current == COMMA) {
            next()
            commaSeparatedList1 { assignmentExpression() }
        } else {
            Collections.emptyList()
        }
        return ScanfCall(scanf, format, arguments)
    }

    // 3.3.2 Postfix operators
    private fun postfixExpression(primary: Expression): Expression {
        var left = primary
        while (true) {
            when (current) {
                OPEN_BRACKET -> left = Subscript(left, token, bracketed { expression() })
                OPEN_PAREN -> left = FunctionCall(left, parenthesized { commaSeparatedList0(CLOSE_PAREN) { assignmentExpression() } })
                DOT -> left = DirectMemberAccess(left, consume(token), expect(IDENTIFIER))
                ARROW -> left = IndirectMemberAccess(left, consume(token), expect(IDENTIFIER))
                PLUS_PLUS,
                MINUS_MINUS -> left = Postfix(consume(token), left)
                else -> return left
            }
        }
    }

    // 3.3.3 Unary operators
    private fun unaryExpression(): Expression = when (current) {
        PLUS_PLUS,
        MINUS_MINUS -> Prefix(consume(token), unaryExpression())
        AMP -> Reference(consume(token), unaryExpression())
        STAR -> Dereference(consume(token), unaryExpression())
        PLUS -> UnaryPlus(consume(token), castExpression())
        MINUS -> UnaryMinus(consume(token), castExpression())
        TILDE -> BitwiseNot(consume(token), castExpression())
        BANG -> LogicalNot(consume(token), castExpression())
        SIZEOF -> sizeof()
        else -> postfixExpression(primaryExpression())
    }

    private fun sizeof(): Expression {
        val operator = token
        if (next() == OPEN_PAREN) {
            next()
            val specifiers = declarationSpecifiers0()
            if (!specifiers.isEmpty()) {
                val result = SizeofType(operator, DeclarationSpecifiers(specifiers), abstractDeclarator())
                expect(CLOSE_PAREN)
                return result
            } else {
                val primary = expression()
                expect(CLOSE_PAREN)
                val unary = postfixExpression(primary)
                return SizeofExpression(operator, unary)
            }
        } else {
            return SizeofExpression(operator, unaryExpression())
        }
    }

    // 3.3.4 Cast operators
    private fun castExpression(): Expression {
        if (current == OPEN_PAREN) {
            next()
            val specifiers = declarationSpecifiers0()
            if (!specifiers.isEmpty()) {
                notImplementedYet("casting")
            } else {
                val primary = expression()
                expect(CLOSE_PAREN)
                val unary = postfixExpression(primary)
                return unary
            }
        } else {
            return unaryExpression()
        }
    }

    // 3.3.5 Multiplicative operators
    private fun multiplicativeExpression(): Expression {
        var left = castExpression()
        while (true) {
            when (current) {
                STAR,
                SLASH,
                PERCENT -> left = Multiplicative(left, consume(token), castExpression())
                else -> return left
            }
        }
    }

    // 3.3.6 Additive operators
    private fun additiveExpression(): Expression {
        var left = multiplicativeExpression()
        while (true) {
            when (current) {
                PLUS -> left = Plus(left, consume(token), multiplicativeExpression())
                MINUS -> left = Minus(left, consume(token), multiplicativeExpression())
                else -> return left
            }
        }
    }

    // 3.3.7 Bitwise shift operators
    private fun shiftExpression(): Expression {
        var left = additiveExpression()
        while (true) {
            when (current) {
                LESS_LESS -> left = Shift(left, consume(token), additiveExpression())
                MORE_MORE -> left = Shift(left, consume(token), additiveExpression())
                else -> return left
            }
        }
    }

    // 3.3.8 Relational operators
    private fun relationalExpression(): Expression {
        var left = shiftExpression()
        while (true) {
            when (current) {
                LESS,
                MORE,
                LESS_EQ,
                MORE_EQ -> left = RelationalEquality(left, consume(token), shiftExpression())
                else -> return left
            }
        }
    }

    // 3.3.9 Equality operators
    private fun equalityExpression(): Expression {
        var left = relationalExpression()
        while (true) {
            when (current) {
                EQ_EQ,
                BANG_EQ -> left = RelationalEquality(left, consume(token), relationalExpression())
                else -> return left
            }
        }
    }

    // 3.3.10 Bitwise AND operator
    private fun bitwiseAndExpression(): Expression {
        var left = equalityExpression()
        while (current == AMP) {
            left = Bitwise(left, consume(token), equalityExpression())
        }
        return left
    }

    // 3.3.11 Bitwise exclusive OR operator
    private fun bitwiseXorExpression(): Expression {
        var left = bitwiseAndExpression()
        while (current == CARET) {
            left = Bitwise(left, consume(token), bitwiseAndExpression())
        }
        return left
    }

    // 3.3.12 Bitwise inclusive OR operator
    private fun bitwiseOrExpression(): Expression {
        var left = bitwiseXorExpression()
        while (current == PIPE) {
            left = Bitwise(left, consume(token), bitwiseXorExpression())
        }
        return left
    }

    // 3.3.13 Logical AND operator
    private fun logicalAndExpression(): Expression {
        val left = bitwiseOrExpression()
        if (current != AMP_AMP) return left

        return Logical(left, consume(token), logicalAndExpression())
    }

    // 3.3.14 Logical OR operator
    private fun logicalOrExpression(): Expression {
        val left = logicalAndExpression()
        if (current != PIPE_PIPE) return left

        return Logical(left, consume(token), logicalOrExpression())
    }

    // 3.3.15 Conditional operator
    private fun conditionalExpression(): Expression {
        val condition = logicalOrExpression()
        if (current != QUESTION) return condition

        return Conditional(condition, consume(token), expression(), consume(token), conditionalExpression())
    }

    // 3.3.16 Assignment operators
    private fun assignmentExpression(): Expression {
        val left = conditionalExpression()
        return when (current) {
            EQ -> Assignment(left, consume(token), assignmentExpression())
            PLUS_EQ -> PlusAssignment(left, consume(token), assignmentExpression())
            MINUS_EQ -> MinusAssignment(left, consume(token), assignmentExpression())
            STAR_EQ,
            SLASH_EQ,
            PERCENT_EQ,
            LESS_LESS_EQ,
            MORE_MORE_EQ,
            AMP_EQ,
            CARET_EQ,
            PIPE_EQ -> notImplementedYet("compound assignment")
            else -> left
        }
    }

    // 3.3.17 Comma operator
    fun expression(): Expression {
        val left = assignmentExpression()
        if (current != COMMA) return left

        val comma = Comma(left, consume(token), expression())
        return comma
    }

    // 3.5 Declarations
    fun declaration(): Statement {
        val specifiers = declarationSpecifiers1()
        val isTypedef = specifiers.list.any { it.kind() == TYPEDEF }
        val declarators = commaSeparatedList0(SEMICOLON) {
            initDeclarator().apply { declare(name, isTypedef) }
        }
        expect(SEMICOLON)
        return Declaration(specifiers, declarators)
    }

    fun declarationSpecifiers1(): DeclarationSpecifiers {
        val specifiers = declarationSpecifiers0()
        if (specifiers.isEmpty()) illegalStartOf("declaration")
        return DeclarationSpecifiers(specifiers)
    }

    fun declarationSpecifiers0(): List<DeclarationSpecifier> {
        declarationState = DeclarationState.OPEN
        return list0While({ token.isAcceptableDeclarationSpecifier() }) { declarationSpecifier() }
    }

    fun declarationSpecifier(): DeclarationSpecifier {
        return when (current) {
            ENUM -> enumSpecifier()
            STRUCT -> structSpecifier()
            UNION -> notImplementedYet("unions")
            else -> consume(DeclarationSpecifier.Primitive(token))
        }
    }

    private fun enumSpecifier(): DeclarationSpecifier {
        declarationState = DeclarationState.NO_DECLARATOR_REQUIRED
        if (next() == OPEN_BRACE) {
            // anonymous enum
            return DeclarationSpecifier.EnumDef(token, enumBody())
        } else {
            val name = expect(IDENTIFIER).tagged()
            if (current == OPEN_BRACE) {
                // named enum
                return DeclarationSpecifier.EnumDef(name, enumBody())
            } else {
                return DeclarationSpecifier.EnumRef(name)
            }
        }
    }

    private fun enumBody() = braced { commaSeparatedList1 { enumerator() } }

    private fun enumerator(): Enumerator {
        val name = expect(IDENTIFIER)
        val init = if (current == EQ) {
            next()
            assignmentExpression()
        } else null
        return Enumerator(name, init)
    }

    private fun structSpecifier(): DeclarationSpecifier {
        if (next() == OPEN_BRACE) {
            // anonymous struct
            return DeclarationSpecifier.StructDef(token, structBody())
        } else {
            val name = expect(IDENTIFIER).tagged()
            if (current == OPEN_BRACE) {
                // named struct
                val result = DeclarationSpecifier.StructDef(name, structBody())
                declarationState = DeclarationState.NO_DECLARATOR_REQUIRED
                return result
            } else {
                return DeclarationSpecifier.StructRef(name)
            }
        }
    }

    private fun structBody() = braced { list1Until(CLOSE_BRACE) { structDeclaration() } }

    fun structDeclaration(): StructDeclaration {
        val specifiers = declarationSpecifiers1()
        val declarators = commaSeparatedList1 { namedDeclarator() }
        expect(SEMICOLON)
        return StructDeclaration(specifiers, declarators)
    }

    fun initDeclarator(): NamedDeclarator {
        return initDeclarator(namedDeclarator())
    }

    private fun initDeclarator(namedDeclarator: NamedDeclarator): NamedDeclarator {
        if (current == EQ) {
            next()
            with(namedDeclarator) {
                return NamedDeclarator(name, Declarator.Initialized(declarator, initializer()))
            }
        } else {
            return namedDeclarator
        }
    }

    private fun initializer(): Initializer {
        return if (current == OPEN_BRACE) {
            InitializerList(token, braced { trailingCommaSeparatedList1(CLOSE_BRACE) { initializer() } })
        } else {
            ExpressionInitializer(assignmentExpression())
        }
    }

    fun namedDeclarator(): NamedDeclarator {
        with(namedDeclaratorBackwards()) {
            return NamedDeclarator(first, second.reverse())
        }
    }

    fun namedDeclaratorBackwards(): Pair<Token, Declarator> {
        if (current == STAR) {
            next()
            val qualifiers = typeQualifierList()
            return namedDeclaratorBackwards().map { Declarator.Pointer(it, qualifiers) }
        }
        var temp: Pair<Token, Declarator> = when (current) {
            OPEN_PAREN -> parenthesized { namedDeclaratorBackwards() }
            IDENTIFIER -> Pair(consume(token), Declarator.Identity)
            else -> illegalStartOf("declarator")
        }
        while (true) {
            when (current) {
                OPEN_BRACKET -> temp = temp.map { Declarator.Array(it, declaratorArray()) }
                OPEN_PAREN -> temp = temp.map { Declarator.Function(it, declaratorFunction()) }
                else -> return temp
            }
        }
    }

    private fun typeQualifierList(): List<Token> {
        return collectWhile { it == CONST }
    }

    private fun declaratorArray(): Expression? {
        expect(OPEN_BRACKET)
        return unless(CLOSE_BRACKET) { expression() }
    }

    private fun declaratorFunction(): List<FunctionParameter> {
        symbolTable.openScope()
        val parameterList = parenthesized {
            if (current == VOID && lookahead.kind == CLOSE_PAREN) {
                next()
            }
            commaSeparatedList0(CLOSE_PAREN) {
                val specifiers = declarationSpecifiers1()
                val declarator = parameterDeclarator()
                if (declarator.name.wasProvided()) {
                    declare(declarator.name, isTypedefName = false)
                }
                FunctionParameter(specifiers, declarator)
            }
        }
        // close the scope unless it's a function definition
        if (current != OPEN_BRACE) {
            symbolTable.closeScope()
        }
        return parameterList
    }

    private inline fun Pair<Token, Declarator>.map(f: (Declarator) -> Declarator): Pair<Token, Declarator> {
        return Pair(first, f(second))
    }

    fun abstractDeclarator(): Declarator {
        return abstractDeclaratorBackwards().reverse()
    }

    fun abstractDeclaratorBackwards(): Declarator {
        if (current == STAR) {
            next()
            val qualifiers = typeQualifierList()
            return Declarator.Pointer(abstractDeclaratorBackwards(), qualifiers)
        }
        var temp: Declarator = when (current) {
            OPEN_PAREN -> parenthesized { abstractDeclaratorBackwards() }
            IDENTIFIER -> token.error("identifier in abstract declarator")
            else -> Declarator.Identity
        }
        while (true) {
            when (current) {
                OPEN_BRACKET -> temp = Declarator.Array(temp, declaratorArray())
                OPEN_PAREN -> temp = Declarator.Function(temp, declaratorFunction())
                else -> return temp
            }
        }
    }

    fun parameterDeclarator(): NamedDeclarator {
        with(parameterDeclaratorBackwards()) {
            return NamedDeclarator(first, second.reverse())
        }
    }

    fun parameterDeclaratorBackwards(): Pair<Token, Declarator> {
        if (current == STAR) {
            next()
            val qualifiers = typeQualifierList()
            return parameterDeclaratorBackwards().map { Declarator.Pointer(it, qualifiers) }
        }
        var temp: Pair<Token, Declarator> = when (current) {
            OPEN_PAREN -> {
                if (lookahead.isDeclarationSpecifier()) {
                    Pair(token, Declarator.Function(Declarator.Identity, declaratorFunction()))
                } else {
                    parenthesized { parameterDeclaratorBackwards() }
                }
            }
            IDENTIFIER -> Pair(consume(token), Declarator.Identity)
            else -> Pair(token, Declarator.Identity)
        }
        while (true) {
            when (current) {
                OPEN_BRACKET -> temp = temp.map { Declarator.Array(it, declaratorArray()) }
                OPEN_PAREN -> temp = temp.map { Declarator.Function(it, declaratorFunction()) }
                else -> return temp
            }
        }
    }

    // 3.6 Statements
    fun statement(): Statement {
        return when (current) {
            OPEN_BRACE -> block()
            IF -> selectionStatement()
            SWITCH -> switchStatement()
            CASE -> caseStatement()
            DEFAULT -> defaultStatement()
            WHILE -> whileStatement()
            DO -> doStatement()
            FOR -> forStatement()
            GOTO -> gotoStatement()
            CONTINUE -> continueStatement()
            BREAK -> breakStatement()
            RETURN -> returnStatement()
            ASSERT -> assertStatement()

            TYPEDEF,
            EXTERN,
            STATIC,
            AUTO,
            REGISTER,

            VOID,
            CHAR,
            SHORT,
            INT,
            LONG,
            FLOAT,
            DOUBLE,
            SIGNED,
            UNSIGNED,
            STRUCT,
            UNION,
            ENUM,

            CONST,
            VOLATILE -> declaration()

            IDENTIFIER -> {
                if (lookahead.kind == COLON) {
                    labeledStatement()
                } else if (token.isTypedefName()) {
                    declaration()
                } else {
                    expressionStatement()
                }
            }

            else -> expressionStatement()
        }
    }

    fun labeledStatement(): Statement {
        val label = expect(IDENTIFIER)
        expect(COLON)
        return LabeledStatement(label, statement())
    }

    fun gotoStatement(): Statement {
        val goto = expect(GOTO)
        val label = expect(IDENTIFIER)
        expect(SEMICOLON)
        return Goto(goto, label)
    }

    fun continueStatement(): Statement {
        val continu3 = expect(CONTINUE)
        expect(SEMICOLON)
        return Continue(continu3)
    }

    fun breakStatement(): Statement {
        val br3ak = expect(BREAK)
        expect(SEMICOLON)
        return Break(br3ak)
    }

    fun returnStatement(): Statement {
        val r3turn = expect(RETURN)
        val result = expression()
        expect(SEMICOLON)
        return Return(r3turn, result)
    }

    fun assertStatement(): Statement {
        val ass3rt = expect(ASSERT)
        val assertion = expression()
        expect(SEMICOLON)
        return Assert(ass3rt, assertion)
    }

    fun selectionStatement(): Statement {
        val iF = expect(IF)
        val condition = parenthesized { expression() }
        val th3n = statement()
        if (current != ELSE) return IfThen(iF, condition, th3n)

        next()
        val e1se = statement()
        return IfThenElse(iF, condition, th3n, e1se)
    }

    fun switchStatement(): Statement {
        val switch = expect(SWITCH)
        val control = parenthesized { expression() }
        val body = statement()
        return Switch(switch, control, body)
    }

    fun caseStatement(): Statement {
        val case = expect(CASE)
        val choice = expression()
        expect(COLON)
        val body = statement()
        return Case(case, choice, body)
    }

    fun defaultStatement(): Statement {
        val default = expect(DEFAULT)
        expect(COLON)
        val body = statement()
        return Default(default, body)
    }

    fun whileStatement(): Statement {
        val whi1e = expect(WHILE)
        val condition = parenthesized { expression() }
        val body = statement()
        return While(whi1e, condition, body)
    }

    fun doStatement(): Statement {
        val d0 = expect(DO)
        val body = statement()
        expect(WHILE)
        val condition = parenthesized { expression() }
        expect(SEMICOLON)
        return Do(d0, body, condition)
    }

    fun forStatement(): Statement {
        val f0r = expect(FOR)
        expect(OPEN_PAREN)
        if (!declarationSpecifiers0().isEmpty()) {
            token.error("loop variables must be declared above the loop")
        }
        val init = unless(SEMICOLON) { expression() }
        val check = unless(SEMICOLON) { expression() }
        val update = unless(CLOSE_PAREN) { expression() }
        val body = statement()
        return For(f0r, init, check, update, body)
    }

    fun expressionStatement(): Statement {
        val expression = expression()
        expect(SEMICOLON)
        return ExpressionStatement(expression)
    }

    // 3.6.2 Compound statement, or block
    fun block(): Block {
        return symbolTable.scoped {
            Block(token, braced { list0Until(CLOSE_BRACE) { statement() } })
        }
    }

    // 3.7 External definitions
    fun translationUnit(): TranslationUnit {
        return TranslationUnit(list1Until(EOF) { externalDeclaration() })
    }

    fun externalDeclaration(): ASTNode {
        val specifiers = declarationSpecifiers1()
        val isTypedef = specifiers.list.any { it.kind() == TYPEDEF }
        if (declarationState == DeclarationState.NO_DECLARATOR_REQUIRED && !isTypedef) {
            if (current == SEMICOLON) {
                next()
                return Declaration(specifiers, Collections.emptyList())
            }
            if (token.isDeclarationSpecifier()) token.error("Did you forget to terminate the above type with a semicolon?")
        }
        val firstNamedDeclarator = namedDeclarator()
        if (firstNamedDeclarator.declarator is Declarator.Function && current == OPEN_BRACE) {
            next()
            val body = list0Until(CLOSE_BRACE) { statement() }
            val closingBrace = expect(CLOSE_BRACE)
            symbolTable.closeScope()
            return FunctionDefinition(specifiers, firstNamedDeclarator, body, closingBrace)
        } else {
            declare(firstNamedDeclarator.name, isTypedef)
            val declarators = commaSeparatedList1(initDeclarator(firstNamedDeclarator)) {
                initDeclarator().apply { declare(name, isTypedef) }
            }
            expect(SEMICOLON)
            return Declaration(specifiers, declarators)
        }
    }
}
