package syntax.lexer

import common.Diagnostic
import syntax.lexer.TokenKind.IDENTIFIER

class Token(val kind: TokenKind, val start: Int, val source: String, val text: String) {
    val end: Int
        get() = start + source.length

    fun withTokenKind(replacement: TokenKind): Token = Token(replacement, start, source, text)

    fun tagged(): Token = Token(kind, start, source, "#$text".intern())

    fun wasProvided(): Boolean = kind == IDENTIFIER

    fun error(description: String): Nothing = throw Diagnostic(start, description)

    override fun toString(): String = source
}

fun fakeIdentifier(name: String) = Token(IDENTIFIER, Int.MIN_VALUE, name, name)

val missingIdentifier = fakeIdentifier("")
