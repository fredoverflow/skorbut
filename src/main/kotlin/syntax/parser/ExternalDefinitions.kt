package syntax.parser

import syntax.lexer.*
import syntax.tree.*
import java.util.Collections

fun Parser.translationUnit(): TranslationUnit {
    return TranslationUnit(list1Until(END_OF_INPUT) { externalDeclaration() })
}

fun Parser.externalDeclaration(): Node {
    val specifiers = declarationSpecifiers1()
    val isTypedef = specifiers.list.any { it.kind() == TYPEDEF }
    if (declarationState == DeclarationState.NO_DECLARATOR_REQUIRED && !isTypedef) {
        if (current == SEMICOLON) {
            next()
            return Declaration(specifiers, Collections.emptyList())
        }
        if (isDeclarationSpecifier(token)) token.error("Did you forget to terminate the above type with a semicolon?")
    }
    val firstNamedDeclarator = namedDeclarator()
    if (firstNamedDeclarator.declarator is Declarator.Function && current == OPENING_BRACE) {
        next()
        val body = list0Until(CLOSING_BRACE) { statement() }
        val closingBrace = expect(CLOSING_BRACE)
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
