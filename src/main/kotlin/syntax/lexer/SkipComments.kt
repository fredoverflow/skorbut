package syntax.lexer

fun Lexer.skipSingleLineComment() {
    while (next() != '\n') {
        if (current == EOF) return
    }
    next() // skip '\n'
}

fun Lexer.skipMultiLineComment() {
    next() // skip '*'
    do {
        if (current == EOF) return
    } while ((current != '*') or (next() != '/'))
    next() // skip '/'
}
