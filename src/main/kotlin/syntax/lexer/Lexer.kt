package syntax.lexer

import common.Diagnostic

const val EOF = '\u0000'

class Lexer(private val input: String) {
    var start: Int = -1
        private set

    var index: Int = -1
        private set

    fun startAtIndex() {
        start = index
    }

    var current: Char = next()
        private set

    fun next(): Char {
        current = if (++index < input.length) input[index] else EOF
        return current
    }

    fun previous(): Char {
        current = input[--index]
        return current
    }

    fun lexeme(): String {
        return input.substring(start, index)
    }

    fun token(kind: TokenKind): Token {
        return token(kind, lexeme())
    }

    fun token(kind: TokenKind, text: String): Token {
        return token(kind, text, text)
    }

    fun token(kind: TokenKind, source: String, text: String): Token {
        return Token(kind, start, source, text)
    }

    fun verbatim(kind: TokenKind): Token {
        return token(kind, kind.lexeme)
    }

    fun nextVerbatim(kind: TokenKind): Token {
        next()
        return verbatim(kind)
    }

    fun error(message: String): Nothing {
        throw Diagnostic(index, message)
    }

    val identifiersOrKeywords = HashMap<String, Any>(keywords)
}
