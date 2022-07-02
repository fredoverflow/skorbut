package semantic.types

import interpreter.Value

interface ComparablePointerType : Type

data class PointerType(val referencedType: Type) : ComparablePointerType {
    override fun sizeof(): Int = 4

    override fun canCastFromDecayed(source: Type): Boolean {
        if (source === VoidPointerType) return true

        if (source === ConstVoidPointerType) return referencedType.isConst()

        return canCastFromPointer(source)
    }

    private fun canCastFromPointer(source: Type): Boolean {
        if (source !is PointerType) return false

        val sourceReferenced = source.referencedType
        return referencedType == sourceReferenced || referencedType.unqualified() == sourceReferenced
    }

    override fun cast(source: Value): Value {
        if (!canCastFromPointer(source.type().decayed())) throw AssertionError("${source.type()}\n cannot be converted to\n$this")
        return source.decayed()
    }

    override fun toString(): String = declaration("")

    override fun declaration(parent: String): String {
        return referencedType.declaration("*$parent")
    }
}

object VoidPointerType : ComparablePointerType {
    override fun sizeof(): Int = 4

    override fun canCastFromDecayed(source: Type): Boolean {
        if (source === this) return true
        return source is PointerType && !source.referencedType.isConst()
    }

    override fun toString(): String = "void*"

    override fun declaration(parent: String): String = "void*$parent"
}

object ConstVoidPointerType : ComparablePointerType {
    override fun sizeof(): Int = 4

    override fun canCastFromDecayed(source: Type): Boolean {
        if (source === this || source === VoidPointerType) return true
        return source is PointerType
    }

    override fun toString(): String = "const void*"

    override fun declaration(parent: String): String = "const void*$parent"
}
