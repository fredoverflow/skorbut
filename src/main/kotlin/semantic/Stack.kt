package semantic

sealed class Stack<out T> : Iterable<T> {
    abstract fun isEmpty(): Boolean

    abstract fun top(): T

    abstract fun pop(): Stack<T>

    object Nil : Stack<Nothing>() {
        override fun isEmpty(): Boolean = true

        override fun top(): Nothing = throw AssertionError("top on empty stack")

        override fun pop(): Nothing = throw AssertionError("pop on empty stack")
    }

    class Cons<T>(val head: T, val tail: Stack<T>) : Stack<T>() {
        override fun isEmpty(): Boolean = false

        override fun top(): T = head

        override fun pop(): Stack<T> = tail
    }

    override fun iterator(): Iterator<T> = StackIterator(this)

    override fun toString(): String = joinToString(", ", "[", "]")
}

fun <T> emptyStack(): Stack<T> = Stack.Nil

class StackIterator<T>(start: Stack<T>) : Iterator<T> {
    var current = start

    override fun hasNext(): Boolean = !current.isEmpty()

    override fun next(): T {
        val result = current.top()
        current = current.pop()
        return result
    }
}

fun <U, T : U> Stack<T>.push(x: U): Stack<U> = Stack.Cons(x, this)
