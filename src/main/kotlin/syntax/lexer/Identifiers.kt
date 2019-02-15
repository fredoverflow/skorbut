package syntax.lexer

fun Lexer.identifierOrKeyword(): Token {
    while (true) {
        when (next()) {
            '0', '1', '2', '3', '4', '5', '6', '7', '8', '9',
            'A', 'B', 'C', 'D', 'E', 'F', 'G', 'H', 'I', 'J', 'K', 'L', 'M', 'N', 'O', 'P', 'Q', 'R', 'S', 'T', 'U', 'V', 'W', 'X', 'Y', 'Z',
            'a', 'b', 'c', 'd', 'e', 'f', 'g', 'h', 'i', 'j', 'k', 'l', 'm', 'n', 'o', 'p', 'q', 'r', 's', 't', 'u', 'v', 'w', 'x', 'y', 'z',
            '_' -> {
            }

            else -> {
                val text = lexeme().intern()
                val index = java.util.Arrays.binarySearch(TOKENS, 0, 33, text)
                val kind = if (index < 0) IDENTIFIER else index.toByte()
                return token(kind, text)
            }
        }
    }
}
