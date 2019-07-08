package syntax.parser

import common.Diagnostic
import syntax.lexer.Lexer
import syntax.lexer.TokenKind

fun completeIdentifier(beforeCursor: String): List<String> {
    val lexer = Lexer(beforeCursor)
    val parser = Parser(lexer)
    val suffixes = parser.fittingSuffixes(beforeCursor)
    return if (suffixes.isEmpty()) {
        emptyList()
    } else {
        val lcp = longestCommonPrefixOf(suffixes)
        if (!lcp.isEmpty()) {
            listOf(lcp)
        } else {
            suffixes
        }
    }
}

private fun Parser.fittingSuffixes(beforeCursor: String): List<String> {
    try {
        translationUnit()
    } catch (diagnostic: Diagnostic) {
        if (diagnostic.position != beforeCursor.length) throw diagnostic

        if (previous.kind == TokenKind.IDENTIFIER && previous.end == beforeCursor.length) {
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

private fun longestCommonPrefixOf(identifiers: List<String>): String {
    return identifiers.fold(identifiers[0], ::longestCommonPrefix)
}

// TODO Is there an elegant AND EFFICIENT functional solution?
private fun longestCommonPrefix(a: String, b: String): String {
    val n = Math.min(a.length, b.length)
    var i = 0
    while ((i < n) && (a[i] == b[i])) {
        ++i
    }
    return a.substring(0, i)
}
