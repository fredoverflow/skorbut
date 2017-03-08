package common

import java.util.HashMap

class Counter {
    private val counter = HashMap<Any, Int>()

    fun count(x: Any): Int {
        val soFar = counter.getOrElse(x) { 0 }
        counter.put(x, soFar + 1)
        return soFar
    }
}
