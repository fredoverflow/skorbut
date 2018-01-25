package syntax.parser

import syntax.*

abstract class LeftDenotation(val precedence: Int) {
    abstract fun Parser.parse(left: Expression, operator: Token): Expression
}

object SubscriptDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, openBracket: Token): Expression {
        return Subscript(left, openBracket, expression()).also { expect(CLOSING_BRACKET) }
    }
}

object FunctionCallDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, openParen: Token): Expression {
        return FunctionCall(left, commaSeparatedList0(CLOSING_PAREN) { assignmentExpression() }).also { expect(CLOSING_PAREN) }
    }
}

object DirectMemberDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, dot: Token): Expression {
        return DirectMemberAccess(left, dot, expect(IDENTIFIER))
    }
}

object IndirectMemberDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, arrow: Token): Expression {
        return IndirectMemberAccess(left, arrow, expect(IDENTIFIER))
    }
}

object PostfixCrementDenotation : LeftDenotation(PRECEDENCE_POSTFIX) {
    override fun Parser.parse(left: Expression, crement: Token): Expression {
        return Postfix(left, crement)
    }
}

class LeftAssociativeDenotation(precedence: Int, val factory: (Expression, Token, Expression) -> Expression) : LeftDenotation(precedence) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        val right = subexpression(precedence)
        return factory(left, operator, right)
    }
}

class RightAssociativeDenotation(precedence: Int, val factory: (Expression, Token, Expression) -> Expression) : LeftDenotation(precedence) {
    override fun Parser.parse(left: Expression, operator: Token): Expression {
        val right = subexpression(precedence - 1)
        return factory(left, operator, right)
    }
}

class ConditionalDenotation(precedence: Int) : LeftDenotation(precedence) {
    override fun Parser.parse(condition: Expression, question: Token): Expression {
        return Conditional(condition, question, expression(), expect(COLON), subexpression(precedence - 1))
    }
}
