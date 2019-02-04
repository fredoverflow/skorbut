package interpreter

import syntax.tree.DeclarationSpecifiers
import syntax.tree.Expression
import syntax.tree.NamedDeclarator

abstract class FlatStatement {
    open fun transfersControl(): Boolean = false

    open fun forEachSuccessor(action: (String) -> Unit) {
    }
}

class Jump(val target: String) : FlatStatement() {
    override fun toString(): String {
        return "Jump $target"
    }

    override fun transfersControl(): Boolean = true

    override fun forEachSuccessor(action: (String) -> Unit) {
        action(target)
    }
}

class JumpIf(val condition: Expression, val th3n: String, val e1se: String) : FlatStatement() {
    override fun toString(): String {
        return "JumpIf ($condition) $th3n else $e1se"
    }

    override fun transfersControl(): Boolean = true

    override fun forEachSuccessor(action: (String) -> Unit) {
        action(th3n)
        action(e1se)
    }
}

object SwitchPlaceholder : FlatStatement() {
    override fun transfersControl(): Boolean = true
}

class HashSwitch(val control: Expression, val cases: HashMap<ArithmeticValue, String>, val default: String) : FlatStatement() {
    override fun toString(): String {
        return "HashSwitch ($control)"
    }

    override fun transfersControl(): Boolean = true

    override fun forEachSuccessor(action: (String) -> Unit) {
        cases.values.forEach { action(it) }
        action(default)
    }
}

class FlatDeclaration(val specifiers: DeclarationSpecifiers, val namedDeclarators: List<NamedDeclarator>) : FlatStatement() {
    override fun toString(): String = specifiers.toString()
}

class FlatExpressionStatement(val expression: Expression) : FlatStatement() {
    override fun toString(): String = "$expression ;"
}

class FlatReturn(val result: Expression) : FlatStatement() {
    override fun toString(): String = "return $result"

    override fun transfersControl(): Boolean = true
}

class FlatAssert(val condition: Expression) : FlatStatement() {
    override fun toString(): String = "assert $condition"
}
