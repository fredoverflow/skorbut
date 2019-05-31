package interpreter

import semantic.types.DoubleType
import semantic.types.FloatType
import semantic.types.SignedCharType
import semantic.types.SignedIntType
import syntax.lexer.Token
import text.parseInt
import text.skipDigits

import java.util.concurrent.LinkedBlockingDeque
import java.util.concurrent.atomic.AtomicBoolean

class Console {
    private val output = StringBuilder()

    var isDirty: Boolean = false
        private set

    private var input = StringBuilder()

    private val queue = LinkedBlockingDeque<Char>()
    private val blocked = AtomicBoolean(false)

    fun isBlocked(): Boolean = blocked.get()

    var update: Function0<Unit>? = null

    fun putchar(x: Char) {
        output.append(x)
        isDirty = true
    }

    fun print(x: CharSequence) {
        output.append(x)
        isDirty = true
    }

    fun printf(format: Token, arguments: List<Value>): Int {
        val sb = StringBuilder()
        val args = arguments.iterator()
        val fmt = format.text
        var i = 0
        var k = fmt.indexOf('%')
        while (k != -1) {
            sb.append(fmt, i, k)
            if (fmt[++k] == '%') {
                sb.append('%')
            } else {
                i = k
                val fill = if (fmt[i] == '0') '0' else ' '
                k = fmt.skipDigits(i)
                val width = fmt.parseInt(i, k)
                val str = formatValue(args.next(), fmt[k])
                repeat(width - str.length) { sb.append(fill) }
                sb.append(str)
            }
            i = k + 1
            k = fmt.indexOf('%', i)
        }
        sb.append(fmt, i, fmt.length)
        print(sb)
        return sb.length
    }

    private fun formatValue(value: Value, specifier: Char): CharSequence {
        return when (specifier) {
            'c' -> (value as ArithmeticValue).value.toLong().and(0xff).toChar().toString()
            'd' -> (value as ArithmeticValue).value.toLong().toInt().toString()
            'u' -> (value as ArithmeticValue).value.toLong().and(0xffffffff).toString()
            'x' -> Integer.toHexString((value as ArithmeticValue).value.toLong().toInt())
            'f' -> "%f".format((value as ArithmeticValue).value).replace(',', '.')
            's' -> stringStartingAt(value as PointerValue)
            else -> error("illegal conversion specifier %$specifier")
        }
    }

    private fun stringStartingAt(start: PointerValue): CharSequence {
        val sb = StringBuilder()
        var ptr = start
        var x = (ptr.referenced.evaluate() as ArithmeticValue).value
        while (x != 0.0) {
            sb.append(x.toChar())
            ptr += 1
            if (ptr.referenced.isSentinel()) error("missing NUL terminator")
            x = (ptr.referenced.evaluate() as ArithmeticValue).value
        }
        return sb
    }

    fun scanf(format: Token, arguments: List<Value>, after: Function0<Unit>?): Int {
        val fmt = format.text
        var i = 0
        var a = 0
        while (i < fmt.length) {
            when (fmt[i++]) {
                ' ' -> skipWhitespace()
                '%' -> {
                    if (i == fmt.length) format.error("trailing %")
                    if (a == arguments.size) format.error("missing scanf argument")
                    val arg = arguments[a++] as PointerValue
                    val type = arg.referenced.type
                    when (fmt[i++]) {
                        'd' -> {
                            if (type !== SignedIntType) format.error("%d expects ${SignedIntType.pointer()}, not ${type.pointer()}")
                            skipWhitespace()
                            val x = scanInt() ?: return a - 1
                            arg.referenced.assign(Value.signedInt(x))
                        }
                        'f' -> {
                            if (type !== FloatType) format.error("%f expects ${FloatType.pointer()}, not ${type.pointer()}")
                            skipWhitespace()
                            val x = scanDouble() ?: return a - 1
                            arg.referenced.assign(Value.float(x.toFloat()))
                        }
                        'l' -> {
                            if (fmt[i++] != 'f') format.error("l must be followed by f")
                            if (type !== DoubleType) format.error("%f expects ${DoubleType.pointer()}, not ${type.pointer()}")
                            skipWhitespace()
                            val x = scanDouble() ?: return a - 1
                            arg.referenced.assign(Value.double(x))
                        }
                        'c' -> {
                            if (type !== SignedCharType) format.error("%f expects ${SignedCharType.pointer()}, not ${type.pointer()}")
                            arg.referenced.assign(Value.signedChar(getchar().toInt()))
                        }
                        's' -> {
                            format.error("%s is unsafe, please use %123s instead, where 123 is the maximum length")
                        }
                        '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            var len = fmt[i - 1] - '0'
                            while (fmt[i] in '0'..'9') {
                                len = len * 10 + (fmt[i++] - '0')
                            }
                            if (fmt[i++] != 's') format.error("Did you forget to put the s after %$len?")
                            if (type !== SignedCharType) format.error("%s expects ${SignedCharType.pointer()}, not ${type.pointer()}")
                            skipWhitespace()
                            val x = scanString(len)
                            var obj = arg.referenced
                            for (c in x) {
                                obj.assign(Value.signedChar(c.toInt()))
                                obj += 1
                            }
                            obj.assign(Value.NUL)
                        }
                        else -> format.error("illegal conversion specifier %${fmt[i - 1]}")
                    }
                    after?.invoke()
                }
                else -> if (getchar() != fmt[i - 1]) return a
            }
        }
        return arguments.size
    }

    fun skipWhitespace() {
        while (getchar().isWhitespace()) {
        }
        unget()
    }

    private fun scanInt(): Int? {
        var c = getchar()
        var sign = 1
        if (c == '-') {
            sign = -1
            c = getchar()
        }
        if (c !in '0'..'9') return null
        var x = c - '0'
        while (getchar() in '0'..'9') {
            x = x * 10 + (current - '0')
        }
        unget()
        return sign * x
    }

    private fun scanDouble(): Double? {
        var c = getchar()
        var sign = 1
        if (c == '-') {
            sign = -1
            c = getchar()
        }
        if (c !in '0'..'9') return null
        var x = (c - '0').toDouble()
        while (getchar() in '0'..'9') {
            x = x * 10 + (current - '0')
        }
        if (current == '.') {
            var decimal = 1.0
            while (getchar() in '0'..'9') {
                decimal /= 10
                x += (current - '0') * decimal
            }
        }
        unget()
        return sign * x
    }

    private fun scanString(len: Int): CharSequence {
        val sb = StringBuilder()
        while (getchar() in '!'..'~') {
            sb.append(current)
            if (sb.length == len) return sb
        }
        unget()
        return sb
    }

    fun getText(): String {
        isDirty = false
        if (!isBlocked()) return output.toString()

        val result = StringBuilder()
        result.append(output)
        result.append(input)
        result.append('_')
        return result.toString()
    }

    fun keyTyped(x: Char) {
        when (x) {
            in ' '..'~' -> input.append(x)
            '\b' -> backspace()
            '\n' -> enter()
            '\u0004', '\u001a' -> stop()
        }
    }

    private fun backspace() {
        val len = input.length
        if (len > 0) {
            input.setLength(len - 1)
        }
    }

    private fun enter() {
        input.append('\n')
        output.append(input)
        val temp = input
        input = StringBuilder()
        blocked.set(false)
        for (x in temp) {
            queue.put(x)
        }
    }

    fun stop() {
        queue.put('\uffff')
    }

    fun getchar(): Char {
        val x: Char? = queue.poll()
        if (x != null) return remember(x)

        blocked.set(true)
        update?.invoke()
        val y: Char = queue.take()
        return remember(y)
    }

    private fun remember(x: Char): Char {
        current = x
        return x
    }

    private var current = '\u0000'

    private fun unget() {
        queue.putFirst(current)
        current = '\u0000'
    }
}
