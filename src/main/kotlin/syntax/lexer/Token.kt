package syntax.lexer

import common.Diagnostic
import syntax.lexer.TokenKind.IDENTIFIER

class Token(val kind: TokenKind, val start: Int, val source: String, val text: String) {
    val end: Int
        get() = start + source.length

    fun withTokenKind(replacement: TokenKind): Token = Token(replacement, start, source, text)

    fun tagged(): Token = Token(kind, start, source, "#$text".intern())

    fun wasProvided(): Boolean = kind == IDENTIFIER

    fun error(message: String): Nothing {
        throw Diagnostic(start, message)
    }

    fun error(message: String, columnDelta: Int): Nothing {
        throw Diagnostic(start, message, columnDelta = columnDelta)
    }

    fun error(before: String, after: String): Nothing {
        throw Diagnostic(start, before + after, columnDelta = -before.length)
    }

    fun error(message: String, previous: Token): Nothing {
        throw Diagnostic(start, message, previous.start)
    }

    override fun toString(): String = source
}

fun fakeIdentifier(name: String) = Token(IDENTIFIER, Int.MIN_VALUE, name, name)

val missingIdentifier = fakeIdentifier("")
