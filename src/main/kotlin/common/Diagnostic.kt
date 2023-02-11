package common

data class Diagnostic(
    val position: Int, override val message: String,
    val secondPosition: Int = -1,
    val columnDelta: Int = 0
) : Exception(message) {

    override fun toString(): String {
        return message
    }
}
