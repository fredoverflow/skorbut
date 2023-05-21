package semantic

import freditor.persistent.StringedValueMap
import semantic.types.FunctionType
import semantic.types.Type
import syntax.lexer.Token
import syntax.tree.Identifier

data class Symbol(val name: Token, val type: Type, val offset: Int) {
    val usages = ArrayList<Identifier>()

    override fun toString(): String = name.text
}

class SymbolTable {
    private val scopes = Array<StringedValueMap<Symbol>>(128) { StringedValueMap.empty() }
    private var current = 0
    private val closedScopes = ArrayList<StringedValueMap<Symbol>>()
    private val allSymbols = ArrayList<Symbol>()

    fun atGlobalScope(): Boolean {
        return current == 0
    }

    fun openScope() {
        scopes[++current] = StringedValueMap.empty()
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
            scopes[i].get(text)?.let { symbol -> return symbol }
        }
        return null
    }

    fun lookupInClosedScopes(name: Token): Symbol? {
        val text = name.text
        for (i in closedScopes.lastIndex downTo 0) {
            closedScopes[i].get(text)?.let { symbol -> return symbol }
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
                name.error("symbol $name already declared in current scope", previous.name)
            }
        } else {
            val symbol = Symbol(name, type, offset)
            scopes[index] = scopes[index].put(symbol)
            allSymbols.add(symbol)
            return symbol
        }
    }

    fun currentFunction(): Symbol? {
        return scopes[0].maxByOrNull { symbol -> symbol.name.start }
    }

    fun symbols(): Sequence<Symbol> = sequence {
        for (i in current downTo 0) {
            yieldAll(scopes[i])
        }
    }

    fun names(): Sequence<String> {
        return symbols().map(Symbol::toString)
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
