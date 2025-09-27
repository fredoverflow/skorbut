package syntax.lexer

import java.lang.Long.lowestOneBit
import java.lang.Long.numberOfTrailingZeros

private val TokenKind.bitmask: Long
    get() {
        assert(ordinal < Long.SIZE_BITS) { "$this@$ordinal is not among the first ${Long.SIZE_BITS} enum constants" }
        return 1L shl ordinal
    }

class TokenKindSet(val bits: Long) {
    companion object {
        val EMPTY = TokenKindSet(0L)

        fun of(kind: TokenKind): TokenKindSet {
            return TokenKindSet(kind.bitmask)
        }

        fun of(kind1: TokenKind, kind2: TokenKind): TokenKindSet {
            return TokenKindSet(kind1.bitmask or kind2.bitmask)
        }

        fun of(kind1: TokenKind, kind2: TokenKind, kind3: TokenKind): TokenKindSet {
            return TokenKindSet(kind1.bitmask or kind2.bitmask or kind3.bitmask)
        }

        fun of(kind1: TokenKind, kind2: TokenKind, kind3: TokenKind, kind4: TokenKind): TokenKindSet {
            return TokenKindSet(kind1.bitmask or kind2.bitmask or kind3.bitmask or kind4.bitmask)
        }

        fun of(kind1: TokenKind, kind2: TokenKind, kind3: TokenKind, kind4: TokenKind, kind5: TokenKind): TokenKindSet {
            return TokenKindSet(kind1.bitmask or kind2.bitmask or kind3.bitmask or kind4.bitmask or kind5.bitmask)
        }
    }

    fun contains(kind: TokenKind): Boolean {
        return (bits and kind.bitmask) != 0L
    }

    operator fun plus(kind: TokenKind): TokenKindSet {
        return TokenKindSet(bits or kind.bitmask)
    }

    fun isEmpty(): Boolean {
        return bits == 0L
    }

    fun first(): TokenKind {
        return TokenKind.entries[numberOfTrailingZeros(bits)]
    }

    override fun equals(other: Any?): Boolean {
        return other is TokenKindSet && this.bits == other.bits
    }

    override fun hashCode(): Int {
        return (bits * 0x10418282f).ushr(32).toInt()
    }

    override fun toString(): String {
        return generateSequence(bits) { b -> b xor lowestOneBit(b) }
            .takeWhile { b -> b != 0L }
            .map { b -> TokenKind.entries[numberOfTrailingZeros(b)] }
            .joinToString(", ", "[", "]")
    }
}
