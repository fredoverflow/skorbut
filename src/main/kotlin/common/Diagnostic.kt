package common

class Diagnostic(val position: Int, val description: String, val secondPosition: Int = -1) : RuntimeException(description) {
    override fun toString(): String = description
}
