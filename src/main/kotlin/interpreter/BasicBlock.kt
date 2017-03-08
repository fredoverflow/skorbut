package interpreter

import java.util.*

class BasicBlock {
    private val statements = ArrayList<FlatStatement>()
    fun getStatements(): List<FlatStatement> = statements

    fun isOpen(): Boolean {
        return !isClosed()
    }

    fun isClosed(): Boolean {
        return statements.lastOrNull()?.transfersControl() ?: false
    }

    fun isEmpty(): Boolean {
        return statements.isEmpty()
    }

    fun add(statement: FlatStatement) {
        assert(isOpen())
        statements.add(statement)
    }

    fun replaceSwitchPlaceholderWithRealSwitch(replacement: HashSwitch) {
        assert(statements.last() === SwitchPlaceholder)
        statements[statements.lastIndex] = replacement
    }

    override fun toString(): String {
        return "[${if (isReachable) '*' else ' '}] $statements"
    }

    var isReachable = false
        private set

    fun exploreReachability(resolve: (String) -> BasicBlock) {
        if (!isReachable) {
            isReachable = true
            statements.lastOrNull()?.forEachSuccessor { label ->
                resolve(label).exploreReachability(resolve)
            }
        }
    }
}
