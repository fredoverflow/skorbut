package semantic

import common.Diagnostic
import syntax.Expression
import syntax.Token
import java.util.ArrayList

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
