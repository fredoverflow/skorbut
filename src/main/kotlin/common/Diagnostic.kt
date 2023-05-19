package common

data class Diagnostic(
    val position: Int, override val message: String,
    val secondPosition: Int = -1,
    val columnDelta: Int = 0
) : Exception(message) {

    override fun toString(): String {
        return if (secondPosition < 0) {
            message
        } else {
            "$message \uD83D\uDDB0 toggle position"
        }
    }
}
