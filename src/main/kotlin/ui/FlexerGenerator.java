package ui;

public class FlexerGenerator {
    public static void main(String[] args) {
        new freditor.FlexerGenerator(-13, 13).generateTokens(
                "assert", "auto", "break", "case", "char", "const", "continue", "default", "do", "double", "else",
                "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short", "signed",
                "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while",

                "!", "!=", "%", "%=", "&", "&&", "&=", "*", "*=", "+", "++", "+=", ",", "-", "--", "-=", "->",
                ".", "/", "/=", ":", ";", "<", "<<", "<<=", "<=", "=", "==", ">", ">=", ">>", ">>=", "?", "^",
                "^=", "|", "|=", "||", "~"
        );
    }
}