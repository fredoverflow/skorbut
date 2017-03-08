package semantic.types

class Typedef(val aliased: Type) : Type {
    override fun requiresStorage(): Boolean = false

    override fun count(): Int = 0
}

val TypedefSignedIntType = Typedef(SignedIntType)

object MarkerIsTypedefName : Type

object MarkerNotTypedefName : Type
