package semantic.types

import semantic.Symbol
import syntax.*
import java.util.*

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

    override fun count(): Int = members.sumBy { it.type.count() }

    fun member(name: Token) = members.find { it.name.text === name.text }

    override fun toString(): String = "struct $name"
}

val StructTypeLater = StructType(missingIdentifier, Collections.emptyList())

class StructTag(val structType: StructType) : Type {
    override fun requiresStorage(): Boolean = false

    override fun count(): Int = 0
}
