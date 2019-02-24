package syntax.lexer

tailrec fun Lexer.nextToken(): Token {
    startAtIndex()
    return when (current) {
        ' ', '\u0009', '\u000a', '\u000b', '\u000c', '\u000d' -> {
            next()
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
                while (next() != '\n') {
                    if (current == EOF) return pooled(END_OF_INPUT)
                }
                next() // skip '\n'
                nextToken()
            }
            '*' -> {
                next() // skip '*'
                do {
                    if (current == EOF) return pooled(END_OF_INPUT)
                } while ((current != '*') or (next() != '/'))
                next() // skip '/'
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

        EOF -> pooled(END_OF_INPUT)

        else -> error("illegal input character")
    }
}
