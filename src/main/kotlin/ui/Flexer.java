package ui;

public class Flexer extends freditor.Flexer {
    public static final int END = 0;
    public static final int ERROR = -1;

    public static final int NEWLINE = -2;
    public static final int FIRST_SPACE = -3;
    public static final int NEXT_SPACE = 1;

    // public static final int SLASH = -4;
    public static final int SLASH_SLASH = 2;
    public static final int SLASH_STAR = 3;
    public static final int SLASH_STAR___STAR = 4;
    public static final int SLASH_STAR___STAR_SLASH = 5;

    public static final int CHAR_CONSTANT_BEGIN = -4;
    public static final int CHAR_CONSTANT_ESCAPE = 6;
    public static final int CHAR_CONSTANT_INSIDE = 7;
    public static final int CHAR_CONSTANT_END = 8;

    public static final int STRING_LITERAL_BEGIN = -5;
    public static final int STRING_LITERAL_ESCAPE = 9;
    public static final int STRING_LITERAL_INSIDE = 10;
    public static final int STRING_LITERAL_END = 11;

    public static final int FIRST_DIGIT = -6;
    public static final int NEXT_DIGIT = 12;

    public static final int IDENTIFIER_FIRST = -7;
    public static final int IDENTIFIER_NEXT = 13;

    // auto-generated by freditor.TokenGenerator
    public static final int A = -8;
    public static final int AS = 14;
    public static final int ASS = 15;
    public static final int ASSE = 16;
    public static final int ASSER = 17;
    public static final int ASSERT = 18;
    public static final int AU = 19;
    public static final int AUT = 20;
    public static final int AUTO = 21;
    public static final int B = -9;
    public static final int BR = 22;
    public static final int BRE = 23;
    public static final int BREA = 24;
    public static final int BREAK = 25;
    public static final int C = -10;
    public static final int CA = 26;
    public static final int CAS = 27;
    public static final int CASE = 28;
    public static final int CH = 29;
    public static final int CHA = 30;
    public static final int CHAR = 31;
    public static final int CO = 32;
    public static final int CON = 33;
    public static final int CONS = 34;
    public static final int CONST = 35;
    public static final int CONT = 36;
    public static final int CONTI = 37;
    public static final int CONTIN = 38;
    public static final int CONTINU = 39;
    public static final int CONTINUE = 40;
    public static final int D = -11;
    public static final int DE = 41;
    public static final int DEF = 42;
    public static final int DEFA = 43;
    public static final int DEFAU = 44;
    public static final int DEFAUL = 45;
    public static final int DEFAULT = 46;
    public static final int DO = 47;
    public static final int DOU = 48;
    public static final int DOUB = 49;
    public static final int DOUBL = 50;
    public static final int DOUBLE = 51;
    public static final int E = -12;
    public static final int EL = 52;
    public static final int ELS = 53;
    public static final int ELSE = 54;
    public static final int EN = 55;
    public static final int ENU = 56;
    public static final int ENUM = 57;
    public static final int EX = 58;
    public static final int EXT = 59;
    public static final int EXTE = 60;
    public static final int EXTER = 61;
    public static final int EXTERN = 62;
    public static final int F = -13;
    public static final int FL = 63;
    public static final int FLO = 64;
    public static final int FLOA = 65;
    public static final int FLOAT = 66;
    public static final int FO = 67;
    public static final int FOR = 68;
    public static final int G = -14;
    public static final int GO = 69;
    public static final int GOT = 70;
    public static final int GOTO = 71;
    public static final int I = -15;
    public static final int IF = 72;
    public static final int IN = 73;
    public static final int INT = 74;
    public static final int L = -16;
    public static final int LO = 75;
    public static final int LON = 76;
    public static final int LONG = 77;
    public static final int R = -17;
    public static final int RE = 78;
    public static final int REG = 79;
    public static final int REGI = 80;
    public static final int REGIS = 81;
    public static final int REGIST = 82;
    public static final int REGISTE = 83;
    public static final int REGISTER = 84;
    public static final int RET = 85;
    public static final int RETU = 86;
    public static final int RETUR = 87;
    public static final int RETURN = 88;
    public static final int S = -18;
    public static final int SH = 89;
    public static final int SHO = 90;
    public static final int SHOR = 91;
    public static final int SHORT = 92;
    public static final int SI = 93;
    public static final int SIG = 94;
    public static final int SIGN = 95;
    public static final int SIGNE = 96;
    public static final int SIGNED = 97;
    public static final int SIZ = 98;
    public static final int SIZE = 99;
    public static final int SIZEO = 100;
    public static final int SIZEOF = 101;
    public static final int ST = 102;
    public static final int STA = 103;
    public static final int STAT = 104;
    public static final int STATI = 105;
    public static final int STATIC = 106;
    public static final int STR = 107;
    public static final int STRU = 108;
    public static final int STRUC = 109;
    public static final int STRUCT = 110;
    public static final int SW = 111;
    public static final int SWI = 112;
    public static final int SWIT = 113;
    public static final int SWITC = 114;
    public static final int SWITCH = 115;
    public static final int T = -19;
    public static final int TY = 116;
    public static final int TYP = 117;
    public static final int TYPE = 118;
    public static final int TYPED = 119;
    public static final int TYPEDE = 120;
    public static final int TYPEDEF = 121;
    public static final int U = -20;
    public static final int UN = 122;
    public static final int UNI = 123;
    public static final int UNIO = 124;
    public static final int UNION = 125;
    public static final int UNS = 126;
    public static final int UNSI = 127;
    public static final int UNSIG = 128;
    public static final int UNSIGN = 129;
    public static final int UNSIGNE = 130;
    public static final int UNSIGNED = 131;
    public static final int V = -21;
    public static final int VO = 132;
    public static final int VOI = 133;
    public static final int VOID = 134;
    public static final int VOL = 135;
    public static final int VOLA = 136;
    public static final int VOLAT = 137;
    public static final int VOLATI = 138;
    public static final int VOLATIL = 139;
    public static final int VOLATILE = 140;
    public static final int W = -22;
    public static final int WH = 141;
    public static final int WHI = 142;
    public static final int WHIL = 143;
    public static final int WHILE = 144;
    public static final int BANG = -23;
    public static final int BANG_EQUAL = 145;
    public static final int PERCENT = -24;
    public static final int PERCENT_EQUAL = 146;
    public static final int AMPERSAND = -25;
    public static final int AMPERSAND_AMPERSAND = 147;
    public static final int AMPERSAND_EQUAL = 148;
    public static final int OPEN_PAREN = -26;
    public static final int CLOSE_PAREN = -27;
    public static final int ASTERISK = -28;
    public static final int ASTERISK_EQUAL = 149;
    public static final int PLUS = -29;
    public static final int PLUS_PLUS = 150;
    public static final int PLUS_EQUAL = 151;
    public static final int COMMA = -30;
    public static final int HYPHEN = -31;
    public static final int HYPHEN_HYPHEN = 152;
    public static final int HYPHEN_EQUAL = 153;
    public static final int HYPHEN_MORE = 154;
    public static final int DOT = -32;
    public static final int SLASH = -33;
    public static final int SLASH_EQUAL = 155;
    public static final int COLON = -34;
    public static final int SEMICOLON = -35;
    public static final int LESS = -36;
    public static final int LESS_LESS = 156;
    public static final int LESS_LESS_EQUAL = 157;
    public static final int LESS_EQUAL = 158;
    public static final int EQUAL = -37;
    public static final int EQUAL_EQUAL = 159;
    public static final int MORE = -38;
    public static final int MORE_EQUAL = 160;
    public static final int MORE_MORE = 161;
    public static final int MORE_MORE_EQUAL = 162;
    public static final int QUESTION = -39;
    public static final int OPEN_BRACKET = -40;
    public static final int CLOSE_BRACKET = -41;
    public static final int CARET = -42;
    public static final int CARET_EQUAL = 163;
    public static final int OPEN_BRACE = -43;
    public static final int BAR = -44;
    public static final int BAR_EQUAL = 164;
    public static final int BAR_BAR = 165;
    public static final int CLOSE_BRACE = -45;
    public static final int TILDE = -46;

    @Override
    public int openBrace() {
        return OPEN_BRACE;
    }

    @Override
    public int closeBrace() {
        return CLOSE_BRACE;
    }

    @Override
    public int pickColorForLexeme(int endState) {
        switch (endState) {
            default:
                return 0x000000;

            case ERROR:

            case CHAR_CONSTANT_BEGIN:
            case CHAR_CONSTANT_ESCAPE:
            case CHAR_CONSTANT_INSIDE:

            case STRING_LITERAL_BEGIN:
            case STRING_LITERAL_ESCAPE:
            case STRING_LITERAL_INSIDE:
                return 0x808080;

            case SLASH_SLASH:
            case SLASH_STAR:
            case SLASH_STAR___STAR:
            case SLASH_STAR___STAR_SLASH:
                return 0x008000;

            case CHAR_CONSTANT_END:
            case STRING_LITERAL_END:
                return 0xdc009c;

            case FIRST_DIGIT:
            case NEXT_DIGIT:
                return 0x6400c8;

            case ASSERT:
            case AUTO:
            case BREAK:
            case CASE:
            case CONST:
            case CONTINUE:
            case DEFAULT:
            case ELSE:
            case ENUM:
            case EXTERN:
            case FOR:
            case GOTO:
            case IF:
            case REGISTER:
            case RETURN:
            case SIZEOF:
            case STATIC:
            case STRUCT:
            case SWITCH:
            case TYPEDEF:
            case UNION:
            case VOLATILE:
            case WHILE:
                return 0x0000ff;

            case CHAR:
            case DOUBLE:
            case FLOAT:
            case INT:
            case LONG:
            case SHORT:
            case SIGNED:
            case UNSIGNED:
            case VOID:
                return 0x008080;

            case OPEN_PAREN:
            case CLOSE_PAREN:
            case OPEN_BRACKET:
            case CLOSE_BRACKET:
            case OPEN_BRACE:
            case CLOSE_BRACE:
                return 0xff0000;

            case BANG:
            case BANG_EQUAL:
            case PERCENT:
            case PERCENT_EQUAL:
            case AMPERSAND:
            case AMPERSAND_AMPERSAND:
            case AMPERSAND_EQUAL:
            case ASTERISK:
            case ASTERISK_EQUAL:
            case PLUS:
            case PLUS_PLUS:
            case PLUS_EQUAL:
            case HYPHEN:
            case HYPHEN_HYPHEN:
            case HYPHEN_EQUAL:
            case HYPHEN_MORE:
            case DOT:
            case SLASH:
            case SLASH_EQUAL:
            case COLON:
            case LESS:
            case LESS_LESS:
            case LESS_LESS_EQUAL:
            case LESS_EQUAL:
            case EQUAL:
            case EQUAL_EQUAL:
            case MORE:
            case MORE_EQUAL:
            case MORE_MORE:
            case MORE_MORE_EQUAL:
            case QUESTION:
            case CARET:
            case CARET_EQUAL:
            case BAR:
            case BAR_EQUAL:
            case BAR_BAR:
            case TILDE:
                return 0x804040;
        }
    }

    @Override
    protected int nextStateOrEnd(int currentState, char input) {
        switch (currentState) {
            default:
                throw new AssertionError("unhandled lexer state " + currentState + " for input " + input);
            case END:
            case ERROR:
            case NEWLINE:
            case SLASH_STAR___STAR_SLASH:
            case CHAR_CONSTANT_END:
            case STRING_LITERAL_END:
                // auto-generated by freditor.TokenGenerator
            case BANG_EQUAL:
            case PERCENT_EQUAL:
            case AMPERSAND_AMPERSAND:
            case AMPERSAND_EQUAL:
            case OPEN_PAREN:
            case CLOSE_PAREN:
            case ASTERISK_EQUAL:
            case PLUS_PLUS:
            case PLUS_EQUAL:
            case COMMA:
            case HYPHEN_HYPHEN:
            case HYPHEN_EQUAL:
            case HYPHEN_MORE:
            case DOT:
            case SLASH_EQUAL:
            case COLON:
            case SEMICOLON:
            case LESS_LESS_EQUAL:
            case LESS_EQUAL:
            case EQUAL_EQUAL:
            case MORE_EQUAL:
            case MORE_MORE_EQUAL:
            case QUESTION:
            case OPEN_BRACKET:
            case CLOSE_BRACKET:
            case CARET_EQUAL:
            case OPEN_BRACE:
            case BAR_EQUAL:
            case BAR_BAR:
            case CLOSE_BRACE:
            case TILDE:
                switch (input) {
                    default:
                        return ERROR;

                    case '\n':
                        return NEWLINE;
                    case ' ':
                        return FIRST_SPACE;
                    // case '/': return SLASH;

                    case '\'':
                        return CHAR_CONSTANT_BEGIN;
                    case '\"':
                        return STRING_LITERAL_BEGIN;

                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                        return FIRST_DIGIT;

                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'G':
                    case 'H':
                    case 'I':
                    case 'J':
                    case 'K':
                    case 'L':
                    case 'M':
                    case 'N':
                    case 'O':
                    case 'P':
                    case 'Q':
                    case 'R':
                    case 'S':
                    case 'T':
                    case 'U':
                    case 'V':
                    case 'W':
                    case 'X':
                    case 'Y':
                    case 'Z':
                    case 'h':
                    case 'j':
                    case 'k':
                    case 'm':
                    case 'n':
                    case 'o':
                    case 'p':
                    case 'q':
                    case 'x':
                    case 'y':
                    case 'z':
                    case '_':
                        return IDENTIFIER_FIRST;
// auto-generated by freditor.TokenGenerator
                    case 'a':
                        return A;
                    case 'b':
                        return B;
                    case 'c':
                        return C;
                    case 'd':
                        return D;
                    case 'e':
                        return E;
                    case 'f':
                        return F;
                    case 'g':
                        return G;
                    case 'i':
                        return I;
                    case 'l':
                        return L;
                    case 'r':
                        return R;
                    case 's':
                        return S;
                    case 't':
                        return T;
                    case 'u':
                        return U;
                    case 'v':
                        return V;
                    case 'w':
                        return W;
                    case '!':
                        return BANG;
                    case '%':
                        return PERCENT;
                    case '&':
                        return AMPERSAND;
                    case '(':
                        return OPEN_PAREN;
                    case ')':
                        return CLOSE_PAREN;
                    case '*':
                        return ASTERISK;
                    case '+':
                        return PLUS;
                    case ',':
                        return COMMA;
                    case '-':
                        return HYPHEN;
                    case '.':
                        return DOT;
                    case '/':
                        return SLASH;
                    case ':':
                        return COLON;
                    case ';':
                        return SEMICOLON;
                    case '<':
                        return LESS;
                    case '=':
                        return EQUAL;
                    case '>':
                        return MORE;
                    case '?':
                        return QUESTION;
                    case '[':
                        return OPEN_BRACKET;
                    case ']':
                        return CLOSE_BRACKET;
                    case '^':
                        return CARET;
                    case '{':
                        return OPEN_BRACE;
                    case '|':
                        return BAR;
                    case '}':
                        return CLOSE_BRACE;
                    case '~':
                        return TILDE;
                }
            case FIRST_SPACE:
            case NEXT_SPACE:
                switch (input) {
                    case ' ':
                        return NEXT_SPACE;
                    default:
                        return END;
                }
            case SLASH:
                switch (input) {
                    case '/':
                        return SLASH_SLASH;
                    case '*':
                        return SLASH_STAR;
                    case '=':
                        return SLASH_EQUAL;
                    default:
                        return END;
                }
            case SLASH_SLASH:
                switch (input) {
                    case '\n':
                        return END;
                    default:
                        return SLASH_SLASH;
                }
            case SLASH_STAR:
                switch (input) {
                    case '*':
                        return SLASH_STAR___STAR;
                    default:
                        return SLASH_STAR;
                }
            case SLASH_STAR___STAR:
                switch (input) {
                    case '*':
                        return SLASH_STAR___STAR;
                    case '/':
                        return SLASH_STAR___STAR_SLASH;
                    default:
                        return SLASH_STAR;
                }
            case CHAR_CONSTANT_BEGIN:
            case CHAR_CONSTANT_INSIDE:
                switch (input) {
                    case '\\':
                        return CHAR_CONSTANT_ESCAPE;
                    default:
                        return CHAR_CONSTANT_INSIDE;
                    case '\'':
                        return CHAR_CONSTANT_END;
                    case '\n':
                        return ERROR;
                }
            case CHAR_CONSTANT_ESCAPE:
                switch (input) {
                    default:
                        return CHAR_CONSTANT_INSIDE;
                    case '\n':
                        return ERROR;
                }
            case STRING_LITERAL_BEGIN:
            case STRING_LITERAL_INSIDE:
                switch (input) {
                    case '\\':
                        return STRING_LITERAL_ESCAPE;
                    default:
                        return STRING_LITERAL_INSIDE;
                    case '\"':
                        return STRING_LITERAL_END;
                    case '\n':
                        return ERROR;
                }
            case STRING_LITERAL_ESCAPE:
                switch (input) {
                    default:
                        return STRING_LITERAL_INSIDE;
                    case '\n':
                        return ERROR;
                }
            case FIRST_DIGIT:
            case NEXT_DIGIT:
                switch (input) {
                    case '0':
                    case '1':
                    case '2':
                    case '3':
                    case '4':
                    case '5':
                    case '6':
                    case '7':
                    case '8':
                    case '9':
                    case '.':
                    case 'X':
                    case 'x':
                    case 'A':
                    case 'a':
                    case 'B':
                    case 'b':
                    case 'C':
                    case 'c':
                    case 'D':
                    case 'd':
                    case 'E':
                    case 'e':
                    case 'F':
                    case 'f':
                        return NEXT_DIGIT;
                    default:
                        return END;
                }
            case IDENTIFIER_FIRST:
            case IDENTIFIER_NEXT:
                // auto-generated by freditor.TokenGenerator
            case ASSERT:
            case AUTO:
            case BREAK:
            case CASE:
            case CHAR:
            case CONST:
            case CONTINUE:
            case DEFAULT:
            case DOUBLE:
            case ELSE:
            case ENUM:
            case EXTERN:
            case FLOAT:
            case FOR:
            case GOTO:
            case IF:
            case INT:
            case LONG:
            case REGISTER:
            case RETURN:
            case SHORT:
            case SIGNED:
            case SIZEOF:
            case STATIC:
            case STRUCT:
            case SWITCH:
            case TYPEDEF:
            case UNION:
            case UNSIGNED:
            case VOID:
            case VOLATILE:
            case WHILE:
                return identifier(input);

// auto-generated by freditor.TokenGenerator
            case A:
                return keyword('s', AS, 'u', AU, input);
            case AS:
                return keyword('s', ASS, input);
            case ASS:
                return keyword('e', ASSE, input);
            case ASSE:
                return keyword('r', ASSER, input);
            case ASSER:
                return keyword('t', ASSERT, input);
            case AU:
                return keyword('t', AUT, input);
            case AUT:
                return keyword('o', AUTO, input);
            case B:
                return keyword('r', BR, input);
            case BR:
                return keyword('e', BRE, input);
            case BRE:
                return keyword('a', BREA, input);
            case BREA:
                return keyword('k', BREAK, input);
            case C:
                return keyword('a', CA, 'h', CH, 'o', CO, input);
            case CA:
                return keyword('s', CAS, input);
            case CAS:
                return keyword('e', CASE, input);
            case CH:
                return keyword('a', CHA, input);
            case CHA:
                return keyword('r', CHAR, input);
            case CO:
                return keyword('n', CON, input);
            case CON:
                return keyword('s', CONS, 't', CONT, input);
            case CONS:
                return keyword('t', CONST, input);
            case CONT:
                return keyword('i', CONTI, input);
            case CONTI:
                return keyword('n', CONTIN, input);
            case CONTIN:
                return keyword('u', CONTINU, input);
            case CONTINU:
                return keyword('e', CONTINUE, input);
            case D:
                return keyword('e', DE, 'o', DO, input);
            case DE:
                return keyword('f', DEF, input);
            case DEF:
                return keyword('a', DEFA, input);
            case DEFA:
                return keyword('u', DEFAU, input);
            case DEFAU:
                return keyword('l', DEFAUL, input);
            case DEFAUL:
                return keyword('t', DEFAULT, input);
            case DO:
                return keyword('u', DOU, input);
            case DOU:
                return keyword('b', DOUB, input);
            case DOUB:
                return keyword('l', DOUBL, input);
            case DOUBL:
                return keyword('e', DOUBLE, input);
            case E:
                return keyword('l', EL, 'n', EN, 'x', EX, input);
            case EL:
                return keyword('s', ELS, input);
            case ELS:
                return keyword('e', ELSE, input);
            case EN:
                return keyword('u', ENU, input);
            case ENU:
                return keyword('m', ENUM, input);
            case EX:
                return keyword('t', EXT, input);
            case EXT:
                return keyword('e', EXTE, input);
            case EXTE:
                return keyword('r', EXTER, input);
            case EXTER:
                return keyword('n', EXTERN, input);
            case F:
                return keyword('l', FL, 'o', FO, input);
            case FL:
                return keyword('o', FLO, input);
            case FLO:
                return keyword('a', FLOA, input);
            case FLOA:
                return keyword('t', FLOAT, input);
            case FO:
                return keyword('r', FOR, input);
            case G:
                return keyword('o', GO, input);
            case GO:
                return keyword('t', GOT, input);
            case GOT:
                return keyword('o', GOTO, input);
            case I:
                return keyword('f', IF, 'n', IN, input);
            case IN:
                return keyword('t', INT, input);
            case L:
                return keyword('o', LO, input);
            case LO:
                return keyword('n', LON, input);
            case LON:
                return keyword('g', LONG, input);
            case R:
                return keyword('e', RE, input);
            case RE:
                return keyword('g', REG, 't', RET, input);
            case REG:
                return keyword('i', REGI, input);
            case REGI:
                return keyword('s', REGIS, input);
            case REGIS:
                return keyword('t', REGIST, input);
            case REGIST:
                return keyword('e', REGISTE, input);
            case REGISTE:
                return keyword('r', REGISTER, input);
            case RET:
                return keyword('u', RETU, input);
            case RETU:
                return keyword('r', RETUR, input);
            case RETUR:
                return keyword('n', RETURN, input);
            case S:
                return keyword('h', SH, 'i', SI, 't', ST, 'w', SW, input);
            case SH:
                return keyword('o', SHO, input);
            case SHO:
                return keyword('r', SHOR, input);
            case SHOR:
                return keyword('t', SHORT, input);
            case SI:
                return keyword('g', SIG, 'z', SIZ, input);
            case SIG:
                return keyword('n', SIGN, input);
            case SIGN:
                return keyword('e', SIGNE, input);
            case SIGNE:
                return keyword('d', SIGNED, input);
            case SIZ:
                return keyword('e', SIZE, input);
            case SIZE:
                return keyword('o', SIZEO, input);
            case SIZEO:
                return keyword('f', SIZEOF, input);
            case ST:
                return keyword('a', STA, 'r', STR, input);
            case STA:
                return keyword('t', STAT, input);
            case STAT:
                return keyword('i', STATI, input);
            case STATI:
                return keyword('c', STATIC, input);
            case STR:
                return keyword('u', STRU, input);
            case STRU:
                return keyword('c', STRUC, input);
            case STRUC:
                return keyword('t', STRUCT, input);
            case SW:
                return keyword('i', SWI, input);
            case SWI:
                return keyword('t', SWIT, input);
            case SWIT:
                return keyword('c', SWITC, input);
            case SWITC:
                return keyword('h', SWITCH, input);
            case T:
                return keyword('y', TY, input);
            case TY:
                return keyword('p', TYP, input);
            case TYP:
                return keyword('e', TYPE, input);
            case TYPE:
                return keyword('d', TYPED, input);
            case TYPED:
                return keyword('e', TYPEDE, input);
            case TYPEDE:
                return keyword('f', TYPEDEF, input);
            case U:
                return keyword('n', UN, input);
            case UN:
                return keyword('i', UNI, 's', UNS, input);
            case UNI:
                return keyword('o', UNIO, input);
            case UNIO:
                return keyword('n', UNION, input);
            case UNS:
                return keyword('i', UNSI, input);
            case UNSI:
                return keyword('g', UNSIG, input);
            case UNSIG:
                return keyword('n', UNSIGN, input);
            case UNSIGN:
                return keyword('e', UNSIGNE, input);
            case UNSIGNE:
                return keyword('d', UNSIGNED, input);
            case V:
                return keyword('o', VO, input);
            case VO:
                return keyword('i', VOI, 'l', VOL, input);
            case VOI:
                return keyword('d', VOID, input);
            case VOL:
                return keyword('a', VOLA, input);
            case VOLA:
                return keyword('t', VOLAT, input);
            case VOLAT:
                return keyword('i', VOLATI, input);
            case VOLATI:
                return keyword('l', VOLATIL, input);
            case VOLATIL:
                return keyword('e', VOLATILE, input);
            case W:
                return keyword('h', WH, input);
            case WH:
                return keyword('i', WHI, input);
            case WHI:
                return keyword('l', WHIL, input);
            case WHIL:
                return keyword('e', WHILE, input);
            // auto-generated by freditor.TokenGenerator
            case BANG:
                return operator('=', BANG_EQUAL, input);
            case PERCENT:
                return operator('=', PERCENT_EQUAL, input);
            case AMPERSAND:
                return operator('&', AMPERSAND_AMPERSAND, '=', AMPERSAND_EQUAL, input);
            case ASTERISK:
                return operator('=', ASTERISK_EQUAL, input);
            case PLUS:
                return operator('+', PLUS_PLUS, '=', PLUS_EQUAL, input);
            case HYPHEN:
                return operator('-', HYPHEN_HYPHEN, '=', HYPHEN_EQUAL, '>', HYPHEN_MORE, input);
            // case SLASH: return operator('=', SLASH_EQUAL, input);
            case LESS:
                return operator('<', LESS_LESS, '=', LESS_EQUAL, input);
            case LESS_LESS:
                return operator('=', LESS_LESS_EQUAL, input);
            case EQUAL:
                return operator('=', EQUAL_EQUAL, input);
            case MORE:
                return operator('=', MORE_EQUAL, '>', MORE_MORE, input);
            case MORE_MORE:
                return operator('=', MORE_MORE_EQUAL, input);
            case CARET:
                return operator('=', CARET_EQUAL, input);
            case BAR:
                return operator('=', BAR_EQUAL, '|', BAR_BAR, input);
        }
    }

    @Override
    protected int identifier(char input) {
        switch (input) {
            case 'A':
            case 'B':
            case 'C':
            case 'D':
            case 'E':
            case 'F':
            case 'G':
            case 'H':
            case 'I':
            case 'J':
            case 'K':
            case 'L':
            case 'M':
            case 'N':
            case 'O':
            case 'P':
            case 'Q':
            case 'R':
            case 'S':
            case 'T':
            case 'U':
            case 'V':
            case 'W':
            case 'X':
            case 'Y':
            case 'Z':
            case 'a':
            case 'b':
            case 'c':
            case 'd':
            case 'e':
            case 'f':
            case 'g':
            case 'h':
            case 'i':
            case 'j':
            case 'k':
            case 'l':
            case 'm':
            case 'n':
            case 'o':
            case 'p':
            case 'q':
            case 'r':
            case 's':
            case 't':
            case 'u':
            case 'v':
            case 'w':
            case 'x':
            case 'y':
            case 'z':
            case '_':
            case '0':
            case '1':
            case '2':
            case '3':
            case '4':
            case '5':
            case '6':
            case '7':
            case '8':
            case '9':
                return IDENTIFIER_NEXT;
            default:
                return END;
        }
    }
}
