package syntax.lexer

import syntax.lexer.TokenKind.CHARACTER_CONSTANT
import syntax.lexer.TokenKind.STRING_LITERAL

fun Lexer.characterConstant(): Token {
    val executionChar = when (next()) {
        '\\' -> escapeSequence()

        in '\u0020'..'\u007e' -> current
        in '\u00a0'..'\u00ff' -> current

        else -> error("illegal character inside character constant")
    }
    if (next() != '\'') error("character constant must be closed by '")

    next()
    return token(CHARACTER_CONSTANT, lexeme(), executionChar.toString())
}

fun Lexer.escapeSequence(): Char = when (next()) {
    '\'', '\"', '?', '\\' -> current

    'a' -> '\u0007'
    'b' -> '\u0008'
    't' -> '\u0009'
    'n' -> '\u000a'
    'v' -> '\u000b'
    'f' -> '\u000c'
    'r' -> '\u000d'

    '0' -> '\u0000'

    else -> error("illegal escape character")
}

fun Lexer.stringLiteral(): Token {
    val sb = StringBuilder()
    while (true) {
        val executionChar = when (next()) {
            '\\' -> escapeSequence()

            '\"' -> {
                next()
                return token(STRING_LITERAL, lexeme(), sb.toString().intern())
            }

            in '\u0020'..'\u007e' -> current
            in '\u00a0'..'\u00ff' -> current

            else -> error("illegal character inside string literal")
        }
        sb.append(executionChar)
    }
}
