package syntax.parser

import semantic.types.FunctionType
import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.translationUnit(): TranslationUnit {
    return TranslationUnit(list1Until(END_OF_INPUT, ::externalDeclaration))
}

fun Parser.externalDeclaration(): Node {
    val specifiers = declarationSpecifiers1declareDefTagName()
    if (current == SEMICOLON && specifiers.isDeclaratorOptional()) {
        return Declaration(specifiers, emptyList()).semicolon()
    }
    val firstNamedDeclarator = namedDeclarator()
    return if (firstNamedDeclarator.declarator.leaf() is Declarator.Function && current == OPENING_BRACE) {
        symbolTable.declare(firstNamedDeclarator.name, FunctionType.DEFINITION_MARKER, 0)
        symbolTable.rescoped {
            braced {
                FunctionDefinition(specifiers, firstNamedDeclarator, list0Until(CLOSING_BRACE, ::statement), token)
            }
        }
    } else {
        val isTypedefName = specifiers.storageClass == TYPEDEF
        declare(firstNamedDeclarator, isTypedefName)
        val declarators = commaSeparatedList1(initDeclarator(firstNamedDeclarator)) {
            initDeclarator().apply { declare(this, isTypedefName) }
        }
        Declaration(specifiers, declarators).semicolon()
    }
}
