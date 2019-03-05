package syntax.parser

import syntax.lexer.TokenKind
import syntax.lexer.TokenKind.*
import syntax.tree.*

const val PRECEDENCE_POSTFIX = 150
const val PRECEDENCE_PREFIX = 140
const val PRECEDENCE_COMMA = 10

fun Parser.condition(): Expression {
    return parenthesized { expression() }
}

@Suppress("NOTHING_TO_INLINE")
inline fun Parser.expression(): Expression {
    return subexpression(outerPrecedence = 0)
}

@Suppress("NOTHING_TO_INLINE")
inline fun Parser.assignmentExpression(): Expression {
    return subexpression(outerPrecedence = PRECEDENCE_COMMA)
}

fun Parser.subexpression(outerPrecedence: Int): Expression {
    val nullDenotation = nullDenotations[current.ordinal] ?: illegalStartOf("expression")

    return subexpression(with(nullDenotation) { parse(accept()) }, outerPrecedence)
}

tailrec fun Parser.subexpression(left: Expression, outerPrecedence: Int): Expression {
    val leftDenotation = leftDenotations[current.ordinal] ?: return left
    if (leftDenotation.precedence <= outerPrecedence) return left

    return subexpression(with(leftDenotation) { parse(left, accept()) }, outerPrecedence)
}

private val nullDenotations = arrayOfNulls<NullDenotation>(128).apply {
    this[IDENTIFIER] = IdentifierDenotation
    this[DOUBLE_CONSTANT, FLOAT_CONSTANT, INTEGER_CONSTANT, CHARACTER_CONSTANT] = ConstantDenotation
    this[STRING_LITERAL] = StringLiteralDenotation
    this[OPENING_PAREN] = PossibleCastDenotation
    this[PLUS_PLUS, HYPHEN_HYPHEN, AMPERSAND, ASTERISK, PLUS, HYPHEN, TILDE, BANG] = PrefixDenotation
    this[SIZEOF] = SizeofDenotation
}

private val leftDenotations = arrayOfNulls<LeftDenotation>(128).apply {
    this[OPENING_BRACKET] = SubscriptDenotation
    this[OPENING_PAREN] = FunctionCallDenotation
    this[DOT] = DirectMemberDenotation
    this[HYPHEN_MORE] = IndirectMemberDenotation
    this[PLUS_PLUS, HYPHEN_HYPHEN] = PostfixCrementDenotation

    this[ASTERISK, SLASH, PERCENT] = LeftAssociativeDenotation(130, ::Multiplicative)
    this[PLUS] = LeftAssociativeDenotation(120, ::Plus)
    this[HYPHEN] = LeftAssociativeDenotation(120, ::Minus)
    this[LESS_LESS, MORE_MORE] = LeftAssociativeDenotation(110, ::Shift)
    this[LESS, MORE, LESS_EQUAL, MORE_EQUAL] = LeftAssociativeDenotation(100, ::RelationalEquality)
    this[EQUAL_EQUAL, BANG_EQUAL] = LeftAssociativeDenotation(90, ::RelationalEquality)
    this[AMPERSAND] = LeftAssociativeDenotation(80, ::Bitwise)
    this[CARET] = LeftAssociativeDenotation(70, ::Bitwise)
    this[BAR] = LeftAssociativeDenotation(60, ::Bitwise)

    this[AMPERSAND_AMPERSAND] = RightAssociativeDenotation(50, ::Logical)
    this[BAR_BAR] = RightAssociativeDenotation(40, ::Logical)
    this[QUESTION] = ConditionalDenotation(30)
    this[EQUAL] = RightAssociativeDenotation(20, ::Assignment)
    this[PLUS_EQUAL] = RightAssociativeDenotation(20, ::PlusAssignment)
    this[HYPHEN_EQUAL] = RightAssociativeDenotation(20, ::MinusAssignment)
    this[COMMA] = RightAssociativeDenotation(PRECEDENCE_COMMA, ::Comma)
}

private operator fun <V> Array<V>.set(index: TokenKind, value: V) {
    this[index.ordinal] = value
}

private operator fun <V> Array<V>.set(vararg indexes: TokenKind, value: V) {
    for (index in indexes) {
        this[index.ordinal] = value
    }
}
