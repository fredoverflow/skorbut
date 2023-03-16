package interpreter

import syntax.lexer.Token
import syntax.lexer.missingIdentifier
import syntax.tree.DeclarationSpecifiers
import syntax.tree.Expression
import syntax.tree.NamedDeclarator

sealed class FlatStatement {
    abstract fun root(): Token

    open fun forEachSuccessor(action: (String) -> Unit) {
    }
}

sealed class TransferringControl : FlatStatement()

class Jump(val keyword: Token, val target: String) : TransferringControl() {
    override fun root(): Token = keyword

    override fun forEachSuccessor(action: (String) -> Unit) {
        action(target)
    }
}

class ImplicitContinue(val keyword: Token, val target: String) : TransferringControl() {
    override fun root(): Token = keyword

    override fun forEachSuccessor(action: (String) -> Unit) {
        action(target)
    }
}

class JumpIf(val condition: Expression, val th3n: String, val e1se: String) : TransferringControl() {
    override fun root(): Token = condition.root()

    override fun forEachSuccessor(action: (String) -> Unit) {
        action(th3n)
        action(e1se)
    }
}

object SwitchPlaceholder : TransferringControl() {
    override fun root(): Token = missingIdentifier
}

class HashSwitch(val control: Expression, val cases: HashMap<ArithmeticValue, String>, val default: String) :
    TransferringControl() {
    override fun root(): Token = control.root()

    override fun forEachSuccessor(action: (String) -> Unit) {
        cases.values.forEach { action(it) }
        action(default)
    }
}

class FlatDeclaration(val specifiers: DeclarationSpecifiers, val namedDeclarators: List<NamedDeclarator>) :
    FlatStatement() {
    override fun root(): Token = specifiers.root()
}

class FlatExpressionStatement(val expression: Expression) : FlatStatement() {
    override fun root(): Token = expression.root()
}

class FlatReturn(val r3turn: Token, val result: Expression?) : TransferringControl() {
    override fun root(): Token = r3turn
}

class FlatAssert(val condition: Expression) : FlatStatement() {
    override fun root(): Token = condition.root()
}
