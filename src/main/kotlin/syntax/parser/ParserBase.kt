package syntax.parser

import common.Diagnostic
import syntax.*
import java.util.*

abstract class ParserBase(private val lexer: Lexer) {
    private var previousEnd: Int = 0

    var token: Token = lexer.nextToken()
        private set

    var current: Byte = token.kind
        private set

    var lookahead: Token = lexer.nextToken()
        private set

    fun next(): Byte {
        previousEnd = token.end()
        token = lookahead
        current = token.kind
        lookahead = lexer.nextToken()
        return current
    }

    fun <Result> consume(result: Result): Result {
        next()
        return result
    }

    fun expect(expected: Byte): Token {
        if (current != expected) throw Diagnostic(previousEnd, "expected ${expected.show()}")
        return consume(token)
    }

    fun illegalStartOf(rule: String): Nothing {
        token.error("illegal start of $rule")
    }

    fun notImplementedYet(feature: String): Nothing {
        token.error("$feature not implemented yet")
    }

    inline fun <T> commaSeparatedList1(first: T, parse: () -> T): List<T> {
        if (current != COMMA) return Collections.singletonList(first)

        val list = ArrayList<T>()
        list.add(first)
        do {
            next()
            list.add(parse())
        } while (current == COMMA)
        return list
    }

    inline fun <T> commaSeparatedList1(parse: () -> T): List<T> {
        return commaSeparatedList1(parse(), parse)
    }

    inline fun <T> commaSeparatedList0(terminator: Byte, parse: () -> T): List<T> {
        if (current == terminator) {
            return Collections.emptyList()
        } else {
            return commaSeparatedList1(parse)
        }
    }

    inline fun <T> trailingCommaSeparatedList1(terminator: Byte, parse: () -> T): List<T> {
        val list = ArrayList<T>()
        list.add(parse())
        while (current == COMMA && next() != terminator) {
            list.add(parse())
        }
        return list
    }

    inline fun <T> list1While(good: (Byte) -> Boolean, parse: () -> T): List<T> {
        val first = parse()
        if (!good(current)) return Collections.singletonList(first)

        val list = ArrayList<T>()
        list.add(first)
        do {
            list.add(parse())
        } while (good(current))
        return list
    }

    inline fun <T> list0While(good: (Byte) -> Boolean, parse: () -> T): List<T> {
        if (!good(current)) {
            return Collections.emptyList()
        } else {
            return list1While(good, parse)
        }
    }

    inline fun <T> list1Until(terminator: Byte, parse: () -> T): List<T> {
        return list1While({ it != terminator }, parse)
    }

    inline fun <T> list0Until(terminator: Byte, parse: () -> T): List<T> {
        return list0While({ it != terminator }, parse)
    }

    inline fun collectWhile(good: (Byte) -> Boolean): List<Token> {
        return list0While(good) { consume(token) }
    }

    inline fun <T> parenthesized(parse: () -> T): T {
        expect(OPEN_PAREN)
        val result = parse()
        expect(CLOSE_PAREN)
        return result
    }

    inline fun <T> braced(parse: () -> T): T {
        expect(OPEN_BRACE)
        val result = parse()
        expect(CLOSE_BRACE)
        return result
    }

    inline fun <T> unless(terminator: Byte, parse: () -> T): T? {
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
