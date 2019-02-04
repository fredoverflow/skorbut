package semantic

import common.Diagnostic
import semantic.types.FunctionType
import semantic.types.Type
import syntax.lexer.Token

data class Symbol(val name: Token, val type: Type, val offset: Int) {
    override fun toString(): String = "$name: $type @ $offset"
}


val emptyScope: Stack<Symbol> = Stack.Nil

fun Stack<Symbol>.declareFlat(name: Token, type: Type, offset: Int): Stack<Symbol> {
    val previous = lookupFlat(name)
    if (previous != null) {
        if (previous.type is FunctionType && !previous.type.defined && type is FunctionType && type.defined) {
            if (previous.type == type) {
                previous.type.defined = true
            } else {
                name.error("function definition signature does not agree with function declaration signature")
            }
        } else {
            throw Diagnostic(name.start, "$name was already declared elsewhere (click to alternate)", previous.name.start)
        }
    }
    return push(Symbol(name, type, offset))
}

fun Stack<Symbol>.lookupFlat(name: Token): Symbol? {
    assert(name.wasProvided())
    return firstOrNull { it.name.text === name.text }
}


val emptySymbolTable: Stack<Stack<Symbol>> = Stack.Nil.push(emptyScope)


fun Stack<Stack<Symbol>>.openScope(): Stack<Stack<Symbol>> = push(emptyScope)

fun Stack<Stack<Symbol>>.closeScope(): Stack<Stack<Symbol>> = pop()

fun Stack<Stack<Symbol>>.declare(name: Token, type: Type, offset: Int): Stack<Stack<Symbol>> {
    return pop().push(top().declareFlat(name, type, offset))
}

fun Stack<Stack<Symbol>>.declareOutside(name: Token, type: Type, offset: Int): Stack<Stack<Symbol>> {
    return pop().declare(name, type, offset).push(top())
}

fun Stack<Stack<Symbol>>.lookup(name: Token): Symbol? {
    for (scope in this) {
        val info = scope.lookupFlat(name)
        if (info != null) return info
    }
    return null
}
