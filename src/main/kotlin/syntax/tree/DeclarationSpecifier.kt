package syntax.tree

import semantic.types.Later
import semantic.types.Type
import syntax.lexer.ENUM
import syntax.lexer.STRUCT
import syntax.lexer.Token

sealed class DeclarationSpecifier : Node() {
    abstract fun kind(): Byte

    class Primitive(val token: Token) : DeclarationSpecifier() {
        override fun kind(): Byte = token.kind

        override fun root(): Token = token
    }

    class StructDef(val name: Token, val body: List<StructDeclaration>) : DeclarationSpecifier() {
        override fun kind(): Byte = STRUCT

        override fun root(): Token = name

        override fun forEachChild(action: (Node) -> Unit) {
            body.forEach { action(it) }
        }

        override fun toString(): String = if (name.wasProvided()) "struct $name" else "struct"
    }

    class StructRef(val name: Token) : DeclarationSpecifier() {
        override fun kind(): Byte = STRUCT

        override fun root(): Token = name

        override fun toString(): String = "struct $name"
    }

    class EnumDef(val name: Token, val body: List<Enumerator>) : DeclarationSpecifier() {
        override fun kind(): Byte = ENUM

        override fun root(): Token = name

        override fun forEachChild(action: (Node) -> Unit) {
            body.forEach { action(it) }
        }

        override fun toString(): String = if (name.wasProvided()) "enum $name" else "enum"
    }

    class EnumRef(val name: Token) : DeclarationSpecifier() {
        override fun kind(): Byte = ENUM

        override fun root(): Token = name

        override fun toString(): String = "enum $name"
    }
}

class StructDeclaration(val specifiers: DeclarationSpecifiers, val declarators: List<NamedDeclarator>) : Node() {
    override fun root(): Token = specifiers.root()

    override fun forEachChild(action: (Node) -> Unit) {
        declarators.forEach { action(it) }
    }

    override fun toString(): String = specifiers.toString()
}

class DeclarationSpecifiers(val list: List<DeclarationSpecifier>) : Node() {
    var storageClass: Byte = -1
    var type: Type = Later

    override fun root(): Token = list[0].root()

    override fun forEachChild(action: (Node) -> Unit) {
        list.forEach { action(it) }
    }

    override fun toString(): String = list.joinToString(" ")
}

class Enumerator(val name: Token, val init: Expression?) : Node() {
    override fun root(): Token = name
}
