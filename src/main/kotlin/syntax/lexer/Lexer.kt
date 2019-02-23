package syntax.lexer

import common.Diagnostic
import freditor.persistent.ChampMap

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
        current = try {
            input[++index]
        } catch (eof: StringIndexOutOfBoundsException) {
            EOF
        }
        return current
    }

    fun previous(): Char {
        current = input[--index]
        return current
    }

    fun continueAfter(target: Char) {
        current = try {
            while (input[++index] != target) {
            }
            input[++index] // continue after target
        } catch (eof: StringIndexOutOfBoundsException) {
            EOF
        }
    }

    fun continueAfter(before: Char, target: Char) {
        current = try {
            input[++index] // current shall not be compared with before
            while (input[++index] != target || input[index - 1] != before) {
            }
            input[++index] // continue after target
        } catch (eof: StringIndexOutOfBoundsException) {
            EOF
        }
    }

    fun lexeme(): String {
        return input.substring(start, index)
    }

    fun token(kind: Byte): Token {
        return token(kind, lexeme())
    }

    fun token(kind: Byte, text: String): Token {
        return token(kind, text, text)
    }

    fun token(kind: Byte, sourceText: String, executionText: String): Token {
        return Token(kind, start, sourceText, executionText)
    }

    fun pooled(kind: Byte): Token {
        return token(kind, kind.show())
    }

    fun nextPooled(kind: Byte): Token {
        next()
        return pooled(kind)
    }

    fun error(description: String): Nothing {
        throw Diagnostic(index, description)
    }

    @Suppress("UNCHECKED_CAST")
    var identifiersOrKeywords = keywords as ChampMap<String, Any>
}
