package syntax.lexer

import syntax.lexer.TokenKind.*

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

        '(' -> nextVerbatim(OPENING_PAREN)
        ')' -> nextVerbatim(CLOSING_PAREN)
        ',' -> nextVerbatim(COMMA)
        '.' -> nextVerbatim(DOT)
        ':' -> nextVerbatim(COLON)
        ';' -> nextVerbatim(SEMICOLON)
        '?' -> nextVerbatim(QUESTION)
        '[' -> nextVerbatim(OPENING_BRACKET)
        ']' -> nextVerbatim(CLOSING_BRACKET)
        '{' -> nextVerbatim(OPENING_BRACE)
        '}' -> nextVerbatim(CLOSING_BRACE)
        '~' -> nextVerbatim(TILDE)

        '!' -> when (next()) {
            '=' -> nextVerbatim(BANG_EQUAL)
            else -> verbatim(BANG)
        }

        '%' -> when (next()) {
            '=' -> nextVerbatim(PERCENT_EQUAL)
            else -> verbatim(PERCENT)
        }

        '&' -> when (next()) {
            '=' -> nextVerbatim(AMPERSAND_EQUAL)
            '&' -> nextVerbatim(AMPERSAND_AMPERSAND)
            else -> verbatim(AMPERSAND)
        }

        '*' -> when (next()) {
            '=' -> nextVerbatim(ASTERISK_EQUAL)
            else -> verbatim(ASTERISK)
        }

        '+' -> when (next()) {
            '=' -> nextVerbatim(PLUS_EQUAL)
            '+' -> nextVerbatim(PLUS_PLUS)
            else -> verbatim(PLUS)
        }

        '-' -> when (next()) {
            '=' -> nextVerbatim(HYPHEN_EQUAL)
            '-' -> nextVerbatim(HYPHEN_HYPHEN)
            '>' -> nextVerbatim(HYPHEN_MORE)
            else -> verbatim(HYPHEN)
        }

        '/' -> when (next()) {
            '/' -> {
                while (next() != '\n') {
                    if (current == EOF) return verbatim(END_OF_INPUT)
                }
                next() // skip '\n'
                nextToken()
            }
            '*' -> {
                next() // skip '*'
                do {
                    if (current == EOF) return verbatim(END_OF_INPUT)
                } while ((current != '*') or (next() != '/'))
                next() // skip '/'
                nextToken()
            }
            '=' -> nextVerbatim(SLASH_EQUAL)
            else -> verbatim(SLASH)
        }

        '<' -> when (next()) {
            '=' -> nextVerbatim(LESS_EQUAL)
            '<' -> when (next()) {
                '=' -> nextVerbatim(LESS_LESS_EQUAL)
                else -> verbatim(LESS_LESS)
            }
            else -> verbatim(LESS)
        }

        '=' -> when (next()) {
            '=' -> nextVerbatim(EQUAL_EQUAL)
            else -> verbatim(EQUAL)
        }

        '>' -> when (next()) {
            '=' -> nextVerbatim(MORE_EQUAL)
            '>' -> when (next()) {
                '=' -> nextVerbatim(MORE_MORE_EQUAL)
                else -> verbatim(MORE_MORE)
            }
            else -> verbatim(MORE)
        }

        '^' -> when (next()) {
            '=' -> nextVerbatim(CARET_EQUAL)
            else -> verbatim(CARET)
        }

        '|' -> when (next()) {
            '=' -> nextVerbatim(BAR_EQUAL)
            '|' -> nextVerbatim(BAR_BAR)
            else -> verbatim(BAR)
        }

        EOF -> verbatim(END_OF_INPUT)

        else -> error("illegal input character")
    }
}
