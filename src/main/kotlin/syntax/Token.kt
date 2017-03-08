package syntax

import common.Diagnostic

class Token(val kind: Byte, val start: Int, val source: String, val text: String) {
    fun end(): Int = start + source.length

    fun withTokenKind(replacement: Byte): Token = Token(replacement, start, source, text)

    fun tagged(): Token = Token(kind, start, source, "#$text".intern())

    fun wasProvided(): Boolean = kind == IDENTIFIER

    fun error(description: String): Nothing = throw Diagnostic(start, description)

    override fun toString(): String = source
}

val TOKENS = arrayOf("assert", "auto", "break", "case", "char", "const", "continue", "default", "do",
        "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short",
        "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while",
        "[", "]", "(", ")", ".", "->", "++", "--", "&", "*", "+", "-", "~", "!", "/", "%", "<<", ">>",
        "<", ">", "<=", ">=", "==", "!=", "^", "|", "&&", "||", "?", ":",
        "=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", "&=", "^=", "|=", ",", "{", "}", ";",
        "double constant", "float constant", "integer constant", "character constant",
        "string literal", "identifier", "printf", "scanf", "end of file", "\$anon")

fun Byte.show(): String = TOKENS[this.toInt()]

const val ASSERT: Byte = 0
const val AUTO: Byte = 1
const val BREAK: Byte = 2
const val CASE: Byte = 3
const val CHAR: Byte = 4
const val CONST: Byte = 5
const val CONTINUE: Byte = 6
const val DEFAULT: Byte = 7
const val DO: Byte = 8
const val DOUBLE: Byte = 9
const val ELSE: Byte = 10
const val ENUM: Byte = 11
const val EXTERN: Byte = 12
const val FLOAT: Byte = 13
const val FOR: Byte = 14
const val GOTO: Byte = 15
const val IF: Byte = 16
const val INT: Byte = 17
const val LONG: Byte = 18
const val REGISTER: Byte = 19
const val RETURN: Byte = 20
const val SHORT: Byte = 21
const val SIGNED: Byte = 22
const val SIZEOF: Byte = 23
const val STATIC: Byte = 24
const val STRUCT: Byte = 25
const val SWITCH: Byte = 26
const val TYPEDEF: Byte = 27
const val UNION: Byte = 28
const val UNSIGNED: Byte = 29
const val VOID: Byte = 30
const val VOLATILE: Byte = 31
const val WHILE: Byte = 32

const val OPEN_BRACKET: Byte = 33
const val CLOSE_BRACKET: Byte = 34
const val OPEN_PAREN: Byte = 35
const val CLOSE_PAREN: Byte = 36
const val DOT: Byte = 37
const val ARROW: Byte = 38

const val PLUS_PLUS: Byte = 39
const val MINUS_MINUS: Byte = 40
const val AMP: Byte = 41
const val STAR: Byte = 42
const val PLUS: Byte = 43
const val MINUS: Byte = 44
const val TILDE: Byte = 45
const val BANG: Byte = 46

const val SLASH: Byte = 47
const val PERCENT: Byte = 48
const val LESS_LESS: Byte = 49
const val MORE_MORE: Byte = 50
const val LESS: Byte = 51
const val MORE: Byte = 52
const val LESS_EQ: Byte = 53
const val MORE_EQ: Byte = 54
const val EQ_EQ: Byte = 55
const val BANG_EQ: Byte = 56
const val CARET: Byte = 57
const val PIPE: Byte = 58
const val AMP_AMP: Byte = 59
const val PIPE_PIPE: Byte = 60

const val QUESTION: Byte = 61
const val COLON: Byte = 62

const val EQ: Byte = 63
const val STAR_EQ: Byte = 64
const val SLASH_EQ: Byte = 65
const val PERCENT_EQ: Byte = 66
const val PLUS_EQ: Byte = 67
const val MINUS_EQ: Byte = 68
const val LESS_LESS_EQ: Byte = 69
const val MORE_MORE_EQ: Byte = 70
const val AMP_EQ: Byte = 71
const val CARET_EQ: Byte = 72
const val PIPE_EQ: Byte = 73

const val COMMA: Byte = 74

const val OPEN_BRACE: Byte = 75
const val CLOSE_BRACE: Byte = 76
const val SEMICOLON: Byte = 77

const val DOUBLE_CONSTANT: Byte = 78
const val FLOAT_CONSTANT: Byte = 79
const val INTEGER_CONSTANT: Byte = 80

const val CHARACTER_CONSTANT: Byte = 81
const val STRING_LITERAL: Byte = 82

const val PRINTF: Byte = 83
const val IDENTIFIER: Byte = 84
const val SCANF: Byte = 85

const val EOF: Byte = 86

fun fakeIdentifier(name: String) = Token(IDENTIFIER, Int.MIN_VALUE, name, name)

val missingIdentifier = fakeIdentifier("")
