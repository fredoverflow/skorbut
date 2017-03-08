package semantic.types

object VoidType : Type {
    override fun pointer(): Type = VoidPointerType

    override fun count(): Int = 0

    override fun addConst(): Type = ConstVoidType

    override fun toString(): String = "void"
}

object ConstVoidType : Type {
    override fun pointer(): Type = ConstVoidPointerType

    override fun count(): Int = 0

    override fun isConst(): Boolean = true

    override fun addConst(): Type = this

    override fun unqualified(): Type = VoidType

    override fun toString(): String = "const void"
}
