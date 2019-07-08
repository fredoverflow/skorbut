package syntax.parser

import org.junit.Assert.assertEquals
import org.junit.Test

class AutoCompleterTest {
    @Test
    fun fullSuffix() {
        val actual = completeIdentifier("int foo, bar, baz; int main() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun partialSuffix() {
        val actual = completeIdentifier("int foo, bar, baz; int main() { b")
        assertEquals(listOf("a"), actual)
    }

    @Test
    fun ambiguous() {
        val actual = completeIdentifier("int foo, bar, baz; int main() { ba")
        assertEquals(listOf("r", "z"), actual)
    }

    @Test
    fun alreadyComplete() {
        val actual = completeIdentifier("int foo, bar, baz; int main() { foo")
        assertEquals(emptyList<String>(), actual)
    }

    @Test
    fun parameter() {
        val actual = completeIdentifier("double f(double number, double numbest) { num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun local() {
        val actual = completeIdentifier("double f() { double number, numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun parameterAndLocal() {
        val actual = completeIdentifier("double f(double number) { double numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun globalAndLocal() {
        val actual = completeIdentifier("double number; double f() { double numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun nestedLocals() {
        val actual = completeIdentifier("double f() { double number; { double numbest; num")
        assertEquals(listOf("be"), actual)
    }

    @Test
    fun outOfScope() {
        val actual = completeIdentifier("double f() { double number; { double numbest; } num")
        assertEquals(listOf("ber"), actual)
    }

    @Test
    fun recursion() {
        val actual = completeIdentifier("void foo() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun backwardCall() {
        val actual = completeIdentifier("void foo() {} void bar() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun forwardCall() {
        val actual = completeIdentifier("void foo(); void bar() { f")
        assertEquals(listOf("oo"), actual)
    }

    @Test
    fun afterMutualRecursion() {
        val actual = completeIdentifier("void baz(); void bar() { baz(); } void baz() { bar(); } void foo() { b")
        assertEquals(listOf("a"), actual)
    }
}
