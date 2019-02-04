package semantic

import common.Diagnostic
import syntax.lexer.Token
import syntax.tree.Expression

abstract class LinterBase {
    private val warnings = ArrayList<Diagnostic>()

    fun getWarnings(): List<Diagnostic> = warnings.sortedBy { it.position }

    protected fun Token.warn(description: String) {
        warnings.add(Diagnostic(start, description))
    }

    protected fun Expression.warn(description: String) {
        root().warn(description)
    }
}
