package ui;

import freditor.FlexerState;
import freditor.FlexerStateBuilder;
import freditor.persistent.ChampMap;

import static freditor.FlexerState.EMPTY;
import static freditor.FlexerState.THIS;

public class Flexer extends freditor.Flexer {
    public static final Flexer instance = new Flexer();

    private static final FlexerState SLASH_SLASH = new FlexerState('\n', null).setDefault(THIS);
    private static final FlexerState SLASH_ASTERISK___ASTERISK_SLASH = EMPTY.tail();
    private static final FlexerState SLASH_ASTERISK___ASTERISK = new FlexerState('*', THIS, '/', SLASH_ASTERISK___ASTERISK_SLASH);
    private static final FlexerState SLASH_ASTERISK = new FlexerState('*', SLASH_ASTERISK___ASTERISK).setDefault(THIS);

    private static final FlexerState CHAR_CONSTANT_END = EMPTY.tail();
    private static final FlexerState CHAR_CONSTANT_ESCAPE = new FlexerState('\n', null);
    private static final FlexerState CHAR_CONSTANT_TAIL = new FlexerState('\n', null, '\'', CHAR_CONSTANT_END, '\\', CHAR_CONSTANT_ESCAPE).setDefault(THIS);
    private static final FlexerState CHAR_CONSTANT_HEAD = CHAR_CONSTANT_TAIL.head();

    private static final FlexerState STRING_LITERAL_END = EMPTY.tail();
    private static final FlexerState STRING_LITERAL_ESCAPE = new FlexerState('\n', null);
    private static final FlexerState STRING_LITERAL_TAIL = new FlexerState('\n', null, '\"', STRING_LITERAL_END, '\\', STRING_LITERAL_ESCAPE).setDefault(THIS);
    private static final FlexerState STRING_LITERAL_HEAD = STRING_LITERAL_TAIL.head();

    static {
        SLASH_ASTERISK___ASTERISK.setDefault(SLASH_ASTERISK);
        CHAR_CONSTANT_ESCAPE.setDefault(CHAR_CONSTANT_TAIL);
        STRING_LITERAL_ESCAPE.setDefault(STRING_LITERAL_TAIL);
    }

    private static final FlexerState NUMBER_TAIL = new FlexerState("..09AFXXafxx", THIS);
    private static final FlexerState NUMBER_HEAD = NUMBER_TAIL.head();

    private static final FlexerState IDENTIFIER_TAIL = new FlexerState("09AZ__az", THIS);
    private static final FlexerState IDENTIFIER_HEAD = IDENTIFIER_TAIL.head();

    private static final FlexerState START = new FlexerStateBuilder()
            .set('(', OPENING_PAREN)
            .set(')', CLOSING_PAREN)
            .set('[', OPENING_BRACKET)
            .set(']', CLOSING_BRACKET)
            .set('{', OPENING_BRACE)
            .set('}', CLOSING_BRACE)
            .set('\n', NEWLINE)
            .set(' ', SPACE_HEAD)
            .set('/', new FlexerState('*', SLASH_ASTERISK, '/', SLASH_SLASH).head())
            .set('\'', CHAR_CONSTANT_HEAD)
            .set('\"', STRING_LITERAL_HEAD)
            .set("09", NUMBER_HEAD)
            .set("AZ__az", IDENTIFIER_HEAD)
            .build()
            .verbatim(IDENTIFIER_TAIL, "assert", "auto", "break", "case", "char", "const", "continue", "default", "do",
                    "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register",
                    "return", "short", "signed", "sizeof", "static", "struct", "switch", "typedef", "union",
                    "unsigned", "void", "volatile", "while")
            .verbatim(EMPTY, "!", "!=", "%", "%=", "&", "&&", "&=", "*", "*=", "+", "++", "+=", ",", "-", "--", "-=",
                    "->", ".", "/", "/=", ":", ";", "<", "<<", "<<=", "<=", "=", "==", ">", ">=", ">>", ">>=", "?",
                    "^", "^=", "|", "|=", "||", "~")
            .setDefault(ERROR);

    @Override
    protected FlexerState start() {
        return START;
    }

    @Override
    public int pickColorForLexeme(FlexerState previousState, FlexerState endState) {
        Integer color = lexemeColors.get(endState);
        return color != null ? color : 0x000000;
    }

    private static final ChampMap<FlexerState, Integer> lexemeColors = ChampMap.of(ERROR, 0x808080)
            .put(CHAR_CONSTANT_HEAD, CHAR_CONSTANT_TAIL, CHAR_CONSTANT_ESCAPE, STRING_LITERAL_HEAD, STRING_LITERAL_TAIL, STRING_LITERAL_ESCAPE, 0x808080)
            .put(SLASH_SLASH, SLASH_ASTERISK, SLASH_ASTERISK___ASTERISK, SLASH_ASTERISK___ASTERISK_SLASH, 0x008000)
            .put(CHAR_CONSTANT_END, STRING_LITERAL_END, 0xdc009c)
            .put(NUMBER_HEAD, NUMBER_TAIL, 0x6400c8)
            .tup(0x0000ff, START::read, "assert", "auto", "break", "case", "const", "continue", "default", "do", "else",
                    "enum", "extern", "for", "goto", "if", "register", "return", "sizeof", "static", "struct", "switch",
                    "typedef", "union", "volatile", "while")
            .tup(0x008080, START::read, "char", "double", "float", "int", "long", "short", "signed", "unsigned", "void")
            .tup(0xff0000, START::read, "(", ")", "[", "]", "{", "}")
            .tup(0x804040, START::read, "!", "!=", "%", "%=", "&", "&&", "&=", "*", "*=", "+", "++", "+=", "-", "--",
                    "-=", "->", ".", "/", "/=", ":", "<", "<<", "<<=", "<=", "=", "==", ">", ">=", ">>", ">>=", "?",
                    "^", "^=", "|", "|=", "||", "~");
}
