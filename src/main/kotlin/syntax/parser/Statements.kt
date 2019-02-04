package syntax.parser

import syntax.lexer.*
import syntax.tree.*

fun Parser.statement(): Statement {
    return when (current) {
        OPENING_BRACE -> block()
        IF -> selectionStatement()
        SWITCH -> switchStatement()
        CASE -> caseStatement()
        DEFAULT -> defaultStatement()
        WHILE -> whileStatement()
        DO -> doStatement()
        FOR -> forStatement()
        GOTO -> gotoStatement()
        CONTINUE -> continueStatement()
        BREAK -> breakStatement()
        RETURN -> returnStatement()
        ASSERT -> assertStatement()

        TYPEDEF,
        EXTERN,
        STATIC,
        AUTO,
        REGISTER,

        VOID,
        CHAR,
        SHORT,
        INT,
        LONG,
        FLOAT,
        DOUBLE,
        SIGNED,
        UNSIGNED,
        STRUCT,
        UNION,
        ENUM,

        CONST,
        VOLATILE -> declaration()

        IDENTIFIER -> {
            if (lookahead.kind == COLON) {
                labeledStatement()
            } else if (isTypedefName(token)) {
                declaration()
            } else {
                expressionStatement()
            }
        }

        else -> expressionStatement()
    }
}

fun Parser.labeledStatement(): Statement {
    val label = expect(IDENTIFIER)
    expect(COLON)
    return LabeledStatement(label, statement())
}

fun Parser.gotoStatement(): Statement {
    val goto = expect(GOTO)
    val label = expect(IDENTIFIER)
    expect(SEMICOLON)
    return Goto(goto, label)
}

fun Parser.continueStatement(): Statement {
    val continu3 = expect(CONTINUE)
    expect(SEMICOLON)
    return Continue(continu3)
}

fun Parser.breakStatement(): Statement {
    val br3ak = expect(BREAK)
    expect(SEMICOLON)
    return Break(br3ak)
}

fun Parser.returnStatement(): Statement {
    val r3turn = expect(RETURN)
    val result = expression()
    expect(SEMICOLON)
    return Return(r3turn, result)
}

fun Parser.assertStatement(): Statement {
    val ass3rt = expect(ASSERT)
    val assertion = expression()
    expect(SEMICOLON)
    return Assert(ass3rt, assertion)
}

fun Parser.selectionStatement(): Statement {
    val iF = expect(IF)
    val condition = parenthesized { expression() }
    val th3n = statement()
    if (current != ELSE) return IfThen(iF, condition, th3n)

    next()
    val e1se = statement()
    return IfThenElse(iF, condition, th3n, e1se)
}

fun Parser.switchStatement(): Statement {
    val switch = expect(SWITCH)
    val control = parenthesized { expression() }
    val body = statement()
    return Switch(switch, control, body)
}

fun Parser.caseStatement(): Statement {
    val case = expect(CASE)
    val choice = expression()
    expect(COLON)
    val body = statement()
    return Case(case, choice, body)
}

fun Parser.defaultStatement(): Statement {
    val default = expect(DEFAULT)
    expect(COLON)
    val body = statement()
    return Default(default, body)
}

fun Parser.whileStatement(): Statement {
    val whi1e = expect(WHILE)
    val condition = parenthesized { expression() }
    val body = statement()
    return While(whi1e, condition, body)
}

fun Parser.doStatement(): Statement {
    val d0 = expect(DO)
    val body = statement()
    expect(WHILE)
    val condition = parenthesized { expression() }
    expect(SEMICOLON)
    return Do(d0, body, condition)
}

fun Parser.forStatement(): Statement {
    val f0r = expect(FOR)
    expect(OPENING_PAREN)
    if (!declarationSpecifiers0().isEmpty()) {
        token.error("loop variables must be declared above the loop")
    }
    val init = unless(SEMICOLON) { expression() }
    val check = unless(SEMICOLON) { expression() }
    val update = unless(CLOSING_PAREN) { expression() }
    val body = statement()
    return For(f0r, init, check, update, body)
}

fun Parser.expressionStatement(): Statement {
    val expression = expression()
    expect(SEMICOLON)
    return ExpressionStatement(expression)
}

fun Parser.block(): Block {
    return symbolTable.scoped {
        Block(token, braced { list0Until(CLOSING_BRACE) { statement() } })
    }
}
