package syntax.parser

import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.translationUnit(): TranslationUnit {
    return TranslationUnit(list1Until(END_OF_INPUT, ::externalDeclaration))
}

fun Parser.externalDeclaration(): Node {
    val specifiers = declarationSpecifiers1()
    if (specifiers.isDeclaratorOptional()) {
        if (current == SEMICOLON) {
            return Declaration(specifiers, emptyList()).semicolon()
        }
        if (isDeclarationSpecifier(token)) token.error("Did you forget to terminate the above type with a semicolon?")
    }
    val firstNamedDeclarator = namedDeclarator()
    return if (firstNamedDeclarator.declarator is Declarator.Function && current == OPENING_BRACE) {
        symbolTable.rescoped {
            braced {
                FunctionDefinition(specifiers, firstNamedDeclarator, list0Until(CLOSING_BRACE, ::statement), token)
            }
        }
    } else {
        val isTypedefName = specifiers.storageClass == TYPEDEF
        declare(firstNamedDeclarator.name, isTypedefName)
        val declarators = commaSeparatedList1(initDeclarator(firstNamedDeclarator)) {
            initDeclarator().apply { declare(name, isTypedefName) }
        }
        Declaration(specifiers, declarators).semicolon()
    }
}

fun DeclarationSpecifiers.isDeclaratorOptional(): Boolean {
    return (storageClass != TYPEDEF) && when (typeTokens.first()) {
        ENUM -> true
        STRUCT -> list.any { it is DeclarationSpecifier.StructDef && it.name.wasProvided() }
        else -> false
    }
}
