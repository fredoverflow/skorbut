package syntax.lexer

tailrec fun Lexer.identifierOrKeyword(): Token = when (next()) {
    '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
    'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
    'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
    '_' -> identifierOrKeyword()

    else -> {
        val lexeme = lexeme()
        val keyword = lexemePool.binarySearch(lexeme, 0, NUM_KEYWORDS)
        when {
            keyword >= 0 -> pooled(keyword.toByte())
            else -> token(IDENTIFIER, lexeme.intern())
        }
    }
}
