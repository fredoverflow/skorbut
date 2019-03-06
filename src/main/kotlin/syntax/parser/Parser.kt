package syntax.parser

import common.Diagnostic
import semantic.SymbolTable
import syntax.lexer.Lexer
import syntax.lexer.Token
import syntax.lexer.TokenKind
import syntax.lexer.TokenKind.*
import syntax.lexer.nextToken

class Parser(private val lexer: Lexer) {
    private var previousEnd: Int = 0

    var token: Token = lexer.nextToken()
        private set

    var current: TokenKind = token.kind
        private set

    var lookahead: Token = lexer.nextToken()
        private set

    fun next(): TokenKind {
        previousEnd = token.end
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

    fun expect(expected: TokenKind): Token {
        if (current != expected) throw Diagnostic(previousEnd, "expected $expected")
        return accept()
    }

    fun <T> T.semicolon(): T {
        expect(SEMICOLON)
        return this
    }

    infix fun <T> T.before(expected: TokenKind): T {
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
        val list = mutableListOf(first)
        while (current == COMMA) {
            next()
            list.add(parse())
        }
        return list
    }

    inline fun <T> commaSeparatedList1(parse: () -> T): List<T> {
        return commaSeparatedList1(parse(), parse)
    }

    inline fun <T> commaSeparatedList0(terminator: TokenKind, parse: () -> T): List<T> {
        return if (current == terminator) {
            emptyList()
        } else {
            commaSeparatedList1(parse)
        }
    }

    inline fun <T> trailingCommaSeparatedList1(terminator: TokenKind, parse: () -> T): List<T> {
        val list = mutableListOf(parse())
        while (current == COMMA && next() != terminator) {
            list.add(parse())
        }
        return list
    }

    inline fun <T> list1While(proceed: () -> Boolean, parse: () -> T): List<T> {
        val list = mutableListOf(parse())
        while (proceed()) {
            list.add(parse())
        }
        return list
    }

    inline fun <T> list0While(proceed: () -> Boolean, parse: () -> T): List<T> {
        return if (!proceed()) {
            emptyList()
        } else {
            list1While(proceed, parse)
        }
    }

    inline fun <T> list1Until(terminator: TokenKind, parse: () -> T): List<T> {
        return list1While({ current != terminator }, parse)
    }

    inline fun <T> list0Until(terminator: TokenKind, parse: () -> T): List<T> {
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

    infix fun <T> (() -> T).optionalBefore(terminator: TokenKind): T? {
        return if (current == terminator) {
            next()
            null
        } else {
            this() before terminator
        }
    }

    inline fun <T> optional(indicator: TokenKind, parse: () -> T): T? {
        return if (current != indicator) {
            null
        } else {
            next()
            parse()
        }
    }

    val symbolTable = SymbolTable()
}
