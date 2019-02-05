package syntax.parser

import syntax.lexer.*
import syntax.tree.*

fun Parser.statement(): Statement {
    return when (current) {
        IF -> IfThenElse(accept(), condition(), statement(), optional(ELSE, ::statement))
        SWITCH -> Switch(accept(), condition(), statement())
        CASE -> Case(accept(), expression().colon(), statement())
        DEFAULT -> Default(accept().colon(), statement())

        WHILE -> While(accept(), condition(), statement())
        DO -> Do(accept(), statement(), expect(WHILE), condition()).semicolon()
        FOR -> forStatement()

        GOTO -> Goto(accept(), expect(IDENTIFIER)).semicolon()
        CONTINUE -> Continue(accept()).semicolon()
        BREAK -> Break(accept()).semicolon()
        RETURN -> Return(accept(), expression()).semicolon()
        ASSERT -> Assert(accept(), expression()).semicolon()

        TYPEDEF, EXTERN, STATIC, AUTO, REGISTER,
        VOID,
        CHAR, SHORT, INT, LONG, FLOAT, DOUBLE,
        SIGNED, UNSIGNED,
        STRUCT, UNION, ENUM,
        CONST, VOLATILE -> declaration()

        OPENING_BRACE -> symbolTable.scoped {
            Block(token, braced { list0Until(CLOSING_BRACE, ::statement) })
        }

        IDENTIFIER -> when {
            lookahead.kind == COLON -> LabeledStatement(accept().colon(), statement())
            isTypedefName(token) -> declaration()
            else -> ExpressionStatement(expression()).semicolon()
        }

        else -> ExpressionStatement(expression()).semicolon()
    }
}

fun Parser.forStatement(): Statement {
    val f0r = expect(FOR)
    expect(OPENING_PAREN)
    if (!declarationSpecifiers0().isEmpty()) {
        token.error("loop variables must be declared above the loop")
    }
    val init = unless(SEMICOLON, ::expression)
    val check = unless(SEMICOLON, ::expression)
    val update = unless(CLOSING_PAREN, ::expression)
    val body = statement()
    return For(f0r, init, check, update, body)
}
