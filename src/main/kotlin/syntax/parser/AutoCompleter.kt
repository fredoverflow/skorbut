package syntax.parser

import common.Diagnostic
import syntax.lexer.Lexer
import syntax.lexer.TokenKind

fun completeIdentifier(textBeforeSelection: String): List<String> {
    val lexer = Lexer(textBeforeSelection)
    val parser = Parser(lexer)
    val suffixes = parser.fittingSuffixes(textBeforeSelection)
    return if (suffixes.isEmpty()) {
        suffixes
    } else {
        val lcp = longestCommonPrefixOf(suffixes)
        if (lcp.isEmpty()) {
            suffixes
        } else {
            listOf(lcp)
        }
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
