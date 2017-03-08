package syntax

import common.Diagnostic
import java.util.*

abstract class ParserBase(private val lexer: Lexer) {
    private var previousEnd: Int = 0

    protected var token: Token = lexer.nextToken()
        private set

    protected var current: Byte = token.kind
        private set

    protected var lookahead: Token = lexer.nextToken()
        private set

    protected fun next(): Byte {
        previousEnd = token.end()
        token = lookahead
        current = token.kind
        lookahead = lexer.nextToken()
        return current
    }

    protected fun <Result> consume(result: Result): Result {
        next()
        return result
    }

    protected fun expect(expected: Byte): Token {
        if (current != expected) throw Diagnostic(previousEnd, "expected ${expected.show()}")
        return consume(token)
    }

    protected fun illegalStartOf(rule: String): Nothing {
        token.error("illegal start of $rule")
    }

    protected fun notImplementedYet(feature: String): Nothing {
        token.error("$feature not implemented yet")
    }

    protected inline fun <T> commaSeparatedList1(first: T, parse: () -> T): List<T> {
        if (current != COMMA) return Collections.singletonList(first)

        val list = ArrayList<T>()
        list.add(first)
        do {
            next()
            list.add(parse())
        } while (current == COMMA)
        return list
    }

    protected inline fun <T> commaSeparatedList1(parse: () -> T): List<T> {
        return commaSeparatedList1(parse(), parse)
    }

    protected inline fun <T> commaSeparatedList0(terminator: Byte, parse: () -> T): List<T> {
        if (current == terminator) {
            return Collections.emptyList()
        } else {
            return commaSeparatedList1(parse)
        }
    }

    protected inline fun <T> trailingCommaSeparatedList1(terminator: Byte, parse: () -> T): List<T> {
        val list = ArrayList<T>()
        list.add(parse())
        while (current == COMMA && next() != terminator) {
            list.add(parse())
        }
        return list
    }

    protected inline fun <T> list1While(good: (Byte) -> Boolean, parse: () -> T): List<T> {
        val first = parse()
        if (!good(current)) return Collections.singletonList(first)

        val list = ArrayList<T>()
        list.add(first)
        do {
            list.add(parse())
        } while (good(current))
        return list
    }

    protected inline fun <T> list0While(good: (Byte) -> Boolean, parse: () -> T): List<T> {
        if (!good(current)) {
            return Collections.emptyList()
        } else {
            return list1While(good, parse)
        }
    }

    protected inline fun <T> list1Until(terminator: Byte, parse: () -> T): List<T> {
        return list1While({ it != terminator }, parse)
    }

    protected inline fun <T> list0Until(terminator: Byte, parse: () -> T): List<T> {
        return list0While({ it != terminator }, parse)
    }

    protected inline fun collectWhile(good: (Byte) -> Boolean): List<Token> {
        return list0While(good) { consume(token) }
    }

    protected inline fun <T> parenthesized(parse: () -> T): T {
        expect(OPEN_PAREN)
        val result = parse()
        expect(CLOSE_PAREN)
        return result
    }

    protected inline fun <T> bracketed(parse: () -> T): T {
        expect(OPEN_BRACKET)
        val result = parse()
        expect(CLOSE_BRACKET)
        return result
    }

    protected inline fun <T> braced(parse: () -> T): T {
        expect(OPEN_BRACE)
        val result = parse()
        expect(CLOSE_BRACE)
        return result
    }

    protected inline fun <T> unless(terminator: Byte, parse: () -> T): T? {
        if (current == terminator) {
            next()
            return null
        } else {
            val result = parse()
            expect(terminator)
            return result
        }
    }
}
