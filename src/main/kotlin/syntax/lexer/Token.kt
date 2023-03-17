package syntax.lexer

import common.Diagnostic
import syntax.lexer.TokenKind.IDENTIFIER
import syntax.lexer.TokenKind.STRING_LITERAL

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

    //  0123 456  index
    // "ABC\nXYZ"
    // 0123456789 origin
    fun stringErrorAt(index: Int, message: String): Nothing {
        assert(kind == STRING_LITERAL)
        var origin = 1
        repeat(index) {
            if (source[origin] == '\\') ++origin
            ++origin
        }
        throw Diagnostic(start + origin, message)
    }

    override fun toString(): String = source
}

fun fakeIdentifier(name: String) = Token(IDENTIFIER, Int.MIN_VALUE, name, name)

val missingIdentifier = fakeIdentifier("")
val hiddenIdentifier = fakeIdentifier("_")
