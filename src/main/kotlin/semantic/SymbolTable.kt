package semantic

import semantic.types.FunctionType
import semantic.types.Type
import syntax.lexer.Token
import syntax.tree.Identifier

data class Symbol(val name: Token, val type: Type, val offset: Int) {
    val usages = ArrayList<Identifier>()

    override fun toString(): String = name.text
}

class SymbolTable {
    private val scopes = Array<HashMap<String, Symbol>>(128) { HashMap() }
    private var current = 0
    private val closedScopes = ArrayList<Map<String, Symbol>>()
    private val allSymbols = ArrayList<Symbol>()

    fun atGlobalScope(): Boolean {
        return current == 0
    }

    fun openScope() {
        scopes[++current] = HashMap()
    }

    fun closeScope() {
        assert(!atGlobalScope()) { "Attempt to close the global scope" }
        if (current == 1) {
            closedScopes.clear()
        } else {
            closedScopes.add(scopes[current])
        }
        --current
    }

    fun reopenScope() {
        ++current
    }

    inline fun <T> scoped(action: () -> T): T {
        openScope()
        val result = action()
        closeScope()
        return result
    }

    inline fun <T> rescoped(action: () -> T): T {
        reopenScope()
        val result = action()
        closeScope()
        return result
    }

    fun lookup(name: Token): Symbol? {
        val text = name.text
        for (i in current downTo 0) {
            scopes[i][text]?.let { symbol -> return symbol }
        }
        return null
    }

    fun lookupInClosedScopes(name: Token): Symbol? {
        val text = name.text
        for (i in closedScopes.lastIndex downTo 0) {
            closedScopes[i][text]?.let { symbol -> return symbol }
        }
        return null
    }

    fun declare(name: Token, type: Type, offset: Int): Symbol {
        return declareIn(scopes[current], name, type, offset)
    }

    fun declareOutside(name: Token, type: Type, offset: Int): Symbol {
        return declareIn(scopes[current - 1], name, type, offset)
    }

    private fun declareIn(scope: HashMap<String, Symbol>, name: Token, type: Type, offset: Int): Symbol {
        val text = name.text
        val previous = scope[text]
        if (previous != null) {
            if (previous.type is FunctionType && !previous.type.defined && type is FunctionType && type.defined) {
                if (previous.type == type) {
                    previous.type.defined = true
                    previous.usages.add(Identifier(name).also { it.symbol = previous })
                    return previous
                } else {
                    name.error("function definition signature does not agree with function declaration signature")
                }
            } else {
                name.error("symbol $name already declared in current scope", previous.name)
            }
        } else {
            val symbol = Symbol(name, type, offset)
            scope[text] = symbol
            allSymbols.add(symbol)
            return symbol
        }
    }

    fun currentFunction(): Symbol? {
        return scopes[0].values.maxByOrNull { symbol -> symbol.name.start }
    }

    fun names(): Sequence<String> = sequence {
        for (i in current downTo 0) {
            for ((_, symbol) in scopes[i]) {
                yield(symbol.name.text)
            }
        }
    }

    fun symbolAt(position: Int): Symbol? {
        for (symbol in allSymbols) {
            if (symbol.name.start <= position && position <= symbol.name.end) return symbol
            for (usage in symbol.usages) {
                if (usage.name.start <= position && position <= usage.name.end) return symbol
            }
        }
        return null
    }
}
