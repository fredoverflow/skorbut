package semantic

import common.Diagnostic
import syntax.lexer.Token
import syntax.tree.Expression

abstract class LinterBase {
    private val warnings = ArrayList<Diagnostic>()

    fun getWarnings(): List<Diagnostic> = warnings.sortedBy { it.position }

    protected fun Token.warn(message: String) {
        warnings.add(Diagnostic(start, message))
    }

    protected fun Expression.warn(message: String) {
        root().warn(message)
    }
}
