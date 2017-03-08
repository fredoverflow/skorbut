package syntax

import common.Diagnostic

abstract class LexerBase(private val input: String) {
    private var start: Int = -1
    private var index: Int = -1
    protected var ch: Char = '@'
        private set

    init {
        eat()
    }

    protected fun eat(): Char {
        try {
            ch = input[++index]
        } catch (eof: StringIndexOutOfBoundsException) {
            ch = '\u007f'
        }
        return ch
    }

    private fun unsafeEat(): Char {
        ch = input[++index]
        return ch
    }

    protected fun eatCommentsAndWhitespace() {
        out@ while (true) {
            when (ch) {
                ' ', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d' -> eat()
                '/' -> when (eat()) {
                    '/' -> ignoreSingleLineComment()
                    '*' -> ignoreMultiLineComment()
                    else -> {
                        // undo last eat
                        --index
                        ch = '/'
                        break@out
                    }
                }
                else -> break@out
            }
        }
        start = index
    }

    private fun ignoreSingleLineComment() {
        try {
            while (unsafeEat() != '\n') {
            }
        } catch (eof: StringIndexOutOfBoundsException) {
        }
    }

    private fun ignoreMultiLineComment() {
        try {
            do {
                while (unsafeEat() != '*') {
                }
                while (unsafeEat() == '*') {
                }
            } while (ch != '/')
            eat()
        } catch (eof: StringIndexOutOfBoundsException) {
        }
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

    protected fun error(description: String): Nothing {
        throw Diagnostic(index, description)
    }
}
