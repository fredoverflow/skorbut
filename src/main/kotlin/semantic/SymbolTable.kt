package semantic

import freditor.persistent.StringedValueMap
import semantic.types.FunctionType
import semantic.types.Type
import syntax.lexer.Token

data class Symbol(val name: Token, val type: Type, val offset: Int) {
    override fun toString(): String = name.text
}

class SymbolTable {
    private val scopes = Array(128) { StringedValueMap.empty<Symbol>() }
    private var current = 0

    fun atGlobalScope(): Boolean = current == 0

    fun openScope() {
        scopes[++current] = StringedValueMap.empty()
    }

    fun closeScope() {
        assert(!atGlobalScope()) { "Attempt to close the global scope" }
        --current
    }

    fun reopenScope() {
        ++current
    }

    inline fun <T> scoped(action: () -> T): T {
        openScope()
        try {
            return action()
        } finally {
            closeScope()
        }
    }

    inline fun <T> rescoped(action: () -> T): T {
        reopenScope()
        try {
            return action()
        } finally {
            closeScope()
        }
    }

    fun lookup(name: Token): Symbol? {
        val text = name.text
        for (i in current downTo 0) {
            scopes[i].get(text)?.let { symbol -> return symbol }
        }
        return null
    }

    fun declare(name: Token, type: Type, offset: Int): Symbol {
        return declareAt(current, name, type, offset)
    }

    fun declareOutside(name: Token, type: Type, offset: Int): Symbol {
        return declareAt(current - 1, name, type, offset)
    }

    private fun declareAt(index: Int, name: Token, type: Type, offset: Int): Symbol {
        val text = name.text
        val previous = scopes[index].get(text)
        if (previous != null) {
            if (previous.type is FunctionType && !previous.type.defined && type is FunctionType && type.defined) {
                if (previous.type == type) {
                    previous.type.defined = true
                    return previous
                } else {
                    name.error("function definition signature does not agree with function declaration signature")
                }
            } else {
                name.error("$name was already declared elsewhere", previous.name)
            }
        } else {
            val symbol = Symbol(name, type, offset)
            scopes[index] = scopes[index].put(symbol)
            return symbol
        }
    }
}
