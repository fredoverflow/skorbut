package syntax.lexer

import syntax.lexer.TokenKind.IDENTIFIER

tailrec fun Lexer.identifierOrKeyword(): Token = when (next()) {
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    '_', '0', '1', '2', '3', '4', '5', '6', '7', '8', '9' -> identifierOrKeyword()

    else -> {
        val lexeme = lexeme()
        when (val value: Any? = identifiersOrKeywords[lexeme]) {
            is TokenKind -> verbatim(value)

            is String -> token(IDENTIFIER, value)

            else -> {
                identifiersOrKeywords[lexeme] = lexeme
                token(IDENTIFIER, lexeme)
            }
        }
    }
}
