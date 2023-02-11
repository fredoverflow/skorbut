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
            return when (beforePrevious.kind) {
                TokenKind.DOT, TokenKind.HYPHEN_MORE -> suffixesIn(allMemberNames.asSequence())

                else -> suffixesIn(symbolTable.names())
            }
        }
    }
    return emptyList()
}

private fun Parser.suffixesIn(names: Sequence<String>): List<String> {
    val prefix = previous.text
    val prefixLength = prefix.length

    return names
        .filter { it.length > prefixLength && it.startsWith(prefix) }
        .map { it.substring(prefixLength) }
        .toList()
}

private fun longestCommonPrefix(strings: List<String>): String {
    val shortestString = strings.minByOrNull(String::length) ?: ""
    shortestString.forEachIndexed { index, ch ->
        if (!strings.all { it[index] == ch }) {
            return shortestString.substring(0, index)
        }
    }
    return shortestString
}
