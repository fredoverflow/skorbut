package syntax

import java.util.Random
import org.junit.Test
import org.junit.Assert.*

class LexerTest {
    var lexer = Lexer("")

    @Test fun identifiers() {
        lexer = Lexer("a z a0 z9 a_z foo _bar the_quick_brown_fox_jumped_over_the_lazy_dog THE_QUICK_BROWN_FOX_JUMPED_OVER_THE_LAZY_DOG")

        expectIdentifier("a")
        expectIdentifier("z")
        expectIdentifier("a0")
        expectIdentifier("z9")
        expectIdentifier("a_z")
        expectIdentifier("foo")
        expectIdentifier("_bar")
        expectIdentifier("the_quick_brown_fox_jumped_over_the_lazy_dog")
        expectIdentifier("THE_QUICK_BROWN_FOX_JUMPED_OVER_THE_LAZY_DOG")
    }

    @Test fun stringLiterals() {
        lexer = Lexer("""
        "hello"
        "hi there"
        "say \"hi\""
        "\"please\" is the magic word"
        "use \\n for a new line"
        """)

        expectStringLiteral("hello")
        expectStringLiteral("hi there")
        expectStringLiteral("""say "hi"""")
        expectStringLiteral(""""please" is the magic word""")
        expectStringLiteral("""use \n for a new line""")
    }

    @Test fun singleLineComments() {
        lexer = Lexer("""// comment #1
        a
        // comment #2
        // comment #3
        b
        c // comment #4
        d// comment #5
        e//""")

        expectIdentifier("a")
        expectIdentifier("b")
        expectIdentifier("c")
        expectIdentifier("d")
        expectIdentifier("e")
    }

    @Test fun multiLineComments() {
        lexer = Lexer("""/*
        comment #1
        */
        a   /* comment #2 */
        b  /*/ comment #3*/
        c /**/
        d/***/
        e /* / ** / *** /*/
        f  /*""")

        expectIdentifier("a")
        expectIdentifier("b")
        expectIdentifier("c")
        expectIdentifier("d")
        expectIdentifier("e")
        expectIdentifier("f")
    }

    @Test fun keywords() {
        for (tok in 0..32) {
            tokenRoundTrip(tok)
        }
    }

    @Test fun otherTokens() {
        for (tok in 33..SEMICOLON) {
            tokenRoundTrip(tok)
        }
    }

    private fun tokenRoundTrip(tokIn: Int) {
        val tokenIn = tokIn.toByte().show()
        lexer = Lexer(tokenIn)
        val tokOut = lexer.nextToken().kind
        val tokenOut = tokOut.show()
        assertEquals(tokenIn, tokenOut)
    }

    @Test fun nearKeywords() {
        for (tok in 0..32) {
            val keyword = tok.toByte().show()
            val init = keyword.substring(0, keyword.length - 1)
            val last = keyword.last()
            identifierRoundTrip(init)
            identifierRoundTrip(init + randomLetterOtherThan(last))
            identifierRoundTrip(keyword + randomLetter())
        }
    }

    private fun identifierRoundTrip(identifier: String) {
        lexer = Lexer(identifier)
        expectIdentifier(identifier)
    }

    private fun randomLetter(): Char = (rng.nextInt(26) + 97).toChar()

    private val rng = Random()

    private fun randomLetterOtherThan(forbidden: Char): Char {
        val ch = (rng.nextInt(26 - 1) + 97).toChar()
        // ch is a random character between a and y.
        // In case ch is the forbidden character, 'z' is available.
        // All characters will be chosen with the same probability.
        return if (ch == forbidden) 'z' else ch
    }

    private fun expectIdentifier(identifier: String) {
        val token = lexer.nextToken()
        assertEquals(IDENTIFIER.show(), token.kind.show())
        assertEquals(identifier, token.text)
    }

    private fun expectStringLiteral(stringLiteral: String) {
        val token = lexer.nextToken()
        assertEquals(STRING_LITERAL.show(), token.kind.show())
        assertEquals(stringLiteral, token.text)
    }
}
