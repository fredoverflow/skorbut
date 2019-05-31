package text

fun String.skipDigits(start: Int): Int {
    val n = length
    for (i in start until n) {
        if (this[i] !in '0'..'9') return i
    }
    return n
}

fun String.parseInt(start: Int, end: Int): Int {
    var x = 0
    for (i in start until end) {
        x = x * 10 + (this[i] - '0')
    }
    return x
}
