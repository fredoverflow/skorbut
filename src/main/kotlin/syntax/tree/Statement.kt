package syntax.tree

import syntax.lexer.Token

abstract class Statement : Node()

class Declaration(val specifiers: DeclarationSpecifiers, val namedDeclarators: List<NamedDeclarator>) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        namedDeclarators.forEach(action)
    }

    override fun root(): Token = specifiers.root()

    override fun toString(): String = specifiers.toString()
}

class Block(val openBrace: Token, val statements: List<Statement>) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        statements.forEach(action)
    }

    override fun root(): Token = openBrace
}

class ExpressionStatement(val expression: Expression) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(expression)
    }

    override fun root(): Token = expression.root()

    override fun toString(): String = "${super.toString()} ;"
}

class IfThen(val iF: Token, val condition: Expression, val th3n: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(condition)
        action(th3n)
    }

    override fun root(): Token = iF
}

class IfThenElse(val iF: Token, val condition: Expression, val th3n: Statement, val e1se: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(condition)
        action(th3n)
        action(e1se)
    }

    override fun root(): Token = iF
}

class Switch(val switch: Token, val control: Expression, val body: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(control)
        action(body)
    }

    override fun root(): Token = switch
}

class Case(val case: Token, val choice: Expression, val body: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(choice)
        action(body)
    }

    override fun root(): Token = case
}

class Default(val default: Token, val body: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(body)
    }

    override fun root(): Token = default
}

class While(val whi1e: Token, val condition: Expression, val body: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(condition)
        action(body)
    }

    override fun root(): Token = whi1e
}

class Do(val d0: Token, val body: Statement, val whi1e: Token, val condition: Expression) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(body)
        action(condition)
    }

    override fun root(): Token = d0
}

class For(val f0r: Token, val init: Expression?, val condition: Expression?, val update: Expression?, val body: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        init?.run(action)
        condition?.run(action)
        update?.run(action)
        action(body)
    }

    override fun root(): Token = f0r
}

class Continue(val continu3: Token) : Statement() {
    override fun root(): Token = continu3
}

class Break(val br3ak: Token) : Statement() {
    override fun root(): Token = br3ak
}

class Return(val r3turn: Token, val result: Expression) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(result)
    }

    override fun root(): Token = r3turn
}

class Assert(val ass3rt: Token, val condition: Expression) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(condition)
    }

    override fun root(): Token = ass3rt
}

class LabeledStatement(val label: Token, val statement: Statement) : Statement() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(statement)
    }

    override fun root(): Token = label
}

class Goto(val goto: Token, val label: Token) : Statement() {
    override fun root(): Token = goto
}
