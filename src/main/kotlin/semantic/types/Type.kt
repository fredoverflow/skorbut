package semantic.types

import interpreter.Value

interface Type {
    fun requiresStorage(): Boolean = true

    fun isComplete(): Boolean = sizeof() > 0

    fun sizeof(): Int = 0

    fun sizeof(offset: Int): Int = if (offset == 0) 0 else sizeof()

    fun decayed(): Type = this

    fun pointer(): Type = PointerType(this)

    fun count(): Int = 1

    fun canCastFrom(source: Type): Boolean = canCastFromDecayed(source.decayed())

    fun canCastFromDecayed(source: Type): Boolean = false

    fun cast(source: Value): Value = source

    fun isConst(): Boolean = false

    fun addConst(): Type = Const(this)

    fun unqualified(): Type = this

    fun applyQualifiersTo(target: Type): Type = target
}

data class Const(val underlying: Type) : Type by underlying {
    override fun pointer(): Type = PointerType(this)

    override fun isConst(): Boolean = true

    override fun addConst(): Type = this

    override fun unqualified(): Type = underlying

    override fun applyQualifiersTo(target: Type): Type = target.addConst()

    override fun toString(): String = "Const<$underlying>"
}

object Later : Type
