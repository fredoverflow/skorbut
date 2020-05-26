package semantic.types

data class FunctionType(val parameters: List<Type>, val returnType: Type) : Type {
    companion object {
        fun nullary(returnType: Type): FunctionType = FunctionType(emptyList(), returnType)
        fun unary(x: Type, returnType: Type): FunctionType = FunctionType(listOf(x), returnType)
        fun binary(x: Type, y: Type, returnType: Type): FunctionType = FunctionType(listOf(x, y), returnType)

        fun declarationMarker(): FunctionType = nullary(VoidType)
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
