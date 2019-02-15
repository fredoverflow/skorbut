package syntax.lexer

fun Lexer.characterConstant(): Token {
    val executionChar = when (next()) {
        ' ', '!', '#', '$', '%', '&', '\"', '(', ')', '*', '+', ',', '-', '.', '/',
        '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
        ':', ';', '<', '=', '>', '?', '@',
        'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
        '[', ']', '^', '_', '`',
        'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
        '{', '|', '}', '~' -> current

        '\\' -> escapeSequence()

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
            ' ', '!', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            ':', ';', '<', '=', '>', '?', '@',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '[', ']', '^', '_', '`',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '{', '|', '}', '~' -> current

            '\\' -> escapeSequence()

            '\"' -> {
                next()
                return token(STRING_LITERAL, lexeme(), sb.toString().intern())
            }

            else -> error("illegal character inside string literal")
        }
        sb.append(executionChar)
    }
}
