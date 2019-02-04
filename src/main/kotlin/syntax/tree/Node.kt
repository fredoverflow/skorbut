package syntax.tree

import syntax.lexer.Token

abstract class Node {
    fun walk(enter: (Node) -> Unit, leave: (Node) -> Unit) {
        enter(this)
        walkChildren(enter, leave)
        leave(this)
    }

    fun walkChildren(enter: (Node) -> Unit, leave: (Node) -> Unit) {
        forEachChild {
            it.walk(enter, leave)
        }
    }

    open fun forEachChild(action: (Node) -> Unit) {
    }

    abstract fun root(): Token

    override fun toString(): String = root().toString()
}
