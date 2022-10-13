package syntax.parser

import syntax.lexer.Token
import syntax.lexer.TokenKind.*
import syntax.tree.*

abstract class LeftDenotation(val precedence: Int) {
    abstract fun Parser.parse(left: Expression, operator: Token): Expression
}

object SubscriptDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return Subscript(left, operator, expression() before CLOSING_BRACKET)
    }
}

object FunctionCallDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return FunctionCall(left, commaSeparatedList0(CLOSING_PAREN, ::functionCallArgument) before CLOSING_PAREN)
    }
}

object DirectMemberDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return DirectMemberAccess(left, operator, expect(IDENTIFIER))
    }
}

object IndirectMemberDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return IndirectMemberAccess(left, operator, expect(IDENTIFIER))
    }
}

object PostfixCrementDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return Postfix(left, operator)
    }
}

class LeftAssociativeDenotation(precedence: Int, val factory: (Expression, Token, Expression) -> Expression) : LeftDenotation(precedence) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return factory(left, operator, subexpression(precedence))
    }
}

class RightAssociativeDenotation(precedence: Int, val factory: (Expression, Token, Expression) -> Expression) : LeftDenotation(precedence) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return factory(left, operator, subexpression(precedence - 1))
    }
}

class ConditionalDenotation(precedence: Int) : LeftDenotation(precedence) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        return Conditional(left, operator, expression(), expect(COLON), subexpression(precedence - 1))
    }
}
