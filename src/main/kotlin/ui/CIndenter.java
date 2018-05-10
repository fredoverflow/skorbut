package ui;

import freditor.JavaIndenter;

public class CIndenter extends JavaIndenter {
    public static final CIndenter instance = new CIndenter();

    @Override
    public int indentationDelta(int state) {
        switch (state) {
            case Flexer.OPENING_PAREN:
            case Flexer.OPENING_BRACKET:
            case Flexer.OPENING_BRACE:
                return +4;

            case Flexer.CLOSING_PAREN:
            case Flexer.CLOSING_BRACKET:
            case Flexer.CLOSING_BRACE:
                return -4;

            default:
                return 0;
        }
    }
}
