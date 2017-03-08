package ui

import syntax.*
import text.indexOfOrLength
import java.util.*

fun indent(source: String): String {
    val indentations = ArrayList<Int>()
    var indentation = 0

    val lexer = Lexer(source)
    var token = lexer.nextToken()
    var newline = source.indexOfOrLength('\n', 0)
    while (token.kind != EOF) {
        // scan for leading closing braces
        while (token.start < newline && token.kind == CLOSE_BRACE) {
            --indentation
            token = lexer.nextToken()
        }
        indentations.add(indentation)
        // scan for remaining braces
        while (token.start < newline) {
            when (token.kind) {
                OPEN_BRACE -> ++indentation
                CLOSE_BRACE -> --indentation
            }
            token = lexer.nextToken()
        }
        newline = source.indexOfOrLength('\n', newline + 1)
    }
    val baseline = indentations.min() ?: 0
    val linesWithIndentations = source.lineSequence().zip(indentations.asSequence() + sequenceOf(indentation))
    val properlyIndentedLines = linesWithIndentations.map { "    ".repeat(it.second - baseline) + it.first.trim() }
    return properlyIndentedLines.joinToString("\n")
}
