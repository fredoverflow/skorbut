package semantic

import semantic.types.Type
import syntax.lexer.Token

class MutableSymbolTable {
    private var symbolTable = emptySymbolTable

    fun atGlobalScope(): Boolean = symbolTable.pop().isEmpty()

    fun lookup(name: Token): Symbol? = symbolTable.lookup(name)

    fun lastSymbol(): Symbol = symbolTable.top().top()

    fun declare(name: Token, type: Type, offset: Int) {
        symbolTable = symbolTable.declare(name, type, offset)
    }

    fun declareOutside(name: Token, type: Type, offset: Int) {
        symbolTable = symbolTable.declareOutside(name, type, offset)
    }

    fun <T> scoped(action: () -> T): T {
        openScope()
        try {
            return action()
        } finally {
            closeScope()
        }
    }

    fun openScope() {
        symbolTable = symbolTable.openScope()
    }

    fun closeScope() {
        symbolTable = symbolTable.closeScope()
    }
}
