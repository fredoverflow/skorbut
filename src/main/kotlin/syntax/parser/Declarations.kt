package syntax.parser

import semantic.enumStructUnion
import semantic.storageClasses
import semantic.typeSpecifierIdentifier
import semantic.typeSpecifiers
import semantic.types.FunctionType
import semantic.types.MarkerIsTypedefName
import semantic.types.MarkerNotTypedefName
import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.lexer.TokenKind.*
import syntax.lexer.TokenKindSet
import syntax.tree.*

fun Parser.declare(namedDeclarator: NamedDeclarator, isTypedefName: Boolean) {
    val pseudoType = when {
        isTypedefName -> MarkerIsTypedefName

        namedDeclarator.declarator.leaf() is Declarator.Function -> FunctionType.declarationMarker()

        else -> MarkerNotTypedefName
    }
    symbolTable.declare(namedDeclarator.name, pseudoType, 0)
}

fun Parser.isTypedefName(token: Token): Boolean {
    val symbol = symbolTable.lookup(token)
    if (symbol == null) {
        val taggedSymbol = symbolTable.lookup(token.tagged())
        if (taggedSymbol != null) {
            token.error("Did you forget struct/enum/union before ${token.text}?")
        }
    }
    return symbol?.type === MarkerIsTypedefName
}

fun Parser.isDeclarationSpecifier(token: Token): Boolean = when (token.kind) {
    TYPEDEF, EXTERN, STATIC, AUTO, REGISTER,
    CONST, VOLATILE,
    VOID, CHAR, SHORT, INT, LONG, SIGNED, UNSIGNED, FLOAT, DOUBLE,
    ENUM, STRUCT, UNION -> true

    IDENTIFIER -> isTypedefName(token)

    else -> false
}

fun Parser.declaration(): Statement {
    val specifiers = declarationSpecifiers1declareDefTagName()
    val isTypedef = specifiers.storageClass == TYPEDEF
    val declarators = commaSeparatedList0(SEMICOLON) {
        initDeclarator().apply { declare(this, isTypedef) }
    }
    if (current == OPENING_BRACE) {
        val inner = declarators.last().name
        val outer = symbolTable.currentFunction()!!.name
        inner.error("cannot define function $inner inside function $outer", outer)
    }
    return Declaration(specifiers, declarators).semicolon()
}

fun Parser.declarationSpecifiers1declareDefTagName(): DeclarationSpecifiers {
    val specifiers = declarationSpecifiers1()
    specifiers.defTagName()?.let { name ->
        symbolTable.declare(name, MarkerNotTypedefName, 0)
    }
    return specifiers
}

fun Parser.declarationSpecifiers1(): DeclarationSpecifiers {
    val specifiers = declarationSpecifiers0()
    if (specifiers.list.isEmpty()) {
        val symbol = symbolTable.lookup(token)
        if (symbol != null) {
            val taggedSymbol = symbolTable.lookup(token.tagged())
            if (taggedSymbol != null) {
                token.error("Did you forget struct/enum/union before ${token.text}?")
            }
        }
        illegalStartOf("declaration")
    }
    return specifiers
}

fun Parser.declarationSpecifiers0(): DeclarationSpecifiers {
    var storageClass: TokenKind = VOID
    var qualifiers = TokenKindSet.EMPTY
    var typeTokens = TokenKindSet.EMPTY
    val list = ArrayList<DeclarationSpecifier>()

    loop@ while (true) {
        when (current) {
            TYPEDEF, EXTERN, STATIC, AUTO, REGISTER ->
                if (storageClass == VOID) {
                    storageClass = current
                } else {
                    val previous = list.first { storageClasses.contains(it.kind()) }
                    token.error("multiple storage class specifiers", previous.root())
                }

            CONST, VOLATILE ->
                if (qualifiers.contains(current)) {
                    val previous = list.first { it.kind() == current }
                    token.error("duplicate type qualifier", previous.root())
                } else {
                    qualifiers += current
                }

            IDENTIFIER ->
                if (typeTokens.isEmpty() && isTypedefName(token)) {
                    typeTokens = typeSpecifierIdentifier
                } else {
                    break@loop
                }

            VOID, CHAR, SHORT, INT, LONG, SIGNED, UNSIGNED, FLOAT, DOUBLE,
            ENUM, STRUCT, UNION ->
                if (!typeTokens.isEmpty() && enumStructUnion.contains(typeTokens.first())) {
                    val previous = list.first { enumStructUnion.contains(it.kind()) }
                    token.error(
                        "Did you forget to terminate the previous ${previous.kind()} with a semicolon?",
                        previous.root()
                    )
                } else if (typeTokens.contains(current)) {
                    val previous = list.first { it.kind() == current }
                    token.error("duplicate type specifier", previous.root())
                } else {
                    typeTokens += current
                    if (typeTokens !in typeSpecifiers) token.error("illegal combination of type specifiers: $typeTokens")
                }

            else -> break@loop
        }
        list.add(declarationSpecifier())
    }
    return DeclarationSpecifiers(list, storageClass, qualifiers, typeTokens)
}

fun Parser.declarationSpecifier(): DeclarationSpecifier = when (current) {
    ENUM -> enumSpecifier()

    STRUCT -> structSpecifier()

    UNION -> notImplementedYet("unions")

    else -> DeclarationSpecifier.Primitive(accept())
}

fun Parser.enumSpecifier(): DeclarationSpecifier {
    return if (next() == OPENING_BRACE) {
        // anonymous enum
        DeclarationSpecifier.EnumDef(token, enumBody())
    } else {
        val name = expect(IDENTIFIER).tagged()
        if (current == OPENING_BRACE) {
            // named enum
            DeclarationSpecifier.EnumDef(name, enumBody())
        } else {
            DeclarationSpecifier.EnumRef(name)
        }
    }
}

fun Parser.enumBody(): List<Enumerator> {
    return braced {
        commaSeparatedList1 {
            Enumerator(expect(IDENTIFIER), optional(EQUAL, ::assignmentExpression)).apply {
                symbolTable.declare(name, MarkerNotTypedefName, 0)
            }
        }
    }
}

fun Parser.structSpecifier(): DeclarationSpecifier {
    return if (next() == OPENING_BRACE) {
        // anonymous struct
        DeclarationSpecifier.StructDef(token, structBody())
    } else {
        val name = expect(IDENTIFIER).tagged()
        if (current == OPENING_BRACE) {
            // named struct
            DeclarationSpecifier.StructDef(name, structBody())
        } else {
            DeclarationSpecifier.StructRef(name)
        }
    }
}

fun Parser.structBody(): List<StructDeclaration> {
    return braced {
        list1Until(CLOSING_BRACE) {
            StructDeclaration(declarationSpecifiers1(), commaSeparatedList1 {
                namedDeclarator().apply { allMemberNames.add(name.text) }
            }).semicolon()
        }
    }
}

fun Parser.initDeclarator(): NamedDeclarator {
    return initDeclarator(namedDeclarator())
}

fun Parser.initDeclarator(namedDeclarator: NamedDeclarator): NamedDeclarator {
    return if (current == EQUAL) {
        next()
        with(namedDeclarator) {
            NamedDeclarator(name, Declarator.Initialized(declarator, initializer()))
        }
    } else {
        namedDeclarator
    }
}

fun Parser.initializer(): Initializer {
    return if (current == OPENING_BRACE) {
        InitializerList(token, braced { trailingCommaSeparatedList1(CLOSING_BRACE, ::initializer) })
    } else {
        ExpressionInitializer(assignmentExpression())
    }
}

fun Parser.namedDeclarator(): NamedDeclarator {
    if (current == ASTERISK) {
        next()
        val qualifiers = typeQualifierList()
        return namedDeclarator().map { Declarator.Pointer(it, qualifiers) }
    }
    var temp: NamedDeclarator = when (current) {
        OPENING_PAREN -> parenthesized(::namedDeclarator)

        IDENTIFIER -> NamedDeclarator(accept(), Declarator.Identity)

        else -> illegalStartOf("declarator")
    }
    while (true) {
        temp = when (current) {
            OPENING_BRACKET -> temp.map { Declarator.Array(it, declaratorArray()) }

            OPENING_PAREN -> temp.map { Declarator.Function(it, declaratorFunction()) }

            else -> return temp
        }
    }
}

fun Parser.typeQualifierList(): List<Token> {
    return collectWhile { current == CONST }
}

fun Parser.declaratorArray(): Expression? {
    expect(OPENING_BRACKET)
    return ::expression optionalBefore CLOSING_BRACKET
}

fun Parser.declaratorFunction(): List<FunctionParameter> {
    return symbolTable.scoped {
        parenthesized {
            if (current == VOID && lookahead.kind == CLOSING_PAREN) {
                next()
            }
            commaSeparatedList0(CLOSING_PAREN) {
                val specifiers = declarationSpecifiers1declareDefTagName()
                val declarator = parameterDeclarator()
                if (declarator.name.wasProvided()) {
                    declare(declarator, isTypedefName = false)
                }
                FunctionParameter(specifiers, declarator)
            }
        }
    }
}

fun Parser.abstractDeclarator(): Declarator {
    if (current == ASTERISK) {
        next()
        val qualifiers = typeQualifierList()
        return Declarator.Pointer(abstractDeclarator(), qualifiers)
    }
    var temp: Declarator = when (current) {
        OPENING_PAREN -> parenthesized(::abstractDeclarator)

        IDENTIFIER -> token.error("identifier in abstract declarator")

        else -> Declarator.Identity
    }
    while (true) {
        temp = when (current) {
            OPENING_BRACKET -> Declarator.Array(temp, declaratorArray())

            OPENING_PAREN -> Declarator.Function(temp, declaratorFunction())

            else -> return temp
        }
    }
}

fun Parser.parameterDeclarator(): NamedDeclarator {
    if (current == ASTERISK) {
        next()
        val qualifiers = typeQualifierList()
        return parameterDeclarator().map { Declarator.Pointer(it, qualifiers) }
    }
    var temp: NamedDeclarator = when (current) {
        OPENING_PAREN -> {
            if (isDeclarationSpecifier(lookahead)) {
                NamedDeclarator(token, Declarator.Function(Declarator.Identity, declaratorFunction()))
            } else {
                parenthesized(::parameterDeclarator)
            }
        }

        IDENTIFIER -> NamedDeclarator(accept(), Declarator.Identity)

        else -> NamedDeclarator(token, Declarator.Identity)
    }
    while (true) {
        temp = when (current) {
            OPENING_BRACKET -> temp.map { Declarator.Array(it, declaratorArray()) }

            OPENING_PAREN -> temp.map { Declarator.Function(it, declaratorFunction()) }

            else -> return temp
        }
    }
}
