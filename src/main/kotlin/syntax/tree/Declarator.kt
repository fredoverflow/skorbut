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
    fun leaf(): Declarator = leaf(Identity)

    protected abstract fun leaf(parent: Declarator): Declarator

    object Identity : Declarator() {
        override fun leaf(parent: Declarator): Declarator = parent
    }

    class Pointer(val child: Declarator, val qualifiers: List<Token>) : Declarator() {
        override fun leaf(parent: Declarator): Declarator = child.leaf(this)
    }

    class Array(val child: Declarator, val length: Expression?) : Declarator() {
        override fun leaf(parent: Declarator): Declarator = child.leaf(this)
    }

    class Function(val child: Declarator, val parameters: List<FunctionParameter>) : Declarator() {
        override fun leaf(parent: Declarator): Declarator = child.leaf(this)
    }

    class Initialized(val declarator: Declarator, val init: Initializer) : Declarator() {
        override fun leaf(parent: Declarator): Declarator = declarator.leaf(Identity)
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
