package syntax.parser

import common.Diagnostic
import syntax.lexer.Lexer
import syntax.lexer.TokenKind

fun autocompleteIdentifier(textBeforeSelection: String): List<String> {
    val lexer = Lexer(textBeforeSelection)
    val parser = Parser(lexer)
    val suffixes = parser.fittingSuffixes(textBeforeSelection)
    val lcp = longestCommonPrefix(suffixes)
    return if (lcp.isEmpty()) {
        suffixes
    } else {
        listOf(lcp)
    }
}

private fun Parser.fittingSuffixes(textBeforeSelection: String): List<String> {
    try {
        translationUnit()
    } catch (diagnostic: Diagnostic) {
        if (diagnostic.position != textBeforeSelection.length) throw diagnostic

        if (previous.kind == TokenKind.IDENTIFIER && previous.end == textBeforeSelection.length) {
            return suffixesInSymbolTable()
        }
    }
    return emptyList()
}

private fun Parser.suffixesInSymbolTable(): List<String> {
    val suffixes = HashSet<String>()
    val prefix = previous.text
    val prefixLength = prefix.length
    symbolTable.forEach { symbol ->
        val text = symbol.name.text
        if (text.length > prefixLength && text.startsWith(prefix)) {
            suffixes.add(text.substring(prefixLength))
        }
    }
    return suffixes.toList()
}

private fun longestCommonPrefix(strings: List<String>): String {
    val shortestString = strings.minBy(String::length) ?: ""
    shortestString.forEachIndexed { index, ch ->
        if (!strings.all { command -> command[index] == ch }) {
            return shortestString.substring(0, index)
        }
    }
    return shortestString
}
