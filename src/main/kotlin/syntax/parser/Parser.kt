package syntax.parser

import common.Diagnostic
import semantic.SymbolTable
import syntax.lexer.*

class Parser(private val lexer: Lexer) {
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

    fun accept(): Token {
        val result = token
        next()
        return result
    }

    fun expect(expected: Byte): Token {
        if (current != expected) throw Diagnostic(previousEnd, "expected ${expected.show()}")
        return accept()
    }

    fun <T> T.semicolon(): T {
        expect(SEMICOLON)
        return this
    }

    infix fun <T> T.before(expected: Byte): T {
        expect(expected)
        return this
    }

    fun illegalStartOf(rule: String): Nothing {
        token.error("illegal start of $rule")
    }

    fun notImplementedYet(feature: String): Nothing {
        token.error("$feature not implemented yet")
    }

    inline fun <T> commaSeparatedList1(first: T, parse: () -> T): List<T> {
        if (current != COMMA) return listOf(first)

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
            return emptyList()
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

    inline fun <T> list1While(proceed: () -> Boolean, parse: () -> T): List<T> {
        val first = parse()
        if (!proceed()) return listOf(first)

        val list = ArrayList<T>()
        list.add(first)
        do {
            list.add(parse())
        } while (proceed())
        return list
    }

    inline fun <T> list0While(proceed: () -> Boolean, parse: () -> T): List<T> {
        if (!proceed()) {
            return emptyList()
        } else {
            return list1While(proceed, parse)
        }
    }

    inline fun <T> list1Until(terminator: Byte, parse: () -> T): List<T> {
        return list1While({ current != terminator }, parse)
    }

    inline fun <T> list0Until(terminator: Byte, parse: () -> T): List<T> {
        return list0While({ current != terminator }, parse)
    }

    inline fun collectWhile(proceed: () -> Boolean): List<Token> {
        return list0While(proceed, ::accept)
    }

    inline fun <T> parenthesized(parse: () -> T): T {
        expect(OPENING_PAREN)
        val result = parse()
        expect(CLOSING_PAREN)
        return result
    }

    inline fun <T> braced(parse: () -> T): T {
        expect(OPENING_BRACE)
        val result = parse()
        expect(CLOSING_BRACE)
        return result
    }

    infix fun <T> (() -> T).optionalBefore(terminator: Byte): T? {
        if (current == terminator) {
            next()
            return null
        } else {
            val result = this()
            expect(terminator)
            return result
        }
    }

    inline fun <T> optional(indicator: Byte, parse: () -> T): T? {
        if (current != indicator) {
            return null
        } else {
            next()
            return parse()
        }
    }

    val symbolTable = SymbolTable()

    var acceptableSpecifiers = 0
    var declaratorOptional = false
}
