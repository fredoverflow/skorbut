package syntax

import semantic.types.Later
import semantic.types.Type

class NamedDeclarator(val name: Token, val declarator: Declarator) : ASTNode() {
    override fun root(): Token = name

    var type: Type = Later
    var offset: Int = 1234567890

    override fun toString(): String = "$name : $type"
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

abstract class Initializer : ASTNode()

class ExpressionInitializer(val expression: Expression) : Initializer() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        action(expression)
    }

    override fun root(): Token = expression.root()
}

class InitializerList(val openBrace: Token, val list: List<Initializer>) : Initializer() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        list.forEach(action)
    }

    override fun root(): Token = openBrace
}
