package syntax.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class AutocompletionTest {
    @Test
    fun fullSuffix() {
        val actual = autocompleteIdentifier("int foo, bar, baz; int main() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun partialSuffix() {
        val actual = autocompleteIdentifier("int foo, bar, baz; int main() { b")
        assertEquals(listOf("a"), actual)
    }

    @Test
    fun ambiguous() {
        val actual = autocompleteIdentifier("int foo, bar, baz; int main() { ba")
        assertEquals(listOf("r", "z"), actual)
    }

    @Test
    fun alreadyComplete() {
        val actual = autocompleteIdentifier("int foo, bar, baz; int main() { foo")
        assertEquals(emptyList<String>(), actual)
    }

    @Test
    fun parameter() {
        val actual = autocompleteIdentifier("double f(double number, double numbest) { num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun local() {
        val actual = autocompleteIdentifier("double f() { double number, numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun parameterAndLocal() {
        val actual = autocompleteIdentifier("double f(double number) { double numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun globalAndLocal() {
        val actual = autocompleteIdentifier("double number; double f() { double numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun nestedLocals() {
        val actual = autocompleteIdentifier("double f() { double number; { double numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun outOfScope() {
        val actual = autocompleteIdentifier("double f() { double number; { double numbest; } num")
        assertEquals(listOf("ber"), actual)
    }

    @Test
    fun recursion() {
        val actual = autocompleteIdentifier("void foo() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun backwardCall() {
        val actual = autocompleteIdentifier("void foo() {} void bar() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun forwardCall() {
        val actual = autocompleteIdentifier("void foo(); void bar() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun afterMutualRecursion() {
        val actual = autocompleteIdentifier("void baz(); void bar() { baz(); } void baz() { bar(); } void foo() { b")
        assertEquals(listOf("a"), actual)
    }

    @Test
    fun enumerationConstant() {
        val actual = autocompleteIdentifier("enum { WORD_SIZE = sizeof W")
        assertEquals(listOf("ORD_SIZE"), actual)
    }
}
