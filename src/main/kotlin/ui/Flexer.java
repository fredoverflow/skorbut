package ui;

public class Flexer extends freditor.Flexer {
    public static final Flexer instance = new Flexer();

    // public static final int SLASH = -10;
    public static final int SLASH_SLASH = 2;
    public static final int SLASH_ASTERISK = 3;
    public static final int SLASH_ASTERISK___ASTERISK = 4;
    public static final int SLASH_ASTERISK___ASTERISK_SLASH = 5;

    public static final int CHAR_CONSTANT_BEGIN = -10;
    public static final int CHAR_CONSTANT_ESCAPE = 6;
    public static final int CHAR_CONSTANT_INSIDE = 7;
    public static final int CHAR_CONSTANT_END = 8;

    public static final int STRING_LITERAL_BEGIN = -11;
    public static final int STRING_LITERAL_ESCAPE = 9;
    public static final int STRING_LITERAL_INSIDE = 10;
    public static final int STRING_LITERAL_END = 11;

    public static final int FIRST_DIGIT = -12;
    public static final int NEXT_DIGIT = 12;

    public static final int IDENTIFIER_FIRST = -13;
    public static final int IDENTIFIER_NEXT = 13;

    // auto-generated by freditor.FlexerGenerator
    public static final int A = -14;
    public static final int AS = 14;
    public static final int ASS = 15;
    public static final int ASSE = 16;
    public static final int ASSER = 17;
    public static final int ASSERT = 18;
    public static final int AU = 19;
    public static final int AUT = 20;
    public static final int AUTO = 21;
    public static final int B = -15;
    public static final int BR = 22;
    public static final int BRE = 23;
    public static final int BREA = 24;
    public static final int BREAK = 25;
    public static final int C = -16;
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
    public static final int D = -17;
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
    public static final int E = -18;
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
    public static final int F = -19;
    public static final int FL = 63;
    public static final int FLO = 64;
    public static final int FLOA = 65;
    public static final int FLOAT = 66;
    public static final int FO = 67;
    public static final int FOR = 68;
    public static final int G = -20;
    public static final int GO = 69;
    public static final int GOT = 70;
    public static final int GOTO = 71;
    public static final int I = -21;
    public static final int IF = 72;
    public static final int IN = 73;
    public static final int INT = 74;
    public static final int L = -22;
    public static final int LO = 75;
    public static final int LON = 76;
    public static final int LONG = 77;
    public static final int R = -23;
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
    public static final int S = -24;
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
    public static final int T = -25;
    public static final int TY = 116;
    public static final int TYP = 117;
    public static final int TYPE = 118;
    public static final int TYPED = 119;
    public static final int TYPEDE = 120;
    public static final int TYPEDEF = 121;
    public static final int U = -26;
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
    public static final int V = -27;
    public static final int VO = 132;
    public static final int VOI = 133;
    public static final int VOID = 134;
    public static final int VOL = 135;
    public static final int VOLA = 136;
    public static final int VOLAT = 137;
    public static final int VOLATI = 138;
    public static final int VOLATIL = 139;
    public static final int VOLATILE = 140;
    public static final int W = -28;
    public static final int WH = 141;
    public static final int WHI = 142;
    public static final int WHIL = 143;
    public static final int WHILE = 144;
    public static final int BANG = -29;
    public static final int BANG_EQUAL = 145;
    public static final int PERCENT = -30;
    public static final int PERCENT_EQUAL = 146;
    public static final int AMPERSAND = -31;
    public static final int AMPERSAND_AMPERSAND = 147;
    public static final int AMPERSAND_EQUAL = 148;
    public static final int ASTERISK = -32;
    public static final int ASTERISK_EQUAL = 149;
    public static final int PLUS = -33;
    public static final int PLUS_PLUS = 150;
    public static final int PLUS_EQUAL = 151;
    public static final int COMMA = -34;
    public static final int HYPHEN = -35;
    public static final int HYPHEN_HYPHEN = 152;
    public static final int HYPHEN_EQUAL = 153;
    public static final int HYPHEN_MORE = 154;
    public static final int DOT = -36;
    public static final int SLASH = -37;
    public static final int SLASH_EQUAL = 155;
    public static final int COLON = -38;
    public static final int SEMICOLON = -39;
    public static final int LESS = -40;
    public static final int LESS_LESS = 156;
    public static final int LESS_LESS_EQUAL = 157;
    public static final int LESS_EQUAL = 158;
    public static final int EQUAL = -41;
    public static final int EQUAL_EQUAL = 159;
    public static final int MORE = -42;
    public static final int MORE_EQUAL = 160;
    public static final int MORE_MORE = 161;
    public static final int MORE_MORE_EQUAL = 162;
    public static final int QUESTION = -43;
    public static final int CARET = -44;
    public static final int CARET_EQUAL = 163;
    public static final int BAR = -45;
    public static final int BAR_EQUAL = 164;
    public static final int BAR_BAR = 165;
    public static final int TILDE = -46;

    @Override
    public int pickColorForLexeme(int previousState, char firstCharacter, int endState) {
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
            case SLASH_ASTERISK:
            case SLASH_ASTERISK___ASTERISK:
            case SLASH_ASTERISK___ASTERISK_SLASH:
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
            case DO:
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

            case OPENING_PAREN:
            case CLOSING_PAREN:
            case OPENING_BRACKET:
            case CLOSING_BRACKET:
            case OPENING_BRACE:
            case CLOSING_BRACE:
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
            case SLASH_ASTERISK___ASTERISK_SLASH:
            case CHAR_CONSTANT_END:
            case STRING_LITERAL_END:
                // auto-generated by freditor.FlexerGenerator
            case BANG_EQUAL:
            case PERCENT_EQUAL:
            case AMPERSAND_AMPERSAND:
            case AMPERSAND_EQUAL:
            case OPENING_PAREN:
            case CLOSING_PAREN:
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
            case OPENING_BRACKET:
            case CLOSING_BRACKET:
            case CARET_EQUAL:
            case OPENING_BRACE:
            case BAR_EQUAL:
            case BAR_BAR:
            case CLOSING_BRACE:
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

                    case '_':

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
                        return IDENTIFIER_FIRST;
                    // auto-generated by freditor.FlexerGenerator
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
                        return OPENING_PAREN;
                    case ')':
                        return CLOSING_PAREN;
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
                        return OPENING_BRACKET;
                    case ']':
                        return CLOSING_BRACKET;
                    case '^':
                        return CARET;
                    case '{':
                        return OPENING_BRACE;
                    case '|':
                        return BAR;
                    case '}':
                        return CLOSING_BRACE;
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
                        return SLASH_ASTERISK;
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
            case SLASH_ASTERISK:
                switch (input) {
                    case '*':
                        return SLASH_ASTERISK___ASTERISK;
                    default:
                        return SLASH_ASTERISK;
                }
            case SLASH_ASTERISK___ASTERISK:
                switch (input) {
                    case '*':
                        return SLASH_ASTERISK___ASTERISK;
                    case '/':
                        return SLASH_ASTERISK___ASTERISK_SLASH;
                    default:
                        return SLASH_ASTERISK;
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
                    case '.':

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

                    case 'A':
                    case 'B':
                    case 'C':
                    case 'D':
                    case 'E':
                    case 'F':
                    case 'X':

                    case 'a':
                    case 'b':
                    case 'c':
                    case 'd':
                    case 'e':
                    case 'f':
                    case 'x':
                        return NEXT_DIGIT;

                    default:
                        return END;
                }
            case IDENTIFIER_FIRST:
            case IDENTIFIER_NEXT:
                // auto-generated by freditor.FlexerGenerator
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

            // auto-generated by freditor.FlexerGenerator
            case A:
                return input == 's' ? AS : input == 'u' ? AU : identifier(input);
            case AS:
                return input == 's' ? ASS : identifier(input);
            case ASS:
                return input == 'e' ? ASSE : identifier(input);
            case ASSE:
                return input == 'r' ? ASSER : identifier(input);
            case ASSER:
                return input == 't' ? ASSERT : identifier(input);
            case AU:
                return input == 't' ? AUT : identifier(input);
            case AUT:
                return input == 'o' ? AUTO : identifier(input);
            case B:
                return input == 'r' ? BR : identifier(input);
            case BR:
                return input == 'e' ? BRE : identifier(input);
            case BRE:
                return input == 'a' ? BREA : identifier(input);
            case BREA:
                return input == 'k' ? BREAK : identifier(input);
            case C:
                return input == 'a' ? CA : input == 'h' ? CH : input == 'o' ? CO : identifier(input);
            case CA:
                return input == 's' ? CAS : identifier(input);
            case CAS:
                return input == 'e' ? CASE : identifier(input);
            case CH:
                return input == 'a' ? CHA : identifier(input);
            case CHA:
                return input == 'r' ? CHAR : identifier(input);
            case CO:
                return input == 'n' ? CON : identifier(input);
            case CON:
                return input == 's' ? CONS : input == 't' ? CONT : identifier(input);
            case CONS:
                return input == 't' ? CONST : identifier(input);
            case CONT:
                return input == 'i' ? CONTI : identifier(input);
            case CONTI:
                return input == 'n' ? CONTIN : identifier(input);
            case CONTIN:
                return input == 'u' ? CONTINU : identifier(input);
            case CONTINU:
                return input == 'e' ? CONTINUE : identifier(input);
            case D:
                return input == 'e' ? DE : input == 'o' ? DO : identifier(input);
            case DE:
                return input == 'f' ? DEF : identifier(input);
            case DEF:
                return input == 'a' ? DEFA : identifier(input);
            case DEFA:
                return input == 'u' ? DEFAU : identifier(input);
            case DEFAU:
                return input == 'l' ? DEFAUL : identifier(input);
            case DEFAUL:
                return input == 't' ? DEFAULT : identifier(input);
            case DO:
                return input == 'u' ? DOU : identifier(input);
            case DOU:
                return input == 'b' ? DOUB : identifier(input);
            case DOUB:
                return input == 'l' ? DOUBL : identifier(input);
            case DOUBL:
                return input == 'e' ? DOUBLE : identifier(input);
            case E:
                return input == 'l' ? EL : input == 'n' ? EN : input == 'x' ? EX : identifier(input);
            case EL:
                return input == 's' ? ELS : identifier(input);
            case ELS:
                return input == 'e' ? ELSE : identifier(input);
            case EN:
                return input == 'u' ? ENU : identifier(input);
            case ENU:
                return input == 'm' ? ENUM : identifier(input);
            case EX:
                return input == 't' ? EXT : identifier(input);
            case EXT:
                return input == 'e' ? EXTE : identifier(input);
            case EXTE:
                return input == 'r' ? EXTER : identifier(input);
            case EXTER:
                return input == 'n' ? EXTERN : identifier(input);
            case F:
                return input == 'l' ? FL : input == 'o' ? FO : identifier(input);
            case FL:
                return input == 'o' ? FLO : identifier(input);
            case FLO:
                return input == 'a' ? FLOA : identifier(input);
            case FLOA:
                return input == 't' ? FLOAT : identifier(input);
            case FO:
                return input == 'r' ? FOR : identifier(input);
            case G:
                return input == 'o' ? GO : identifier(input);
            case GO:
                return input == 't' ? GOT : identifier(input);
            case GOT:
                return input == 'o' ? GOTO : identifier(input);
            case I:
                return input == 'f' ? IF : input == 'n' ? IN : identifier(input);
            case IN:
                return input == 't' ? INT : identifier(input);
            case L:
                return input == 'o' ? LO : identifier(input);
            case LO:
                return input == 'n' ? LON : identifier(input);
            case LON:
                return input == 'g' ? LONG : identifier(input);
            case R:
                return input == 'e' ? RE : identifier(input);
            case RE:
                return input == 'g' ? REG : input == 't' ? RET : identifier(input);
            case REG:
                return input == 'i' ? REGI : identifier(input);
            case REGI:
                return input == 's' ? REGIS : identifier(input);
            case REGIS:
                return input == 't' ? REGIST : identifier(input);
            case REGIST:
                return input == 'e' ? REGISTE : identifier(input);
            case REGISTE:
                return input == 'r' ? REGISTER : identifier(input);
            case RET:
                return input == 'u' ? RETU : identifier(input);
            case RETU:
                return input == 'r' ? RETUR : identifier(input);
            case RETUR:
                return input == 'n' ? RETURN : identifier(input);
            case S:
                return input == 'h' ? SH : input == 'i' ? SI : input == 't' ? ST : input == 'w' ? SW : identifier(input);
            case SH:
                return input == 'o' ? SHO : identifier(input);
            case SHO:
                return input == 'r' ? SHOR : identifier(input);
            case SHOR:
                return input == 't' ? SHORT : identifier(input);
            case SI:
                return input == 'g' ? SIG : input == 'z' ? SIZ : identifier(input);
            case SIG:
                return input == 'n' ? SIGN : identifier(input);
            case SIGN:
                return input == 'e' ? SIGNE : identifier(input);
            case SIGNE:
                return input == 'd' ? SIGNED : identifier(input);
            case SIZ:
                return input == 'e' ? SIZE : identifier(input);
            case SIZE:
                return input == 'o' ? SIZEO : identifier(input);
            case SIZEO:
                return input == 'f' ? SIZEOF : identifier(input);
            case ST:
                return input == 'a' ? STA : input == 'r' ? STR : identifier(input);
            case STA:
                return input == 't' ? STAT : identifier(input);
            case STAT:
                return input == 'i' ? STATI : identifier(input);
            case STATI:
                return input == 'c' ? STATIC : identifier(input);
            case STR:
                return input == 'u' ? STRU : identifier(input);
            case STRU:
                return input == 'c' ? STRUC : identifier(input);
            case STRUC:
                return input == 't' ? STRUCT : identifier(input);
            case SW:
                return input == 'i' ? SWI : identifier(input);
            case SWI:
                return input == 't' ? SWIT : identifier(input);
            case SWIT:
                return input == 'c' ? SWITC : identifier(input);
            case SWITC:
                return input == 'h' ? SWITCH : identifier(input);
            case T:
                return input == 'y' ? TY : identifier(input);
            case TY:
                return input == 'p' ? TYP : identifier(input);
            case TYP:
                return input == 'e' ? TYPE : identifier(input);
            case TYPE:
                return input == 'd' ? TYPED : identifier(input);
            case TYPED:
                return input == 'e' ? TYPEDE : identifier(input);
            case TYPEDE:
                return input == 'f' ? TYPEDEF : identifier(input);
            case U:
                return input == 'n' ? UN : identifier(input);
            case UN:
                return input == 'i' ? UNI : input == 's' ? UNS : identifier(input);
            case UNI:
                return input == 'o' ? UNIO : identifier(input);
            case UNIO:
                return input == 'n' ? UNION : identifier(input);
            case UNS:
                return input == 'i' ? UNSI : identifier(input);
            case UNSI:
                return input == 'g' ? UNSIG : identifier(input);
            case UNSIG:
                return input == 'n' ? UNSIGN : identifier(input);
            case UNSIGN:
                return input == 'e' ? UNSIGNE : identifier(input);
            case UNSIGNE:
                return input == 'd' ? UNSIGNED : identifier(input);
            case V:
                return input == 'o' ? VO : identifier(input);
            case VO:
                return input == 'i' ? VOI : input == 'l' ? VOL : identifier(input);
            case VOI:
                return input == 'd' ? VOID : identifier(input);
            case VOL:
                return input == 'a' ? VOLA : identifier(input);
            case VOLA:
                return input == 't' ? VOLAT : identifier(input);
            case VOLAT:
                return input == 'i' ? VOLATI : identifier(input);
            case VOLATI:
                return input == 'l' ? VOLATIL : identifier(input);
            case VOLATIL:
                return input == 'e' ? VOLATILE : identifier(input);
            case W:
                return input == 'h' ? WH : identifier(input);
            case WH:
                return input == 'i' ? WHI : identifier(input);
            case WHI:
                return input == 'l' ? WHIL : identifier(input);
            case WHIL:
                return input == 'e' ? WHILE : identifier(input);

            // auto-generated by freditor.FlexerGenerator
            case BANG:
                return input == '=' ? BANG_EQUAL : END;
            case PERCENT:
                return input == '=' ? PERCENT_EQUAL : END;
            case AMPERSAND:
                return input == '&' ? AMPERSAND_AMPERSAND : input == '=' ? AMPERSAND_EQUAL : END;
            case ASTERISK:
                return input == '=' ? ASTERISK_EQUAL : END;
            case PLUS:
                return input == '+' ? PLUS_PLUS : input == '=' ? PLUS_EQUAL : END;
            case HYPHEN:
                return input == '-' ? HYPHEN_HYPHEN : input == '=' ? HYPHEN_EQUAL : input == '>' ? HYPHEN_MORE : END;
            // case SLASH: return input == '=' ? SLASH_EQUAL : END;
            case LESS:
                return input == '<' ? LESS_LESS : input == '=' ? LESS_EQUAL : END;
            case LESS_LESS:
                return input == '=' ? LESS_LESS_EQUAL : END;
            case EQUAL:
                return input == '=' ? EQUAL_EQUAL : END;
            case MORE:
                return input == '=' ? MORE_EQUAL : input == '>' ? MORE_MORE : END;
            case MORE_MORE:
                return input == '=' ? MORE_MORE_EQUAL : END;
            case CARET:
                return input == '=' ? CARET_EQUAL : END;
            case BAR:
                return input == '=' ? BAR_EQUAL : input == '|' ? BAR_BAR : END;
        }
    }

    private int identifier(char input) {
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

            case '_':

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
                return IDENTIFIER_NEXT;

            default:
                return END;
        }
    }
}
