package syntax.parser

import semantic.types.MarkerIsTypedefName
import semantic.types.MarkerNotTypedefName
import syntax.lexer.*
import syntax.tree.*

enum class DeclarationState {
    OPEN, PRIMITIVE, USER_DEFINED, NO_DECLARATOR_REQUIRED
}

fun Parser.declare(name: Token, isTypedefName: Boolean) {
    symbolTable.declare(name, if (isTypedefName) MarkerIsTypedefName else MarkerNotTypedefName, 0)
}

fun Parser.isTypedefName(token: Token): Boolean {
    return symbolTable.lookup(token)?.type === MarkerIsTypedefName
}

fun Parser.isDeclarationSpecifier(token: Token): Boolean {
    declarationState = DeclarationState.OPEN
    return isAcceptableDeclarationSpecifier(token)
}

fun Parser.isAcceptableDeclarationSpecifier(token: Token): Boolean = when (token.kind) {
    TYPEDEF, EXTERN, STATIC, AUTO, REGISTER -> true

    VOID, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, SIGNED, UNSIGNED ->
        enterOrRemainInDeclarationState(DeclarationState.PRIMITIVE)

    STRUCT, UNION, ENUM -> enterDeclarationState(DeclarationState.USER_DEFINED)

    IDENTIFIER -> if (isTypedefName(token)) enterDeclarationState(DeclarationState.USER_DEFINED) else false

    CONST, VOLATILE -> true

    else -> false
}

private fun Parser.enterOrRemainInDeclarationState(desiredState: DeclarationState): Boolean {
    if (declarationState == DeclarationState.OPEN) {
        declarationState = desiredState
        return true
    }
    return declarationState == desiredState
}

private fun Parser.enterDeclarationState(desiredState: DeclarationState): Boolean {
    if (declarationState == DeclarationState.OPEN) {
        declarationState = desiredState
        return true
    }
    return false
}

fun Parser.declaration(): Statement {
    val specifiers = declarationSpecifiers1()
    val isTypedef = specifiers.list.any { it.kind() == TYPEDEF }
    val declarators = commaSeparatedList0(SEMICOLON) {
        initDeclarator().apply { declare(name, isTypedef) }
    }
    expect(SEMICOLON)
    return Declaration(specifiers, declarators)
}

fun Parser.declarationSpecifiers1(): DeclarationSpecifiers {
    val specifiers = declarationSpecifiers0()
    if (specifiers.isEmpty()) illegalStartOf("declaration")
    return DeclarationSpecifiers(specifiers)
}

fun Parser.declarationSpecifiers0(): List<DeclarationSpecifier> {
    declarationState = DeclarationState.OPEN
    return list0While({ isAcceptableDeclarationSpecifier(token) }) { declarationSpecifier() }
}

fun Parser.declarationSpecifier(): DeclarationSpecifier {
    return when (current) {
        ENUM -> enumSpecifier()
        STRUCT -> structSpecifier()
        UNION -> notImplementedYet("unions")
        else -> consume(DeclarationSpecifier.Primitive(token))
    }
}

fun Parser.enumSpecifier(): DeclarationSpecifier {
    declarationState = DeclarationState.NO_DECLARATOR_REQUIRED
    if (next() == OPENING_BRACE) {
        // anonymous enum
        return DeclarationSpecifier.EnumDef(token, enumBody())
    } else {
        val name = expect(IDENTIFIER).tagged()
        if (current == OPENING_BRACE) {
            // named enum
            return DeclarationSpecifier.EnumDef(name, enumBody())
        } else {
            return DeclarationSpecifier.EnumRef(name)
        }
    }
}

fun Parser.enumBody(): List<Enumerator> {
    return braced { commaSeparatedList1 { enumerator() } }
}

fun Parser.enumerator(): Enumerator {
    val name = expect(IDENTIFIER)
    val init = if (current == EQUAL) {
        next()
        assignmentExpression()
    } else null
    return Enumerator(name, init)
}

fun Parser.structSpecifier(): DeclarationSpecifier {
    if (next() == OPENING_BRACE) {
        // anonymous struct
        return DeclarationSpecifier.StructDef(token, structBody())
    } else {
        val name = expect(IDENTIFIER).tagged()
        if (current == OPENING_BRACE) {
            // named struct
            val result = DeclarationSpecifier.StructDef(name, structBody())
            declarationState = DeclarationState.NO_DECLARATOR_REQUIRED
            return result
        } else {
            return DeclarationSpecifier.StructRef(name)
        }
    }
}

fun Parser.structBody(): List<StructDeclaration> {
    return braced { list1Until(CLOSING_BRACE) { structDeclaration() } }
}

fun Parser.structDeclaration(): StructDeclaration {
    val specifiers = declarationSpecifiers1()
    val declarators = commaSeparatedList1 { namedDeclarator() }
    expect(SEMICOLON)
    return StructDeclaration(specifiers, declarators)
}

fun Parser.initDeclarator(): NamedDeclarator {
    return initDeclarator(namedDeclarator())
}

fun Parser.initDeclarator(namedDeclarator: NamedDeclarator): NamedDeclarator {
    if (current == EQUAL) {
        next()
        with(namedDeclarator) {
            return NamedDeclarator(name, Declarator.Initialized(declarator, initializer()))
        }
    } else {
        return namedDeclarator
    }
}

fun Parser.initializer(): Initializer {
    return if (current == OPENING_BRACE) {
        InitializerList(token, braced { trailingCommaSeparatedList1(CLOSING_BRACE) { initializer() } })
    } else {
        ExpressionInitializer(assignmentExpression())
    }
}

fun Parser.namedDeclarator(): NamedDeclarator {
    with(namedDeclaratorBackwards()) {
        return NamedDeclarator(first, second.reverse())
    }
}

fun Parser.namedDeclaratorBackwards(): Pair<Token, Declarator> {
    if (current == ASTERISK) {
        next()
        val qualifiers = typeQualifierList()
        return namedDeclaratorBackwards().map { Declarator.Pointer(it, qualifiers) }
    }
    var temp: Pair<Token, Declarator> = when (current) {
        OPENING_PAREN -> parenthesized { namedDeclaratorBackwards() }
        IDENTIFIER -> Pair(consume(token), Declarator.Identity)
        else -> illegalStartOf("declarator")
    }
    while (true) {
        when (current) {
            OPENING_BRACKET -> temp = temp.map { Declarator.Array(it, declaratorArray()) }
            OPENING_PAREN -> temp = temp.map { Declarator.Function(it, declaratorFunction()) }
            else -> return temp
        }
    }
}

fun Parser.typeQualifierList(): List<Token> {
    return collectWhile { it == CONST }
}

fun Parser.declaratorArray(): Expression? {
    expect(OPENING_BRACKET)
    return unless(CLOSING_BRACKET) { expression() }
}

fun Parser.declaratorFunction(): List<FunctionParameter> {
    symbolTable.openScope()
    val parameterList = parenthesized {
        if (current == VOID && lookahead.kind == CLOSING_PAREN) {
            next()
        }
        commaSeparatedList0(CLOSING_PAREN) {
            val specifiers = declarationSpecifiers1()
            val declarator = parameterDeclarator()
            if (declarator.name.wasProvided()) {
                declare(declarator.name, isTypedefName = false)
            }
            FunctionParameter(specifiers, declarator)
        }
    }
    // close the scope unless it's a function definition
    if (current != OPENING_BRACE) {
        symbolTable.closeScope()
    }
    return parameterList
}

private inline fun Pair<Token, Declarator>.map(f: (Declarator) -> Declarator): Pair<Token, Declarator> {
    return Pair(first, f(second))
}

fun Parser.abstractDeclarator(): Declarator {
    return abstractDeclaratorBackwards().reverse()
}

fun Parser.abstractDeclaratorBackwards(): Declarator {
    if (current == ASTERISK) {
        next()
        val qualifiers = typeQualifierList()
        return Declarator.Pointer(abstractDeclaratorBackwards(), qualifiers)
    }
    var temp: Declarator = when (current) {
        OPENING_PAREN -> parenthesized { abstractDeclaratorBackwards() }
        IDENTIFIER -> token.error("identifier in abstract declarator")
        else -> Declarator.Identity
    }
    while (true) {
        when (current) {
            OPENING_BRACKET -> temp = Declarator.Array(temp, declaratorArray())
            OPENING_PAREN -> temp = Declarator.Function(temp, declaratorFunction())
            else -> return temp
        }
    }
}

fun Parser.parameterDeclarator(): NamedDeclarator {
    with(parameterDeclaratorBackwards()) {
        return NamedDeclarator(first, second.reverse())
    }
}

fun Parser.parameterDeclaratorBackwards(): Pair<Token, Declarator> {
    if (current == ASTERISK) {
        next()
        val qualifiers = typeQualifierList()
        return parameterDeclaratorBackwards().map { Declarator.Pointer(it, qualifiers) }
    }
    var temp: Pair<Token, Declarator> = when (current) {
        OPENING_PAREN -> {
            if (isDeclarationSpecifier(lookahead)) {
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
            OPENING_BRACKET -> temp = temp.map { Declarator.Array(it, declaratorArray()) }
            OPENING_PAREN -> temp = temp.map { Declarator.Function(it, declaratorFunction()) }
            else -> return temp
        }
    }
}
