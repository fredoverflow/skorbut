package syntax.parser

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
        For(accept() before OPENING_PAREN,
                forInit(),
                ::expression optionalBefore SEMICOLON,
                ::expression optionalBefore CLOSING_PAREN,
                statement())
    }

    GOTO -> Goto(accept(), expect(IDENTIFIER)).semicolon()
    CONTINUE -> Continue(accept()).semicolon()
    BREAK -> Break(accept()).semicolon()
    RETURN -> Return(accept(), expression()).semicolon()
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

    else -> ExpressionStatement(expression()).semicolon()
}

private fun Parser.forInit(): Statement? = when (current) {
    SEMICOLON -> null.semicolon()

    TYPEDEF, EXTERN, STATIC, AUTO, REGISTER, CONST, VOLATILE,
    VOID, CHAR, SHORT, INT, LONG, FLOAT, DOUBLE, SIGNED, UNSIGNED,
    STRUCT, UNION, ENUM -> declaration()

    IDENTIFIER -> when {
        isTypedefName(token) -> declaration()

        else -> ExpressionStatement(expression()).semicolon()
    }

    else -> ExpressionStatement(expression()).semicolon()
}
