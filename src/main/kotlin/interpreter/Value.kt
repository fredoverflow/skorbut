package interpreter

import semantic.types.*
import syntax.lexer.Token

data class Object(val segment: Segment, val offset: Int, val type: Type, val index: Int, val bound: Int) {
    init {
        if (index < 0) throw AssertionError("negative index $index")
        if (index > bound) throw AssertionError("index $index out of bounds $bound")
    }

    fun isSentinel(): Boolean = (index == bound)

    operator fun plus(delta: Int): Object = copy(offset = offset + delta * type.count(), index = index + delta)

    operator fun minus(delta: Int): Object = copy(offset = offset - delta * type.count(), index = index - delta)

    fun preventSentinelAccess() {
        if (isSentinel()) throw AssertionError("sentinel element access")
    }

    fun checkReferable(): Object {
        segment.checkAlive()
        return this
    }

    fun isReferable(): Boolean {
        return segment.alive
    }

    fun evaluate(): Value {
        return if (type is ArrayType) {
            // array-to-pointer decay
            PointerValue(copy(type = type.elementType, index = 0, bound = type.length))
        } else {
            preventSentinelAccess()
            val result = segment[offset]
            if (result == IndeterminateValue) throw AssertionError("read from uninitialized variable")
            result
        }
    }

    fun assign(newValue: Value) {
        preventSentinelAccess()
        segment[this] = newValue
    }
}

interface Value {
    fun type(): Type

    fun show(): String

    fun decayed(): Value = this

    companion object {
        fun signedChar(x: Int): ArithmeticValue = ArithmeticValue(x.toDouble(), SignedCharType)
        fun unsignedChar(x: Int): ArithmeticValue = ArithmeticValue(x.toDouble(), UnsignedCharType)

        fun signedShort(x: Int): ArithmeticValue = ArithmeticValue(x.toDouble(), SignedShortType)
        fun unsignedShort(x: Int): ArithmeticValue = ArithmeticValue(x.toDouble(), UnsignedShortType)

        fun signedInt(x: Int): ArithmeticValue = ArithmeticValue(x.toDouble(), SignedIntType)
        fun unsignedInt(x: Int): ArithmeticValue = ArithmeticValue(x.toLong().and(0xffffffff).toDouble(), UnsignedIntType)

        fun float(x: Float): ArithmeticValue = ArithmeticValue(x.toDouble(), FloatType)
        fun double(x: Double): ArithmeticValue = ArithmeticValue(x, DoubleType)

        val ONE = signedInt(1)
        val NUL = signedChar(0)
        val ZERO = signedInt(0)
        val MINUS_ONE = signedInt(-1)

        fun truth(x: Boolean): ArithmeticValue = if (x) ONE else ZERO
    }
}

object VoidValue : Value {
    override fun type(): Type = VoidType

    override fun show(): String = "void"
}

data class ArithmeticValue(val value: Double, val type: ArithmeticType) : Value {
    override fun type(): Type = type

    override fun show(): String = type.show(value)

    operator fun plus(that: ArithmeticValue): ArithmeticValue = ArithmeticValue(value + that.value, type.usualArithmeticConversions(that.type)).trim()

    operator fun minus(that: ArithmeticValue): ArithmeticValue = ArithmeticValue(value - that.value, type.usualArithmeticConversions(that.type)).trim()

    operator fun times(that: ArithmeticValue): ArithmeticValue = ArithmeticValue(value * that.value, type.usualArithmeticConversions(that.type)).trim()

    operator fun div(that: ArithmeticValue): ArithmeticValue = ArithmeticValue(value / that.value, type.usualArithmeticConversions(that.type)).trim()

    operator fun rem(that: ArithmeticValue): ArithmeticValue = ArithmeticValue(value % that.value, type.usualArithmeticConversions(that.type)).trim()

    fun trim(): ArithmeticValue = ArithmeticValue(type.trim(value), type)

    fun integralPromotions(): ArithmeticValue = type.integralPromotions().cast(this)

    fun isFalse(): Boolean = (value == 0.0)

    fun isTrue(): Boolean = (value != 0.0)

    fun normalizeBool(): ArithmeticValue = if (value == 0.0 || value == 1.0) this else Value.ONE
}

data class PointerValue(val referenced: Object) : Value {
    override fun type(): Type = PointerType(referenced.type)

    override fun show(): String = "*"

    operator fun plus(delta: Int): PointerValue = PointerValue(referenced.checkReferable() + delta)

    operator fun minus(delta: Int): PointerValue = PointerValue(referenced.checkReferable() - delta)

    operator fun minus(base: PointerValue): Int {
        if (referenced.segment != base.referenced.segment) throw AssertionError("subtract across segments")
        return (referenced.offset - base.referenced.offset) / referenced.type.count()
    }
}

data class FunctionDesignator(val functionName: Token, val functionType: FunctionType) : Value {
    override fun type(): Type = functionType

    override fun show(): String = error("show on function designator")

    override fun decayed(): Value = FunctionPointerValue(this)
}

data class FunctionPointerValue(val designator: FunctionDesignator) : Value {
    override fun type(): Type = PointerType(designator.functionType)

    override fun show(): String = "&${designator.functionName}"
}

object IndeterminateValue : Value {
    override fun type(): Type = throw AssertionError("indeterminate value has no type")

    override fun show(): String = ""
}
