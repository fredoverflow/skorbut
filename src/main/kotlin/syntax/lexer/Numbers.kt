package syntax.lexer

import syntax.lexer.TokenKind.*

fun Lexer.constant(): Token {
    var seenDecimalPoint = false
    while (true) {
        when (next()) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
            }

            'f', 'F' -> {
                next()
                return token(FLOAT_CONSTANT)
            }

            '.' -> {
                if (seenDecimalPoint) return token(DOUBLE_CONSTANT)
                seenDecimalPoint = true
            }

            else -> return token(if (seenDecimalPoint) DOUBLE_CONSTANT else INTEGER_CONSTANT)
        }
    }
}

fun Lexer.zero(): Token {
    next()
    if (current == 'x' || current == 'X') return hexadecimal()
    if (current == 'b' || current == 'B') return binary()
    previous()

    var seen8or9 = false
    var seenDecimalPoint = false
    while (true) {
        when (next()) {
            '0', '1', '2', '3', '4', '5', '6', '7' -> {
            }

            '8', '9' -> {
                seen8or9 = true
            }

            'f', 'F' -> {
                next()
                return token(FLOAT_CONSTANT)
            }

            '.' -> {
                if (seenDecimalPoint) return token(DOUBLE_CONSTANT)
                seenDecimalPoint = true
            }

            else -> {
                if (seenDecimalPoint) return token(DOUBLE_CONSTANT)
                if (!seen8or9) return token(INTEGER_CONSTANT)

                error("octal literal indicated by leading digit 0 cannot contain digit 8 or 9")
            }
        }
    }
}

fun Lexer.hexadecimal(): Token {
    while (true) {
        when (next()) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F',
            'a', 'b', 'c', 'd', 'e', 'f' -> {
            }

            else -> {
                if (index - start > 2) return token(INTEGER_CONSTANT)

                error("hexadecimal literal indicated by leading ${lexeme()} must contain at least one digit")
            }
        }
    }
}

fun Lexer.binary(): Token {
    while (true) {
        when (next()) {
            '0', '1' -> {
            }

            else -> {
                if (index - start > 2) return token(INTEGER_CONSTANT)

                error("binary literal indicated by leading ${lexeme()} must contain at least one digit")
            }
        }
    }
}
