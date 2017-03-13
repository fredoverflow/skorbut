package syntax

import semantic.MutableSymbolTable
import semantic.types.MarkerIsTypedefName
import semantic.types.MarkerNotTypedefName
import java.util.Collections

const val PRECEDENCE_POSTFIX = 150
const val PRECEDENCE_PREFIX = 140
const val PRECEDENCE_COMMA = 10
const val PRECEDENCE_ZERO = 0

enum class DeclarationState {
    OPEN, PRIMITIVE, USER_DEFINED, NO_DECLARATOR_REQUIRED
}

class Parser(lexer: Lexer) : ParserBase(lexer) {

    // ===== Expressions =====

    @Suppress("NOTHING_TO_INLINE")
    inline fun expression(): Expression {
        return subexpression(outerPrecedence = PRECEDENCE_ZERO)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun assignmentExpression(): Expression {
        return subexpression(outerPrecedence = PRECEDENCE_COMMA)
    }

    fun subexpression(outerPrecedence: Int): Expression {
        val nullDenotation = nullDenotations[current] ?: illegalStartOf("expression")

        return subexpression(with(nullDenotation) { this@Parser.parse(consume(token)) }, outerPrecedence)
    }

    tailrec fun subexpression(left: Expression, outerPrecedence: Int): Expression {
        val leftDenotation = leftDenotations[current] ?: return left
        if (leftDenotation.precedence <= outerPrecedence) return left

        return subexpression(with(leftDenotation) { this@Parser.parse(left, consume(token)) }, outerPrecedence)
    }

    private val nullDenotations = ByteMap<NullDenotation>().apply {
        this[IDENTIFIER] = IdentifierDenotation
        this[DOUBLE_CONSTANT, FLOAT_CONSTANT, INTEGER_CONSTANT, CHARACTER_CONSTANT] = ConstantDenotation
        this[STRING_LITERAL] = StringLiteralDenotation
        this[OPEN_PAREN] = PossibleCastDenotation
        this[PLUS_PLUS, MINUS_MINUS, AMP, STAR, PLUS, MINUS, TILDE, BANG] = PrefixDenotation
        this[SIZEOF] = SizeofDenotation
    }

    private val leftDenotations = ByteMap<LeftDenotation>().apply {
        this[OPEN_BRACKET] = SubscriptDenotation
        this[OPEN_PAREN] = FunctionCallDenotation
        this[DOT] = DirectMemberDenotation
        this[ARROW] = IndirectMemberDenotation
        this[PLUS_PLUS, MINUS_MINUS] = PostfixCrementDenotation

        this[STAR, SLASH, PERCENT] = LeftAssociativeDenotation(130, ::Multiplicative)
        this[PLUS] = LeftAssociativeDenotation(120, ::Plus)
        this[MINUS] = LeftAssociativeDenotation(120, ::Minus)
        this[LESS_LESS, MORE_MORE] = LeftAssociativeDenotation(110, ::Shift)
        this[LESS, MORE, LESS_EQ, MORE_EQ] = LeftAssociativeDenotation(100, ::RelationalEquality)
        this[EQ_EQ, BANG_EQ] = LeftAssociativeDenotation(90, ::RelationalEquality)
        this[AMP] = LeftAssociativeDenotation(80, ::Bitwise)
        this[CARET] = LeftAssociativeDenotation(70, ::Bitwise)
        this[PIPE] = LeftAssociativeDenotation(60, ::Bitwise)
        this[AMP_AMP] = LeftAssociativeDenotation(50, ::Logical)
        this[PIPE_PIPE] = LeftAssociativeDenotation(40, ::Logical)
        this[QUESTION] = ConditionalDenotation(30)
        this[EQ] = RightAssociativeDenotation(20, ::Assignment)
        this[PLUS_EQ] = RightAssociativeDenotation(20, ::PlusAssignment)
        this[MINUS_EQ] = RightAssociativeDenotation(20, ::MinusAssignment)
        this[COMMA] = RightAssociativeDenotation(PRECEDENCE_COMMA, ::Comma)
    }

    // ===== Declarations =====

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

    // ===== Statements =====

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

    fun block(): Block {
        return symbolTable.scoped {
            Block(token, braced { list0Until(CLOSE_BRACE) { statement() } })
        }
    }

    // ===== External definitions =====

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
