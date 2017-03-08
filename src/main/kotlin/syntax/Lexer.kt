package syntax

class Lexer(input: String) : LexerBase(input) {
    private fun identifierOrKeyword(): Token {
        while (true) {
            when (ch) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '_' -> eat()

                else -> {
                    val text = lexeme().intern()
                    val index = java.util.Arrays.binarySearch(TOKENS, 0, 33, text)
                    val kind = if (index < 0) IDENTIFIER else index.toByte()
                    return token(kind, text)
                }
            }
        }
    }

    private fun constant(): Token {
        var seenDecimalPoint = false
        while (true) {
            when (ch) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> eat()
                'f', 'F' -> {
                    eat()
                    return token(FLOAT_CONSTANT)
                }
                '.' -> {
                    if (seenDecimalPoint) return token(DOUBLE_CONSTANT)
                    eat()
                    seenDecimalPoint = true
                }
                else -> return token(if (seenDecimalPoint) DOUBLE_CONSTANT else INTEGER_CONSTANT)
            }
        }
    }

    private fun zero(): Token {
        if (eat() == 'x' || ch == 'X') return hexadecimal()
        var seen8or9 = false
        var seenDecimalPoint = false
        while (true) {
            when (ch) {
                '0', '1', '2', '3', '4', '5', '6', '7' -> eat()
                '8', '9' -> {
                    seen8or9 = true
                    eat()
                }
                'f', 'F' -> {
                    eat()
                    return token(FLOAT_CONSTANT)
                }
                '.' -> {
                    if (seenDecimalPoint) return token(DOUBLE_CONSTANT)
                    eat()
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

    private fun hexadecimal(): Token {
        when (eat()) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F',
            'a', 'b', 'c', 'd', 'e', 'f' -> eat()

            else -> error("hexadecimal literal indicated by leading 0x must contain at least one digit")
        }
        while (true) {
            when (ch) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F',
                'a', 'b', 'c', 'd', 'e', 'f' -> eat()

                else -> return token(INTEGER_CONSTANT)
            }
        }
    }

    private fun characterConstant(): Token {
        val executionChar = when (eat()) {
            ' ', '!', '#', '$', '%', '&', '\"', '(', ')', '*', '+', ',', '-', '.', '/',
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            ':', ';', '<', '=', '>', '?', '@',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            '[', ']', '^', '_', '`',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '{', '|', '}', '~' -> ch

            '\\' -> escapeSequence()

            else -> error("illegal character inside character constant")
        }
        if (eat() != '\'') error("character constant must be closed by '")

        eat()
        return token(CHARACTER_CONSTANT, lexeme(), executionChar.toString())
    }

    private fun escapeSequence(): Char {
        return when (eat()) {
            '\'', '\"', '?', '\\' -> ch

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
    }

    private fun stringLiteral(): Token {
        val sb = StringBuilder()
        while (true) {
            val executionChar = when (eat()) {
                ' ', '!', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/',
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                ':', ';', '<', '=', '>', '?', '@',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                '[', ']', '^', '_', '`',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '{', '|', '}', '~' -> ch

                '\\' -> escapeSequence()

                '\"' -> {
                    eat()
                    return token(STRING_LITERAL, lexeme(), sb.toString().intern())
                }

                else -> error("illegal character inside string literal")
            }
            sb.append(executionChar)
        }
    }

    fun nextToken(): Token {
        eatCommentsAndWhitespace()
        return when (ch) {
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> identifierOrKeyword()

            '1', '2', '3', '4', '5', '6', '7', '8', '9' -> constant()
            '0' -> zero()

            '\'' -> characterConstant()

            '\"' -> stringLiteral()

            '(' -> consume(OPEN_PAREN)
            ')' -> consume(CLOSE_PAREN)
            ',' -> consume(COMMA)
            '.' -> consume(DOT)
            ':' -> consume(COLON)
            ';' -> consume(SEMICOLON)
            '?' -> consume(QUESTION)
            '[' -> consume(OPEN_BRACKET)
            ']' -> consume(CLOSE_BRACKET)
            '{' -> consume(OPEN_BRACE)
            '}' -> consume(CLOSE_BRACE)
            '~' -> consume(TILDE)

            '!' -> {
                if (eat() == '=') {
                    consume(BANG_EQ)
                } else pack(BANG)
            }

            '%' -> {
                if (eat() == '=') {
                    consume(PERCENT_EQ)
                } else pack(PERCENT)
            }

            '&' -> {
                if (eat() == '=') {
                    consume(AMP_EQ)
                } else if (ch == '&') {
                    consume(AMP_AMP)
                } else pack(AMP)
            }

            '*' -> {
                if (eat() == '=') {
                    consume(STAR_EQ)
                } else pack(STAR)
            }

            '+' -> {
                if (eat() == '=') {
                    consume(PLUS_EQ)
                } else if (ch == '+') {
                    consume(PLUS_PLUS)
                } else pack(PLUS)
            }

            '-' -> {
                if (eat() == '=') {
                    consume(MINUS_EQ)
                } else if (ch == '-') {
                    consume(MINUS_MINUS)
                } else if (ch == '>') {
                    consume(ARROW)
                } else pack(MINUS)
            }

            '/' -> {
                if (eat() == '=') {
                    consume(SLASH_EQ)
                } else pack(SLASH)
            }

            '<' -> {
                if (eat() == '=') {
                    consume(LESS_EQ)
                } else if (ch == '<') {
                    if (eat() == '=') {
                        consume(LESS_LESS_EQ)
                    } else pack(LESS_LESS)
                } else pack(LESS)
            }

            '=' -> {
                if (eat() == '=') {
                    consume(EQ_EQ)
                } else pack(EQ)
            }

            '>' -> {
                if (eat() == '=') {
                    consume(MORE_EQ)
                } else if (ch == '>') {
                    if (eat() == '=') {
                        consume(MORE_MORE_EQ)
                    } else pack(MORE_MORE)
                } else pack(MORE)
            }

            '^' -> {
                if (eat() == '=') {
                    consume(CARET_EQ)
                } else pack(CARET)
            }

            '|' -> {
                if (eat() == '=') {
                    consume(PIPE_EQ)
                } else if (ch == '|') {
                    consume(PIPE_PIPE)
                } else pack(PIPE)
            }

            '\u007f' -> pack(EOF)

            else -> error("illegal input character")
        }
    }

    private fun consume(kind: Byte): Token {
        eat()
        return pack(kind)
    }

    private fun pack(kind: Byte): Token {
        return token(kind, kind.show())
    }
}
