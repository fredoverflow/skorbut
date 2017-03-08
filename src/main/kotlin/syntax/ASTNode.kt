package syntax

abstract class ASTNode {
    fun walk(enter: (ASTNode) -> Unit, leave: (ASTNode) -> Unit) {
        enter(this)
        walkChildren(enter, leave)
        leave(this)
    }

    fun walkChildren(enter: (ASTNode) -> Unit, leave: (ASTNode) -> Unit) {
        forEachChild {
            it.walk(enter, leave)
        }
    }

    open fun forEachChild(action: (ASTNode) -> Unit) {
    }

    abstract fun root(): Token

    override fun toString(): String = root().toString()
}
