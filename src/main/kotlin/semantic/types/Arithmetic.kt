package semantic.types

import interpreter.ArithmeticValue
import interpreter.Value
import text.quote

abstract class ArithmeticType : Type {
    abstract fun show(value: Double): String

    abstract val defaultValue: ArithmeticValue

    abstract fun rank(): Int

    open fun isIntegral(): Boolean = true

    abstract fun trim(x: Double): Double

    override fun canCastFromDecayed(source: Type): Boolean = source is ArithmeticType

    override fun cast(source: Value): Value = cast(source as ArithmeticValue)

    fun cast(source: ArithmeticValue): ArithmeticValue = if (source.type === this) source else ArithmeticValue(trim(source.value), this)

    fun integralPromotions(): ArithmeticType = this.max(SignedIntType)

    fun usualArithmeticConversions(that: ArithmeticType): ArithmeticType = integralPromotions().max(that)

    fun max(that: ArithmeticType): ArithmeticType = if (this.rank() < that.rank()) that else this
}

object SignedCharType : ArithmeticType() {
    override fun sizeof(): Int = 1

    override fun show(value: Double): String = value.toChar().quote()

    override val defaultValue: ArithmeticValue by lazy { Value.signedChar(0) }

    override fun rank(): Int = 0

    override fun trim(x: Double): Double {
        if (x < -128.0) throw ArithmeticException("char underflow $x")
        if (x > +127.0) throw ArithmeticException("char overflow $x")
        return x.toByte().toDouble()
    }

    override fun toString(): String = "char"
}

object UnsignedCharType : ArithmeticType() {
    override fun sizeof(): Int = 1

    override fun show(value: Double): String = "x%02X".format(value.toInt())

    override val defaultValue: ArithmeticValue = Value.unsignedChar(0)

    override fun rank(): Int = 1

    override fun trim(x: Double): Double = x.toInt().and(0xff).toDouble()

    override fun toString(): String = "unsigned char"
}

object SignedShortType : ArithmeticType() {
    override fun sizeof(): Int = 2

    override fun show(value: Double): String = value.toInt().toString()

    override val defaultValue: ArithmeticValue = Value.signedShort(0)

    override fun rank(): Int = 2

    override fun trim(x: Double): Double {
        if (x < -32768.0) throw ArithmeticException("short underflow $x")
        if (x > +32767.0) throw ArithmeticException("short overflow $x")
        return x.toShort().toDouble()
    }

    override fun toString(): String = "short"
}

object UnsignedShortType : ArithmeticType() {
    override fun sizeof(): Int = 2

    override fun show(value: Double): String = value.toInt().toString()

    override val defaultValue: ArithmeticValue = Value.unsignedShort(0)

    override fun rank(): Int = 3

    override fun trim(x: Double): Double = x.toInt().and(0xffff).toDouble()

    override fun toString(): String = "unsigned short"
}

object SignedIntType : ArithmeticType() {
    override fun sizeof(): Int = 4

    override fun show(value: Double): String = value.toInt().toString()

    override val defaultValue: ArithmeticValue = Value.signedInt(0)

    override fun rank(): Int = 4

    override fun trim(x: Double): Double {
        if (x < -2147483648.0) throw ArithmeticException("int underflow $x")
        if (x > +2147483647.0) throw ArithmeticException("int overflow $x")
        return x.toInt().toDouble()
    }

    override fun toString(): String = "int"
}

object UnsignedIntType : ArithmeticType() {
    override fun sizeof(): Int = 4

    override fun show(value: Double): String = value.toLong().toString()

    override val defaultValue: ArithmeticValue = Value.unsignedInt(0)

    override fun rank(): Int = 5

    override fun trim(x: Double): Double = x.toLong().and(0xffffffff).toDouble()

    override fun toString(): String = "unsigned int"
}

object FloatType : ArithmeticType() {
    override fun sizeof(): Int = 4

    override fun show(value: Double): String = value.toFloat().toString()

    override val defaultValue: ArithmeticValue = Value.float(0f)

    override fun rank(): Int = 8

    override fun isIntegral(): Boolean = false

    override fun trim(x: Double): Double = x.toFloat().toDouble()

    override fun toString(): String = "float"
}

object DoubleType : ArithmeticType() {
    override fun sizeof(): Int = 8

    override fun show(value: Double): String = value.toString()

    override val defaultValue: ArithmeticValue = Value.double(0.0)

    override fun rank(): Int = 9

    override fun isIntegral(): Boolean = false

    override fun trim(x: Double): Double = x

    override fun toString(): String = "double"
}
