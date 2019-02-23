package syntax.lexer

import common.Diagnostic
import freditor.persistent.ChampMap

class Token(val kind: Byte, val start: Int, val source: String, val text: String) {
    val end: Int
        get() = start + source.length

    fun withTokenKind(replacement: Byte): Token = Token(replacement, start, source, text)

    fun tagged(): Token = Token(kind, start, source, "#$text".intern())

    fun wasProvided(): Boolean = kind == IDENTIFIER

    fun error(description: String): Nothing = throw Diagnostic(start, description)

    override fun toString(): String = source
}

val lexemePool = arrayOf("assert", "auto", "break", "case", "char", "const", "continue", "default", "do",
        "double", "else", "enum", "extern", "float", "for", "goto", "if", "int", "long", "register", "return", "short",
        "signed", "sizeof", "static", "struct", "switch", "typedef", "union", "unsigned", "void", "volatile", "while",
        // keywords come first
        "[", "]", "(", ")", ".", "->", "++", "--", "&", "*", "+", "-", "~", "!", "/", "%", "<<", ">>",
        "<", ">", "<=", ">=", "==", "!=", "^", "|", "&&", "||", "?", ":",
        "=", "*=", "/=", "%=", "+=", "-=", "<<=", ">>=", "&=", "^=", "|=", ",", "{", "}", ";",
        "double constant", "float constant", "integer constant", "character constant",
        "string literal", "printf", "identifier", "scanf", "end of input", "\$anon")

const val NUM_KEYWORDS = 33

val keywords: ChampMap<String, Byte> = lexemePool.take(NUM_KEYWORDS).foldIndexed(ChampMap.empty()) { index, map, keyword ->
    map.put(keyword, index.toByte())
}

fun Byte.show(): String = lexemePool[+this]

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

const val ALL_SPECIFIERS = 0xfb7e3a32.toInt()
const val STORAGE_CLASS = 0x09081002
const val PRIMITIVE = 0x60662210
const val TYPE_SPECIFIERS = 0x72762a10
const val CONST_QUALIFIER = 0x00000020
const val VOLATILE_QUALIFIER = 0x80000000.toInt()
const val CV_QUALIFIER = CONST_QUALIFIER or VOLATILE_QUALIFIER

const val OPENING_BRACKET: Byte = 33
const val CLOSING_BRACKET: Byte = 34
const val OPENING_PAREN: Byte = 35
const val CLOSING_PAREN: Byte = 36
const val DOT: Byte = 37
const val HYPHEN_MORE: Byte = 38

const val PLUS_PLUS: Byte = 39
const val HYPHEN_HYPHEN: Byte = 40
const val AMPERSAND: Byte = 41
const val ASTERISK: Byte = 42
const val PLUS: Byte = 43
const val HYPHEN: Byte = 44
const val TILDE: Byte = 45
const val BANG: Byte = 46

const val SLASH: Byte = 47
const val PERCENT: Byte = 48
const val LESS_LESS: Byte = 49
const val MORE_MORE: Byte = 50
const val LESS: Byte = 51
const val MORE: Byte = 52
const val LESS_EQUAL: Byte = 53
const val MORE_EQUAL: Byte = 54
const val EQUAL_EQUAL: Byte = 55
const val BANG_EQUAL: Byte = 56
const val CARET: Byte = 57
const val BAR: Byte = 58
const val AMPERSAND_AMPERSAND: Byte = 59
const val BAR_BAR: Byte = 60

const val QUESTION: Byte = 61
const val COLON: Byte = 62

const val EQUAL: Byte = 63
const val ASTERISK_EQUAL: Byte = 64
const val SLASH_EQUAL: Byte = 65
const val PERCENT_EQUAL: Byte = 66
const val PLUS_EQUAL: Byte = 67
const val HYPHEN_EQUAL: Byte = 68
const val LESS_LESS_EQUAL: Byte = 69
const val MORE_MORE_EQUAL: Byte = 70
const val AMPERSAND_EQUAL: Byte = 71
const val CARET_EQUAL: Byte = 72
const val BAR_EQUAL: Byte = 73

const val COMMA: Byte = 74

const val OPENING_BRACE: Byte = 75
const val CLOSING_BRACE: Byte = 76
const val SEMICOLON: Byte = 77

const val DOUBLE_CONSTANT: Byte = 78
const val FLOAT_CONSTANT: Byte = 79
const val INTEGER_CONSTANT: Byte = 80

const val CHARACTER_CONSTANT: Byte = 81
const val STRING_LITERAL: Byte = 82

const val PRINTF: Byte = 83
const val IDENTIFIER: Byte = 84
const val SCANF: Byte = 85

const val END_OF_INPUT: Byte = 86

fun fakeIdentifier(name: String) = Token(IDENTIFIER, Int.MIN_VALUE, name, name)

val missingIdentifier = fakeIdentifier("")
