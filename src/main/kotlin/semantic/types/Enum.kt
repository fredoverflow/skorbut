package semantic.types

import interpreter.ArithmeticValue

class EnumerationConstant(val value: ArithmeticValue) : Type {
    override fun requiresStorage(): Boolean = false

    override fun count(): Int = 0
}
