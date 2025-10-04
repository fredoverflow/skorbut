package syntax.lexer

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertSame
import org.junit.jupiter.api.Test
import syntax.lexer.TokenKind.IDENTIFIER
import syntax.lexer.TokenKind.STRING_LITERAL

import kotlin.random.Random

class LexerTest {
    private var lexer = Lexer("")

    @Test
    fun identifiers() {
        lexer =
            Lexer("a z a0 z9 a_z foo _bar the_quick_brown_fox_jumps_over_the_lazy_dog THE_QUICK_BROWN_FOX_JUMPS_OVER_THE_LAZY_DOG")

        expectIdentifier("a")
        expectIdentifier("z")
        expectIdentifier("a0")
        expectIdentifier("z9")
        expectIdentifier("a_z")
        expectIdentifier("foo")
        expectIdentifier("_bar")
        expectIdentifier("the_quick_brown_fox_jumps_over_the_lazy_dog")
        expectIdentifier("THE_QUICK_BROWN_FOX_JUMPS_OVER_THE_LAZY_DOG")
    }

    @Test
    fun stringLiterals() {
        lexer = Lexer(
            """
        "hello"
        "hi there"
        "say \"hi\""
        "\"please\" is the magic word"
        "use \\n for a new line"
        """
        )

        expectStringLiteral("hello")
        expectStringLiteral("hi there")
        expectStringLiteral("""say "hi"""")
        expectStringLiteral(""""please" is the magic word""")
        expectStringLiteral("""use \n for a new line""")
    }

    @Test
    fun singleLineComments() {
        lexer = Lexer(
            """// comment #1
        a
        // comment #2
        // comment #3
        b
        c // comment #4
        d// comment #5
        e//"""
        )

        expectIdentifier("a")
        expectIdentifier("b")
        expectIdentifier("c")
        expectIdentifier("d")
        expectIdentifier("e")
    }

    @Test
    fun multiLineComments() {
        lexer = Lexer(
            """/*
        comment #1
        */
        a   /* comment #2 */
        b  /*/ comment #3*/
        c /**/
        d/***/
        e /* / ** / *** /*/
        f  /*"""
        )

        expectIdentifier("a")
        expectIdentifier("b")
        expectIdentifier("c")
        expectIdentifier("d")
        expectIdentifier("e")
        expectIdentifier("f")
    }

    @Test
    fun keywords() {
        for (kind in TokenKind.KEYWORDS) {
            lexemeRoundTrip(kind)
        }
    }

    @Test
    fun operatorsSeparators() {
        for (kind in TokenKind.OPERATORS_SEPARATORS) {
            lexemeRoundTrip(kind)
        }
    }

    private fun lexemeRoundTrip(kindIn: TokenKind) {
        val lexemeIn = kindIn.lexeme
        lexer = Lexer(lexemeIn)
        val kindOut = lexer.nextToken().kind
        val lexemeOut = kindOut.lexeme
        assertSame(lexemeIn, lexemeOut)
    }

    @Test
    fun nearKeywords() {
        for (kind in TokenKind.KEYWORDS) {
            val keyword = kind.lexeme
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

    private fun randomLetter(): Char = (Random.nextInt(26) + 97).toChar()

    private fun randomLetterOtherThan(forbidden: Char): Char {
        val ch = (Random.nextInt(26 - 1) + 97).toChar()
        // ch is a random character between a and y.
        // In case ch is the forbidden character, 'z' is available.
        // All characters will be chosen with the same probability.
        return if (ch == forbidden) 'z' else ch
    }

    private fun expectIdentifier(identifier: String) {
        val token = lexer.nextToken()
        assertEquals(IDENTIFIER, token.kind)
        assertEquals(identifier, token.text)
    }

    private fun expectStringLiteral(stringLiteral: String) {
        val token = lexer.nextToken()
        assertEquals(STRING_LITERAL, token.kind)
        assertEquals(stringLiteral, token.text)
    }
}
