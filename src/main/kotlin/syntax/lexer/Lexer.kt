package syntax.lexer

class Lexer(input: String) : LexerBase(input) {
    tailrec fun nextToken(): Token {
        start = index
        return when (current) {
            ' ', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d' -> {
                ignoreWhitespace()
                nextToken()
            }

            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> identifierOrKeyword()

            '1', '2', '3', '4', '5', '6', '7', '8', '9' -> constant()
            '0' -> zero()

            '\'' -> characterConstant()

            '\"' -> stringLiteral()

            '(' -> nextPooled(OPENING_PAREN)
            ')' -> nextPooled(CLOSING_PAREN)
            ',' -> nextPooled(COMMA)
            '.' -> nextPooled(DOT)
            ':' -> nextPooled(COLON)
            ';' -> nextPooled(SEMICOLON)
            '?' -> nextPooled(QUESTION)
            '[' -> nextPooled(OPENING_BRACKET)
            ']' -> nextPooled(CLOSING_BRACKET)
            '{' -> nextPooled(OPENING_BRACE)
            '}' -> nextPooled(CLOSING_BRACE)
            '~' -> nextPooled(TILDE)

            '!' -> when (next()) {
                '=' -> nextPooled(BANG_EQUAL)
                else -> pooled(BANG)
            }

            '%' -> when (next()) {
                '=' -> nextPooled(PERCENT_EQUAL)
                else -> pooled(PERCENT)
            }

            '&' -> when (next()) {
                '=' -> nextPooled(AMPERSAND_EQUAL)
                '&' -> nextPooled(AMPERSAND_AMPERSAND)
                else -> pooled(AMPERSAND)
            }

            '*' -> when (next()) {
                '=' -> nextPooled(ASTERISK_EQUAL)
                else -> pooled(ASTERISK)
            }

            '+' -> when (next()) {
                '=' -> nextPooled(PLUS_EQUAL)
                '+' -> nextPooled(PLUS_PLUS)
                else -> pooled(PLUS)
            }

            '-' -> when (next()) {
                '=' -> nextPooled(HYPHEN_EQUAL)
                '-' -> nextPooled(HYPHEN_HYPHEN)
                '>' -> nextPooled(HYPHEN_MORE)
                else -> pooled(HYPHEN)
            }

            '/' -> when (next()) {
                '/' -> {
                    ignoreSingleLineComment()
                    nextToken()
                }
                '*' -> {
                    ignoreMultiLineComment()
                    nextToken()
                }
                '=' -> nextPooled(SLASH_EQUAL)
                else -> pooled(SLASH)
            }

            '<' -> when (next()) {
                '=' -> nextPooled(LESS_EQUAL)
                '<' -> when (next()) {
                    '=' -> nextPooled(LESS_LESS_EQUAL)
                    else -> pooled(LESS_LESS)
                }
                else -> pooled(LESS)
            }

            '=' -> when (next()) {
                '=' -> nextPooled(EQUAL_EQUAL)
                else -> pooled(EQUAL)
            }

            '>' -> when (next()) {
                '=' -> nextPooled(MORE_EQUAL)
                '>' -> when (next()) {
                    '=' -> nextPooled(MORE_MORE_EQUAL)
                    else -> pooled(MORE_MORE)
                }
                else -> pooled(MORE)
            }

            '^' -> when (next()) {
                '=' -> nextPooled(CARET_EQUAL)
                else -> pooled(CARET)
            }

            '|' -> when (next()) {
                '=' -> nextPooled(BAR_EQUAL)
                '|' -> nextPooled(BAR_BAR)
                else -> pooled(BAR)
            }

            '\u007f' -> pooled(END_OF_INPUT)

            else -> error("illegal input character")
        }
    }

    private tailrec fun ignoreWhitespace() {
        when (next()) {
            ' ', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d' -> ignoreWhitespace()

            else -> {
            }
        }
    }

    private fun ignoreSingleLineComment() {
        while (nextOr('\n') != '\n');
    }

    private fun ignoreMultiLineComment() {
        do {
            while (nextOr('*') != '*');
            while (nextOr('/') == '*');
        } while (current != '/')
        next()
    }

    private fun identifierOrKeyword(): Token {
        while (true) {
            when (current) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
                'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
                '_' -> next()

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
            when (current) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> next()
                'f', 'F' -> {
                    next()
                    return token(FLOAT_CONSTANT)
                }
                '.' -> {
                    if (seenDecimalPoint) return token(DOUBLE_CONSTANT)
                    next()
                    seenDecimalPoint = true
                }
                else -> return token(if (seenDecimalPoint) DOUBLE_CONSTANT else INTEGER_CONSTANT)
            }
        }
    }

    private fun zero(): Token {
        if (next() == 'x' || current == 'X') return hexadecimal()
        var seen8or9 = false
        var seenDecimalPoint = false
        while (true) {
            when (current) {
                '0', '1', '2', '3', '4', '5', '6', '7' -> next()
                '8', '9' -> {
                    seen8or9 = true
                    next()
                }
                'f', 'F' -> {
                    next()
                    return token(FLOAT_CONSTANT)
                }
                '.' -> {
                    if (seenDecimalPoint) return token(DOUBLE_CONSTANT)
                    next()
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
        when (next()) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F',
            'a', 'b', 'c', 'd', 'e', 'f' -> next()

            else -> error("hexadecimal literal indicated by leading 0x must contain at least one digit")
        }
        while (true) {
            when (current) {
                '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
                'A', 'B', 'C', 'D', 'E', 'F',
                'a', 'b', 'c', 'd', 'e', 'f' -> next()

                else -> return token(INTEGER_CONSTANT)
            }
        }
    }

    private fun characterConstant(): Token {
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

    private fun escapeSequence(): Char {
        return when (next()) {
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
    }

    private fun stringLiteral(): Token {
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
}
