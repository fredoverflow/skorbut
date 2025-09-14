package ui

import common.puts
import freditor.FlexerState
import freditor.FlexerState.EMPTY
import freditor.FlexerState.THIS
import freditor.FlexerStateBuilder

object Flexer : freditor.Flexer() {
    private val SLASH_SLASH = FlexerState('\n', null).setDefault(THIS)
    private val SLASH_ASTERISK___ASTERISK_SLASH = EMPTY.tail()
    private val SLASH_ASTERISK___ASTERISK = FlexerState('*', THIS, '/', SLASH_ASTERISK___ASTERISK_SLASH)
    private val SLASH_ASTERISK = FlexerState('*', SLASH_ASTERISK___ASTERISK).setDefault(THIS)

    private val CHAR_CONSTANT_END = EMPTY.tail()
    private val CHAR_CONSTANT_ESCAPE = FlexerState('\n', null)
    private val CHAR_CONSTANT_TAIL =
        FlexerState('\n', null, '\'', CHAR_CONSTANT_END, '\\', CHAR_CONSTANT_ESCAPE).setDefault(THIS)
    private val CHAR_CONSTANT_HEAD = CHAR_CONSTANT_TAIL.head()

    private val STRING_LITERAL_END = EMPTY.tail()
    private val STRING_LITERAL_ESCAPE = FlexerState('\n', null)
    private val STRING_LITERAL_TAIL =
        FlexerState('\n', null, '\"', STRING_LITERAL_END, '\\', STRING_LITERAL_ESCAPE).setDefault(THIS)
    private val STRING_LITERAL_HEAD = STRING_LITERAL_TAIL.head()

    init {
        SLASH_ASTERISK___ASTERISK.setDefault(SLASH_ASTERISK)
        CHAR_CONSTANT_ESCAPE.setDefault(CHAR_CONSTANT_TAIL)
        STRING_LITERAL_ESCAPE.setDefault(STRING_LITERAL_TAIL)
    }

    private val NUMBER_TAIL = FlexerState("..09AFXXafxx", THIS)
    private val NUMBER_HEAD = NUMBER_TAIL.head()

    private val IDENTIFIER_TAIL = FlexerState("09AZ__az", THIS)
    private val IDENTIFIER_HEAD = IDENTIFIER_TAIL.head()

    private val START = FlexerStateBuilder()
        .set('(', OPENING_PAREN)
        .set(')', CLOSING_PAREN)
        .set('[', OPENING_BRACKET)
        .set(']', CLOSING_BRACKET)
        .set('{', OPENING_BRACE)
        .set('}', CLOSING_BRACE)
        .set('\n', NEWLINE)
        .set(' ', SPACE_HEAD)
        .set('/', FlexerState('*', SLASH_ASTERISK, '/', SLASH_SLASH).head())
        .set('\'', CHAR_CONSTANT_HEAD)
        .set('\"', STRING_LITERAL_HEAD)
        .set("09", NUMBER_HEAD)
        .set("AZ__az", IDENTIFIER_HEAD)
        .build()
        .verbatim(
            IDENTIFIER_TAIL, "assert", "auto", "break", "case", "char", "const", "continue",
            "default", "do", "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long",
            "register", "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union",
            "unsigned", "void", "volatile", "while"
        )
        .verbatim(
            EMPTY, "!", "!=", "%", "%=", "&", "&&", "&=", "*", "*=", "+", "++", "+=", ",", "-", "--",
            "-=", "->", ".", "/", "/=", ":", ";", "<", "<<", "<<=", "<=", "=", "==", ">", ">=", ">>", ">>=", "?",
            "^", "^=", "|", "|=", "||", "~"
        )
        .setDefault(ERROR)

    override fun start(): FlexerState = START

    override fun pickColorForLexeme(previousState: FlexerState, endState: FlexerState): Int {
        return lexemeColors[endState] ?: 0x000000
    }

    private val lexemeColors = hashMapOf(ERROR to 0x808080)
        .puts(
            CHAR_CONSTANT_HEAD,
            CHAR_CONSTANT_TAIL,
            CHAR_CONSTANT_ESCAPE,
            STRING_LITERAL_HEAD,
            STRING_LITERAL_TAIL,
            STRING_LITERAL_ESCAPE,
            0x808080
        )
        .puts(SLASH_SLASH, SLASH_ASTERISK, SLASH_ASTERISK___ASTERISK, SLASH_ASTERISK___ASTERISK_SLASH, 0x008000)
        .puts(CHAR_CONSTANT_END, STRING_LITERAL_END, 0xdc009c)
        .puts(NUMBER_HEAD, NUMBER_TAIL, 0x6400c8)
        .puts(
            START.read(
                "assert", "auto", "break", "case", "const", "continue", "default", "do", "else",
                "enum", "extern", "for", "goto", "if", "register", "return", "sizeof", "static", "struct", "switch",
                "typedef", "union", "volatile", "while"
            ), 0x0000ff
        )
        .puts(START.read("char", "double", "float", "int", "long", "short", "signed", "unsigned", "void"), 0x008080)
        .puts(START.read("(", ")", "[", "]", "{", "}"), 0xff0000)
        .puts(
            START.read(
                "!", "!=", "%", "%=", "&", "&&", "&=", "*", "*=", "+", "++", "+=", "-", "--",
                "-=", "->", ".", "/", "/=", ":", "<", "<<", "<<=", "<=", "=", "==", ">", ">=", ">>", ">>=", "?",
                "^", "^=", "|", "|=", "||", "~"
            ), 0x804040
        )
}
