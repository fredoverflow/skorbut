package text

fun String.skipDigits(start: Int): Int {
    val n = length
    for (i in start until n) {
        if (this[i] !in '0'..'9') return i
    }
    return n
}
