package interpreter

import semantic.types.Type

class Segment(val type: Type) {
    private val memory: MutableList<Value> = generateSequence { IndeterminateValue }.take(type.count()).toMutableList()
    var readOnlyErrorMessage: String? = null

    var alive = true
        private set

    fun kill() {
        assert(alive)
        alive = false
    }

    fun checkAlive() {
        if (!alive) throw AssertionError("dangling pointer")
    }

    fun count(): Int {
        return type.count()
    }

    operator fun get(offset: Int): Value {
        checkAlive()
        return memory[offset]
    }

    operator fun set(offset: Int, newValue: Value) {
        checkAlive()
        if (readOnlyErrorMessage != null) throw AssertionError(readOnlyErrorMessage)
        val oldValue = memory[offset]
        assert(oldValue === IndeterminateValue || oldValue.type() == newValue.type()) {
            "$oldValue === $IndeterminateValue || ${oldValue.type()} == ${newValue.type()}"
        }
        memory[offset] = newValue
    }

    operator fun set(obj: Object, newValue: Value) {
        set(obj.offset, newValue)
    }
}
