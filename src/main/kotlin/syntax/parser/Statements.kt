package syntax.parser

import common.Diagnostic
import syntax.lexer.Token
import syntax.lexer.TokenKind.*
import syntax.tree.*

fun Parser.statement(): Statement = when (current) {

    IF -> IfThenElse(accept(), condition(), statement(), optional(ELSE, ::statement))

    SWITCH -> Switch(accept(), condition(), statement())

    CASE -> Case(accept(), expression() before COLON, statement())

    DEFAULT -> Default(accept() before COLON, statement())

    WHILE -> While(accept(), condition(), statement())

    DO -> Do(accept(), statement() before WHILE, condition()).semicolon()

    FOR -> symbolTable.scoped {
        val f0r = accept() before OPENING_PAREN
        For(
            f0r,
            forInit(f0r),
            ::expression optionalBefore SEMICOLON,
            ::expression optionalBefore CLOSING_PAREN,
            statement()
        )
    }

    GOTO -> Goto(accept(), expect(IDENTIFIER)).semicolon()

    CONTINUE -> Continue(accept()).semicolon()

    BREAK -> Break(accept()).semicolon()

    RETURN -> Return(accept(), ::expression optionalBefore SEMICOLON)

    ASSERT -> Assert(accept(), expression()).semicolon()

    TYPEDEF, EXTERN, STATIC, AUTO, REGISTER, CONST, VOLATILE,
    VOID, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, SIGNED, UNSIGNED,
    STRUCT, UNION, ENUM -> declaration()

    OPENING_BRACE -> symbolTable.scoped {
        Block(token, braced {
            list0Until(CLOSING_BRACE, ::statement)
        })
    }

    IDENTIFIER -> when {
        lookahead.kind == COLON -> LabeledStatement(accept() before COLON, statement())

        isTypedefName(token) -> declaration()

        else -> ExpressionStatement(expression()).semicolon()
    }

    SEMICOLON -> token.error("unexpected semicolon")

    else -> ExpressionStatement(expression()).semicolon()
}

private fun Parser.forInit(f0r: Token): Statement? = when (current) {
    SEMICOLON -> null.semicolon()

    TYPEDEF, EXTERN, STATIC, AUTO, REGISTER, CONST, VOLATILE,
    VOID, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, SIGNED, UNSIGNED,
    STRUCT, UNION, ENUM -> forInitDeclaration(f0r)

    IDENTIFIER -> when {
        isTypedefName(token) -> forInitDeclaration(f0r)

        else -> forInitExpressionStatement(f0r)
    }

    else -> forInitExpressionStatement(f0r)
}

private const val FOR_LOOP_SYNTAX = "for (init; condition; update)\n         ^          ^\nSemicolons, NOT commas!"

private fun Parser.forInitDeclaration(f0r: Token): Statement {
    try {
        return declaration()
    } catch (diagnostic: Diagnostic) {
        if (diagnostic.message.endsWith(" was already declared elsewhere")) {
            f0r.error(FOR_LOOP_SYNTAX)
        } else {
            throw diagnostic
        }
    }
}

private fun Parser.forInitExpressionStatement(f0r: Token): Statement {
    val result = ExpressionStatement(expression())
    if (current == CLOSING_PAREN) {
        f0r.error(FOR_LOOP_SYNTAX)
    }
    return result.semicolon()
}
