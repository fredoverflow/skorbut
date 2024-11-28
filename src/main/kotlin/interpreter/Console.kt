package interpreter

import semantic.types.DoubleType
import semantic.types.FloatType
import semantic.types.SignedCharType
import semantic.types.SignedIntType
import syntax.lexer.Token
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

    fun puts(str: PointerValue) {
        print(stringStartingAt(str))
        putchar('\n')
    }

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
                k = fmt.skipDigits(i) // width
                if (fmt[k] == '.') {
                    k = fmt.skipDigits(k + 1) // precision
                }
                val specifier = fmt.substring(i - 1, k + 1)
                sb.append(formatValue(args.next(), specifier))
            }
            i = k + 1
            k = fmt.indexOf('%', i)
        }
        sb.append(fmt, i, fmt.length)
        print(sb)
        return sb.length
    }

    private fun formatValue(value: Value, specifier: String): String {
        return when (specifier.last()) {
            'c' -> specifier.format((value as ArithmeticValue).value.toLong().toInt().and(0xff))

            'i' -> specifier.replace('i', 'd').format((value as ArithmeticValue).value.toLong().toInt())

            'u' -> specifier.replace('u', 'd').format((value as ArithmeticValue).value.toLong().and(0xffffffff))

            'd', 'o', 'x', 'X' -> specifier.format((value as ArithmeticValue).value.toLong().toInt())

            'e', 'E', 'f', 'g', 'G' -> specifier.format(java.util.Locale.ENGLISH, (value as ArithmeticValue).value)

            's' -> specifier.format(stringStartingAt(value as PointerValue))

            'p' -> specifier.replace('p', 's').format(value.show())

            else -> error("illegal conversion specifier %${specifier.last()}")
        }
    }

    private fun stringStartingAt(start: PointerValue): CharSequence {
        val sb = StringBuilder()
        var ptr = start
        var x = (ptr.referenced.evaluate() as ArithmeticValue).value
        while (x != 0.0) {
            sb.append(x.toInt().and(0xff).toChar())
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
                '\t', '\n', ' ' -> skipWhitespace()

                '%' -> {
                    val percent = i - 1
                    if (i == fmt.length) format.stringErrorAt(percent, "incomplete conversion specifier")
                    if (a == arguments.size) format.stringErrorAt(percent, "missing argument after format string")
                    val arg = arguments[a++] as PointerValue
                    val referenced = arg.referenced
                    val type = referenced.type
                    when (fmt[i++]) {
                        'd' -> {
                            if (type !== SignedIntType) format.stringErrorAt(percent, "%d expects int*, not ${type.pointer()}")
                            skipWhitespace()
                            val x = scanInt() ?: return a - 1
                            referenced.assign(Value.signedInt(x))
                        }

                        'f' -> {
                            if (type !== FloatType) format.stringErrorAt(percent, "%f expects float*, not ${type.pointer()}")
                            skipWhitespace()
                            val x = scanDouble() ?: return a - 1
                            referenced.assign(Value.float(x.toFloat()))
                        }

                        'l' -> {
                            if (i == fmt.length || fmt[i] != 'f') format.stringErrorAt(i, "missing f after %l")
                            ++i
                            if (type !== DoubleType) format.stringErrorAt(percent, "%lf expects double*, not ${type.pointer()}")
                            skipWhitespace()
                            val x = scanDouble() ?: return a - 1
                            referenced.assign(Value.double(x))
                        }

                        'c' -> {
                            if (type !== SignedCharType) format.stringErrorAt(percent, "%c expects char*, not ${type.pointer()}")
                            referenced.assign(Value.signedChar(getchar()))
                        }

                        's' -> {
                            if (type !== SignedCharType) format.stringErrorAt(percent, "%s expects char*, not ${type.pointer()}")
                            val maxLen = referenced.bound - referenced.index - 1
                            format.stringErrorAt(percent, "%s is unsafe\nuse %${maxLen}s instead")
                        }

                        '1', '2', '3', '4', '5', '6', '7', '8', '9' -> {
                            var len = fmt[i - 1] - '0'
                            while (i < fmt.length && fmt[i] in '0'..'9') {
                                len = len * 10 + (fmt[i++] - '0')
                            }
                            if (i == fmt.length || fmt[i] != 's') format.stringErrorAt(i, "missing s after %$len")
                            ++i
                            if (type !== SignedCharType) format.stringErrorAt(percent, "%s expects char*, not ${type.pointer()}")
                            val maxLen = referenced.bound - referenced.index - 1
                            if (len > maxLen) format.stringErrorAt(percent, "%${len}s is ${len - maxLen} too long\nuse %${maxLen}s instead")
                            skipWhitespace()
                            val x = scanString(len)
                            var obj = referenced
                            for (c in x) {
                                obj.assign(Value.signedChar(c))
                                obj += 1
                            }
                            obj.assign(Value.NUL)
                        }

                        else -> format.stringErrorAt(percent, "illegal conversion specifier %${fmt[i - 1]}")
                    }
                    after?.invoke()
                }

                else -> if (getchar() != fmt[i - 1]) {
                    unget()
                    return a
                }
            }
        }
        return arguments.size
    }

    private fun skipWhitespace() {
        @Suppress("ControlFlowWithEmptyBody")
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
        while (getchar() > ' ') {
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
            in '\u0020'..'\u007e' -> input.append(x)
            in '\u00a0'..'\u00ff' -> input.append(x)

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
