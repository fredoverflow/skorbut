package semantic.types

data class ArrayType(var length: Int, val elementType: Type) : Type {
    override fun sizeof(): Int = length * elementType.sizeof()

    override fun sizeof(offset: Int): Int {
        if (offset >= count()) return sizeof()

        val n = elementType.count()
        return offset / n * elementType.sizeof() + elementType.sizeof(offset % n)
    }

    override fun decayed(): Type = PointerType(elementType)

    override fun count(): Int = length * elementType.count()

    override fun isConst(): Boolean = elementType.isConst()

    override fun addConst(): Type = if (isConst()) this else ArrayType(length, elementType.addConst())

    override fun unqualified(): Type = if (isConst()) ArrayType(length, elementType.unqualified()) else this

    fun dimensions(): Int = if (elementType is ArrayType) elementType.dimensions() + 1 else 1

    override fun toString(): String = declaration("")

    override fun declaration(parent: String): String {
        return if (parent.isPointer()) {
            elementType.declaration("($parent)[$length]")
        } else {
            elementType.declaration("$parent[$length]")
        }
    }
}
