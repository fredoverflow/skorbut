package semantic.types

import semantic.Symbol
import syntax.lexer.Token
import syntax.lexer.missingIdentifier

abstract class CompletableType : Type {
    private var complete = false

    override fun isComplete(): Boolean = complete

    fun makeComplete(): Type {
        assert(!complete)
        complete = true
        return this
    }
}

class StructType(val name: Token, val members: List<Symbol>) : CompletableType() {
    override fun sizeof(): Int = members.sumBy { it.type.sizeof() }

    override fun sizeof(offset: Int): Int {
        var size = 0
        var off = offset
        for (member in members) {
            size += member.type.sizeof(off)
            off -= member.type.count()
            if (off <= 0) break
        }
        return size
    }

    override fun count(): Int = members.sumBy { it.type.count() }

    fun member(name: Token) = members.find { it.name.text === name.text }

    override fun toString(): String = "struct $name"

    override fun declaration(parent: String): String = "struct $name$parent"
}

val StructTypeLater = StructType(missingIdentifier, emptyList())

class StructTag(val structType: StructType) : Type {
    override fun requiresStorage(): Boolean = false

    override fun count(): Int = 0
}
