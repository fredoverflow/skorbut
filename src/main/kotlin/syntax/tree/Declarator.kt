package syntax.tree

import semantic.types.Later
import semantic.types.Type
import syntax.lexer.Token

class NamedDeclarator(val name: Token, val declarator: Declarator) : Node() {
    override fun root(): Token = name

    var type: Type = Later
    var offset: Int = 1234567890

    override fun toString(): String = "$name : $type"

    inline fun map(f: (Declarator) -> Declarator): NamedDeclarator = NamedDeclarator(name, f(declarator))
}

class FunctionParameter(val specifiers: DeclarationSpecifiers, val namedDeclarator: NamedDeclarator)

sealed class Declarator {
    fun reverse(): Declarator = reverse(Identity)

    protected abstract fun reverse(result: Declarator): Declarator

    object Identity : Declarator() {
        override fun reverse(result: Declarator): Declarator = result
    }

    class Pointer(val previous: Declarator, val qualifiers: List<Token>) : Declarator() {
        override fun reverse(result: Declarator): Declarator = previous.reverse(Pointer(result, qualifiers))
    }

    class Array(val previous: Declarator, val length: Expression?) : Declarator() {
        override fun reverse(result: Declarator): Declarator = previous.reverse(Array(result, length))
    }

    class Function(val previous: Declarator, val parameters: List<FunctionParameter>) : Declarator() {
        override fun reverse(result: Declarator): Declarator = previous.reverse(Function(result, parameters))
    }

    class Initialized(val declarator: Declarator, val init: Initializer) : Declarator() {
        override fun reverse(result: Declarator) = error("reverse on initialized declarator")
    }
}

abstract class Initializer : Node()

class ExpressionInitializer(val expression: Expression) : Initializer() {
    override fun forEachChild(action: (Node) -> Unit) {
        action(expression)
    }

    override fun root(): Token = expression.root()
}

class InitializerList(val openBrace: Token, val list: List<Initializer>) : Initializer() {
    override fun forEachChild(action: (Node) -> Unit) {
        list.forEach(action)
    }

    override fun root(): Token = openBrace
}
