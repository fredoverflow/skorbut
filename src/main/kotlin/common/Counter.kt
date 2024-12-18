package common

class Counter {
    private val counter = HashMap<Any, Int>()

    fun count(x: Any): Int {
        val soFar = counter.getOrElse(x) { 0 }
        counter[x] = soFar + 1
        return soFar
    }
}
