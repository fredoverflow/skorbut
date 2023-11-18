package semantic.types

data class FunctionType(val returnType: Type, val parameters: List<Type>) : Type {
    companion object {
        operator fun invoke(returnType: Type, vararg parameters: Type) = FunctionType(returnType, parameters.toList())

        fun declarationMarker(): FunctionType = FunctionType(VoidType)
        val DEFINITION_MARKER: FunctionType = declarationMarker().apply { defined = true }
    }

    var defined: Boolean = false

    override fun requiresStorage(): Boolean = false

    override fun count(): Int = 0

    override fun decayed(): Type = pointer()

    override fun toString(): String = declaration("")

    override fun declaration(parent: String): String {
        val params = parameters.joinToString(transform = Type::toString, prefix = "(", separator = ",", postfix = ")")
        return if (parent.isPointer()) {
            returnType.declaration("($parent)$params")
        } else {
            returnType.declaration("$parent$params")
        }
    }
}
