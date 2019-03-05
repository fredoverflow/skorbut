package syntax.tree

import semantic.types.Later
import semantic.types.Type
import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.lexer.TokenKind.ENUM
import syntax.lexer.TokenKind.STRUCT
import syntax.lexer.TokenKindSet

sealed class DeclarationSpecifier : Node() {
    abstract fun kind(): TokenKind

    class Primitive(val token: Token) : DeclarationSpecifier() {
        override fun kind(): TokenKind = token.kind

        override fun root(): Token = token
    }

    class StructDef(val name: Token, val body: List<StructDeclaration>) : DeclarationSpecifier() {
        override fun kind(): TokenKind = STRUCT

        override fun root(): Token = name

        override fun forEachChild(action: (Node) -> Unit) {
            body.forEach { action(it) }
        }

        override fun toString(): String = if (name.wasProvided()) "struct $name" else "struct"
    }

    class StructRef(val name: Token) : DeclarationSpecifier() {
        override fun kind(): TokenKind = STRUCT

        override fun root(): Token = name

        override fun toString(): String = "struct $name"
    }

    class EnumDef(val name: Token, val body: List<Enumerator>) : DeclarationSpecifier() {
        override fun kind(): TokenKind = ENUM

        override fun root(): Token = name

        override fun forEachChild(action: (Node) -> Unit) {
            body.forEach { action(it) }
        }

        override fun toString(): String = if (name.wasProvided()) "enum $name" else "enum"
    }

    class EnumRef(val name: Token) : DeclarationSpecifier() {
        override fun kind(): TokenKind = ENUM

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

class DeclarationSpecifiers(val list: List<DeclarationSpecifier>,
                            val storageClass: TokenKind,
                            val qualifiers: TokenKindSet,
                            val typeTokens: TokenKindSet) : Node() {
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
