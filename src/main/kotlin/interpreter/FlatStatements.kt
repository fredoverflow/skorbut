package interpreter

import syntax.lexer.Token
import syntax.tree.DeclarationSpecifiers
import syntax.tree.Expression
import syntax.tree.NamedDeclarator

sealed class FlatStatement {
    open fun forEachSuccessor(action: (String) -> Unit) {
    }
}

sealed class TransferringControl : FlatStatement()

class Jump(val target: String) : TransferringControl() {
    override fun forEachSuccessor(action: (String) -> Unit) {
        action(target)
    }
}

class JumpIf(val condition: Expression, val th3n: String, val e1se: String) : TransferringControl() {
    override fun forEachSuccessor(action: (String) -> Unit) {
        action(th3n)
        action(e1se)
    }
}

object SwitchPlaceholder : TransferringControl()

class HashSwitch(val control: Expression, val cases: HashMap<ArithmeticValue, String>, val default: String) :
    TransferringControl() {
    override fun forEachSuccessor(action: (String) -> Unit) {
        cases.values.forEach { action(it) }
        action(default)
    }
}

class FlatDeclaration(val specifiers: DeclarationSpecifiers, val namedDeclarators: List<NamedDeclarator>) :
    FlatStatement()

class FlatExpressionStatement(val expression: Expression) : FlatStatement()

class FlatReturn(val r3turn: Token, val result: Expression?) : TransferringControl()

class FlatAssert(val condition: Expression) : FlatStatement()
