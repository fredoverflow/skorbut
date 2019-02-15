package syntax.lexer

import common.Diagnostic

abstract class LexerBase(private val input: String) {
    protected var start: Int = -1
    protected var index: Int = -1
    protected var current: Char = next()

    protected fun next(): Char = nextOr('\u007f')

    protected fun nextOr(end: Char): Char {
        ++index
        current = if (index < input.length) input[index] else end
        return current
    }

    protected fun lexeme(): String {
        return input.substring(start, index)
    }

    protected fun token(kind: Byte): Token {
        return token(kind, lexeme())
    }

    protected fun token(kind: Byte, text: String): Token {
        return token(kind, text, text)
    }

    protected fun token(kind: Byte, sourceText: String, executionText: String): Token {
        return Token(kind, start, sourceText, executionText)
    }

    protected fun pooled(kind: Byte): Token {
        return token(kind, kind.show())
    }

    protected fun nextPooled(kind: Byte): Token {
        next()
        return pooled(kind)
    }

    protected fun error(description: String): Nothing {
        throw Diagnostic(index, description)
    }
}
