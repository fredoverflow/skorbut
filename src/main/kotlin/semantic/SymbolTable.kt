package semantic

import common.Diagnostic
import freditor.persistent.ChampMap
import semantic.types.FunctionType
import semantic.types.Type
import syntax.lexer.Token

data class Symbol(val name: Token, val type: Type, val offset: Int) {
    override fun toString(): String = "$name: $type @ $offset"
}

class SymbolTable {
    private val allScopes = mutableListOf(ChampMap.empty<String, Symbol>())

    fun atGlobalScope(): Boolean = allScopes.size == 1

    fun openScope() {
        allScopes.add(ChampMap.empty<String, Symbol>())
    }

    fun closeScope() {
        assert(!atGlobalScope()) { "Attempt to close the global scope" }
        allScopes.removeAt(allScopes.lastIndex)
    }

    inline fun <T> scoped(action: () -> T): T {
        openScope()
        try {
            return action()
        } finally {
            closeScope()
        }
    }

    fun lookup(name: Token): Symbol? {
        val text = name.text
        allScopes.asReversed().forEach { scope ->
            scope.get(text)?.let { symbol -> return symbol }
        }
        return null
    }

    fun declare(name: Token, type: Type, offset: Int): Symbol {
        return declareAt(allScopes.size - 1, name, type, offset)
    }

    fun declareOutside(name: Token, type: Type, offset: Int): Symbol {
        return declareAt(allScopes.size - 2, name, type, offset)
    }

    private fun declareAt(scopeIndex: Int, name: Token, type: Type, offset: Int): Symbol {
        val text = name.text
        val previous = allScopes[scopeIndex].get(text)
        if (previous != null) {
            if (previous.type is FunctionType && !previous.type.defined && type is FunctionType && type.defined) {
                if (previous.type == type) {
                    previous.type.defined = true
                    return previous
                } else {
                    name.error("function definition signature does not agree with function declaration signature")
                }
            } else {
                throw Diagnostic(name.start, "$name was already declared elsewhere (click to alternate)", previous.name.start)
            }
        } else {
            val symbol = Symbol(name, type, offset)
            allScopes[scopeIndex] = allScopes[scopeIndex].put(text, symbol)
            return symbol
        }
    }
}
