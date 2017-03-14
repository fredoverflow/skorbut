package syntax.parser

class ByteMap<V> {
    private val table = arrayOfNulls<Any?>(128)

    operator fun set(index: Byte, value: V) {
        val i = index.toInt()
        assert(table[i] == null)
        table[i] = value
    }

    operator fun set(vararg indexes: Byte, value: V) {
        for (index in indexes) {
            set(index, value)
        }
    }

    operator fun get(index: Byte): V? {
        val i = index.toInt()
        @Suppress("UNCHECKED_CAST")
        return table[i] as V?
    }
}
