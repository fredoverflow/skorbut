package syntax.parser

import semantic.MutableSymbolTable
import syntax.*

const val PRECEDENCE_POSTFIX = 150
const val PRECEDENCE_PREFIX = 140
const val PRECEDENCE_COMMA = 10

enum class DeclarationState {
    OPEN, PRIMITIVE, USER_DEFINED, NO_DECLARATOR_REQUIRED
}

class Parser(lexer: Lexer) : ParserBase(lexer) {

    // ===== Expressions =====

    @Suppress("NOTHING_TO_INLINE")
    inline fun expression(): Expression {
        return subexpression(outerPrecedence = 0)
    }

    @Suppress("NOTHING_TO_INLINE")
    inline fun assignmentExpression(): Expression {
        return subexpression(outerPrecedence = PRECEDENCE_COMMA)
    }

    fun subexpression(outerPrecedence: Int): Expression {
        val nullDenotation = nullDenotations[current] ?: illegalStartOf("expression")

        return subexpression(with(nullDenotation) { this@Parser.parse(consume(token)) }, outerPrecedence)
    }

    tailrec fun subexpression(left: Expression, outerPrecedence: Int): Expression {
        val leftDenotation = leftDenotations[current] ?: return left
        if (leftDenotation.precedence <= outerPrecedence) return left

        return subexpression(with(leftDenotation) { this@Parser.parse(left, consume(token)) }, outerPrecedence)
    }

    private val nullDenotations = ByteMap<NullDenotation>().apply {
        this[IDENTIFIER] = IdentifierDenotation
        this[DOUBLE_CONSTANT, FLOAT_CONSTANT, INTEGER_CONSTANT, CHARACTER_CONSTANT] = ConstantDenotation
        this[STRING_LITERAL] = StringLiteralDenotation
        this[OPEN_PAREN] = PossibleCastDenotation
        this[PLUS_PLUS, MINUS_MINUS, AMP, STAR, PLUS, MINUS, TILDE, BANG] = PrefixDenotation
        this[SIZEOF] = SizeofDenotation
    }

    private val leftDenotations = ByteMap<LeftDenotation>().apply {
        this[OPEN_BRACKET] = SubscriptDenotation
        this[OPEN_PAREN] = FunctionCallDenotation
        this[DOT] = DirectMemberDenotation
        this[ARROW] = IndirectMemberDenotation
        this[PLUS_PLUS, MINUS_MINUS] = PostfixCrementDenotation

        this[STAR, SLASH, PERCENT] = LeftAssociativeDenotation(130, ::Multiplicative)
        this[PLUS] = LeftAssociativeDenotation(120, ::Plus)
        this[MINUS] = LeftAssociativeDenotation(120, ::Minus)
        this[LESS_LESS, MORE_MORE] = LeftAssociativeDenotation(110, ::Shift)
        this[LESS, MORE, LESS_EQ, MORE_EQ] = LeftAssociativeDenotation(100, ::RelationalEquality)
        this[EQ_EQ, BANG_EQ] = LeftAssociativeDenotation(90, ::RelationalEquality)
        this[AMP] = LeftAssociativeDenotation(80, ::Bitwise)
        this[CARET] = LeftAssociativeDenotation(70, ::Bitwise)
        this[PIPE] = LeftAssociativeDenotation(60, ::Bitwise)
        this[AMP_AMP] = LeftAssociativeDenotation(50, ::Logical)
        this[PIPE_PIPE] = LeftAssociativeDenotation(40, ::Logical)
        this[QUESTION] = ConditionalDenotation(30)
        this[EQ] = RightAssociativeDenotation(20, ::Assignment)
        this[PLUS_EQ] = RightAssociativeDenotation(20, ::PlusAssignment)
        this[MINUS_EQ] = RightAssociativeDenotation(20, ::MinusAssignment)
        this[COMMA] = RightAssociativeDenotation(PRECEDENCE_COMMA, ::Comma)
    }

    // ===== Declarations =====

    val symbolTable = MutableSymbolTable()

    var declarationState = DeclarationState.OPEN
}
