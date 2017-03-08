package text

fun String.quote(): String {
    val sb = StringBuilder()
    sb.append('"')
    for (x in this) {
        val q = when (x) {
            '\u0000' -> "\\0"
            '\u0007' -> "\\a"
            '\u0008' -> "\\b"
            '\u0009' -> "\\t"
            '\u000a' -> "\\n"
            '\u000b' -> "\\v"
            '\u000c' -> "\\f"
            '\u000d' -> "\\r"
            ' ' -> " "
            '!' -> "!"
            '\"' -> "\\\""
            '#' -> "#"
            '$' -> "$"
            '%' -> "%"
            '&' -> "&"
            '\'' -> "'"
            '(' -> "("
            ')' -> ")"
            '*' -> "*"
            '+' -> "+"
            ',' -> ","
            '-' -> "-"
            '.' -> "."
            '/' -> "/"
            '0' -> "0"
            '1' -> "1"
            '2' -> "2"
            '3' -> "3"
            '4' -> "4"
            '5' -> "5"
            '6' -> "6"
            '7' -> "7"
            '8' -> "8"
            '9' -> "9"
            ':' -> ":"
            ';' -> ";"
            '<' -> "<"
            '=' -> "="
            '>' -> ">"
            '?' -> "?"
            '@' -> "@"
            'A' -> "A"
            'B' -> "B"
            'C' -> "C"
            'D' -> "D"
            'E' -> "E"
            'F' -> "F"
            'G' -> "G"
            'H' -> "H"
            'I' -> "I"
            'J' -> "J"
            'K' -> "K"
            'L' -> "L"
            'M' -> "M"
            'N' -> "N"
            'O' -> "O"
            'P' -> "P"
            'Q' -> "Q"
            'R' -> "R"
            'S' -> "S"
            'T' -> "T"
            'U' -> "U"
            'V' -> "V"
            'W' -> "W"
            'X' -> "X"
            'Y' -> "Y"
            'Z' -> "Z"
            '[' -> "["
            '\\' -> "\\\\"
            ']' -> "]"
            '^' -> "^"
            '_' -> "_"
            '`' -> "`"
            'a' -> "a"
            'b' -> "b"
            'c' -> "c"
            'd' -> "d"
            'e' -> "e"
            'f' -> "f"
            'g' -> "g"
            'h' -> "h"
            'i' -> "i"
            'j' -> "j"
            'k' -> "k"
            'l' -> "l"
            'm' -> "m"
            'n' -> "n"
            'o' -> "o"
            'p' -> "p"
            'q' -> "q"
            'r' -> "r"
            's' -> "s"
            't' -> "t"
            'u' -> "u"
            'v' -> "v"
            'w' -> "w"
            'x' -> "x"
            'y' -> "y"
            'z' -> "z"
            '{' -> "{"
            '|' -> "|"
            '}' -> "}"
            '~' -> "~"

            else -> "\\${Integer.toOctalString(toInt())}"
        }
        sb.append(q)
    }
    return sb.append('"').toString()
}

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

fun String.indexOfOrLength(x: Char, start: Int): Int {
    val index = indexOf(x, start)
    return if (index != -1) index else length
}
