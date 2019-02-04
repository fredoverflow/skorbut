package syntax.parser

import syntax.lexer.*
import syntax.tree.*

abstract class NullDenotation {
    abstract fun Parser.parse(token: Token): Expression
}

object IdentifierDenotation : NullDenotation() {
    override fun Parser.parse(name: Token): Expression {
        return when (name.text) {
            "printf" -> parenthesized { printfCall(name.withTokenKind(PRINTF)) }
            "scanf" -> parenthesized { scanfCall(name.withTokenKind(SCANF)) }
            else -> Identifier(name)
        }
    }

    private fun Parser.printfCall(printf: Token): Expression {
        val format = expect(STRING_LITERAL)
        val arguments = if (current == COMMA) {
            next()
            commaSeparatedList1 { assignmentExpression() }
        } else {
            emptyList()
        }
        return PrintfCall(printf, format, arguments)
    }

    private fun Parser.scanfCall(scanf: Token): Expression {
        val format = expect(STRING_LITERAL)
        val arguments = if (current == COMMA) {
            next()
            commaSeparatedList1 { assignmentExpression() }
        } else {
            emptyList()
        }
        return ScanfCall(scanf, format, arguments)
    }
}

object ConstantDenotation : NullDenotation() {
    override fun Parser.parse(constant: Token): Expression {
        return Constant(constant)
    }
}

object StringLiteralDenotation : NullDenotation() {
    override fun Parser.parse(literal: Token): Expression {
        return StringLiteral(literal)
    }
}

object PossibleCastDenotation : NullDenotation() {
    override fun Parser.parse(openParen: Token): Expression {
        val specifiers = declarationSpecifiers0()
        if (!specifiers.isEmpty()) {
            notImplementedYet("casting")
        } else {
            return expression().also { expect(CLOSING_PAREN) }
        }
    }
}

object PrefixDenotation : NullDenotation() {
    override fun Parser.parse(operator: Token): Expression {
        val operand = subexpression(PRECEDENCE_PREFIX)
        return when (operator.kind) {
            PLUS_PLUS,
            HYPHEN_HYPHEN -> Prefix(operator, operand)
            AMPERSAND -> Reference(operator, operand)
            ASTERISK -> Dereference(operator, operand)
            PLUS -> UnaryPlus(operator, operand)
            HYPHEN -> UnaryMinus(operator, operand)
            TILDE -> BitwiseNot(operator, operand)
            BANG -> LogicalNot(operator, operand)
            else -> error("no parse for $operator")
        }
    }
}

object SizeofDenotation : NullDenotation() {
    override fun Parser.parse(operator: Token): Expression {
        if (current == OPENING_PAREN) {
            next()
            val specifiers = declarationSpecifiers0()
            if (!specifiers.isEmpty()) {
                val result = SizeofType(operator, DeclarationSpecifiers(specifiers), abstractDeclarator())
                expect(CLOSING_PAREN)
                return result
            } else {
                val primary = expression()
                expect(CLOSING_PAREN)
                val unary = subexpression(primary, PRECEDENCE_PREFIX)
                return SizeofExpression(operator, unary)
            }
        } else {
            return SizeofExpression(operator, subexpression(PRECEDENCE_PREFIX))
        }
    }
}
