package syntax.parser

import syntax.lexer.Token
import syntax.lexer.TokenKind.*
import syntax.tree.*

abstract class NullDenotation {
    abstract fun Parser.parse(token: Token): Expression
}

object IdentifierDenotation : NullDenotation() {
    override fun Parser.parse(token: Token): Expression {
        return when (token.text) {
            "printf" -> parenthesized { printfCall(token.withTokenKind(PRINTF)) }
            "scanf" -> parenthesized { scanfCall(token.withTokenKind(SCANF)) }
            else -> Identifier(token)
        }
    }

    private fun Parser.printfCall(printf: Token): Expression {
        val format = expect(STRING_LITERAL)
        val arguments = if (current == COMMA) {
            next()
            commaSeparatedList1(::assignmentExpression)
        } else {
            emptyList()
        }
        return PrintfCall(printf, format, arguments)
    }

    private fun Parser.scanfCall(scanf: Token): Expression {
        val format = expect(STRING_LITERAL)
        val arguments = if (current == COMMA) {
            next()
            commaSeparatedList1(::assignmentExpression)
        } else {
            emptyList()
        }
        return ScanfCall(scanf, format, arguments)
    }
}

object ConstantDenotation : NullDenotation() {
    override fun Parser.parse(token: Token): Expression {
        return Constant(token)
    }
}

object StringLiteralDenotation : NullDenotation() {
    override fun Parser.parse(token: Token): Expression {
        return StringLiteral(token)
    }
}

object PossibleCastDenotation : NullDenotation() {
    override fun Parser.parse(token: Token): Expression {
        val specifiers = declarationSpecifiers0()
        return if (specifiers.list.isEmpty()) {
            expression() before CLOSING_PAREN
        } else {
            val declarator = abstractDeclarator() before CLOSING_PAREN
            Cast(token, specifiers, declarator, subexpression(PRECEDENCE_PREFIX))
        }
    }
}

object PrefixDenotation : NullDenotation() {
    override fun Parser.parse(token: Token): Expression {
        val operand = subexpression(PRECEDENCE_PREFIX)
        return when (token.kind) {
            PLUS_PLUS,
            HYPHEN_HYPHEN -> Prefix(token, operand)
            AMPERSAND -> Reference(token, operand)
            ASTERISK -> Dereference(token, operand)
            PLUS -> UnaryPlus(token, operand)
            HYPHEN -> UnaryMinus(token, operand)
            TILDE -> BitwiseNot(token, operand)
            BANG -> LogicalNot(token, operand)
            else -> error("no parse for $token")
        }
    }
}

object SizeofDenotation : NullDenotation() {
    override fun Parser.parse(token: Token): Expression {
        return if (current == OPENING_PAREN && isDeclarationSpecifier(lookahead)) {
            parenthesized { SizeofType(token, declarationSpecifiers0(), abstractDeclarator()) }
        } else {
            SizeofExpression(token, subexpression(PRECEDENCE_PREFIX))
        }
    }
}
