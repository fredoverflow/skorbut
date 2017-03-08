package semantic.types

import java.util.Collections

data class FunctionType(val parameters: List<Type>, val returnType: Type) : Type {
    companion object {
        fun nullary(returnType: Type): FunctionType = FunctionType(Collections.emptyList(), returnType)
        fun unary(x: Type, returnType: Type): FunctionType = FunctionType(Collections.singletonList(x), returnType)
        fun binary(x: Type, y: Type, returnType: Type): FunctionType = FunctionType(listOf(x, y), returnType)
    }

    var defined: Boolean = false

    override fun requiresStorage(): Boolean = false

    override fun count(): Int = 0

    override fun decayed(): Type = pointer()

    override fun toString(): String = parameters.asSequence().plus(returnType).joinToString(", ", "Function<", ">")
}
