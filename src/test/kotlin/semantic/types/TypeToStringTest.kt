package semantic.types

import org.junit.Assert.assertEquals
import org.junit.Test

class TypeToStringTest {
    @Test
    fun array() {
        assertEquals("int[10]", ArrayType(10, SignedIntType).toString())
    }

    @Test
    fun arrayOfArray() {
        assertEquals("int[1][2]", ArrayType(1, ArrayType(2, SignedIntType)).toString())
    }

    @Test
    fun pointer() {
        assertEquals("int*", PointerType(SignedIntType).toString())
    }

    @Test
    fun arrayOfPointers() {
        assertEquals("int*[10]", ArrayType(10, PointerType(SignedIntType)).toString())
    }

    @Test
    fun pointerToArray() {
        assertEquals("int(*)[10]", PointerType(ArrayType(10, SignedIntType)).toString())
    }

    @Test
    fun pointerToPointerToArray() {
        assertEquals("int(**)[10]", PointerType(PointerType(ArrayType(10, SignedIntType))).toString())
    }

    @Test
    fun qsort() {
        val predicate = FunctionType(SignedIntType, ConstVoidPointerType, ConstVoidPointerType).pointer()
        val qsort = FunctionType(VoidType, VoidPointerType, UnsignedIntType, UnsignedIntType, predicate)

        assertEquals("void(void*,unsigned int,unsigned int,int(*)(const void*,const void*))", qsort.toString())
    }

    @Test
    fun bsearch() {
        val predicate = FunctionType(SignedIntType, ConstVoidPointerType, ConstVoidPointerType).pointer()
        val bsearch = FunctionType(
            VoidPointerType, ConstVoidPointerType, ConstVoidPointerType, UnsignedIntType, UnsignedIntType, predicate
        )

        assertEquals(
            "void*(const void*,const void*,unsigned int,unsigned int,int(*)(const void*,const void*))",
            bsearch.toString()
        )
    }

    @Test
    fun function() {
        assertEquals("int()", FunctionType(SignedIntType).toString())
    }

    @Test
    fun functionReturningPointer() {
        assertEquals("int*()", FunctionType(PointerType(SignedIntType)).toString())
    }

    @Test
    fun functionReturningPointerToPointer() {
        assertEquals("int**()", FunctionType(PointerType(PointerType(SignedIntType))).toString())
    }

    @Test
    fun functionReturningPointerToArray() {
        assertEquals("int(*())[10]", FunctionType(PointerType(ArrayType(10, SignedIntType))).toString())
    }

    @Test
    fun functionReturningPointerToFunction() {
        val callback = PointerType(FunctionType(SignedIntType, SignedIntType, SignedIntType))
        assertEquals("int(*(int(*)(int,int)))(int,int)", FunctionType(callback, callback).toString())
    }

    @Test
    fun constant() {
        assertEquals("const int", Const(SignedIntType).toString())
    }

    @Test
    fun arrayOfConst() {
        assertEquals("const int[10]", ArrayType(10, Const(SignedIntType)).toString())
    }

    @Test
    fun pointerToConst() {
        assertEquals("const int*", PointerType(Const(SignedIntType)).toString())
    }

    @Test
    fun constPointer() {
        assertEquals("int*const", Const(PointerType(SignedIntType)).toString())
    }

    @Test
    fun constPointerToConst() {
        assertEquals("const int*const", Const(PointerType(Const(SignedIntType))).toString())
    }

    @Test
    fun constPointerToConstPointerToConst() {
        assertEquals("const int*const*const", Const(PointerType(Const(PointerType(Const(SignedIntType))))).toString())
    }

    @Test
    fun constPointerToArray() {
        assertEquals("int(*const)[10]", Const(PointerType(ArrayType(10, SignedIntType))).toString())
    }

    @Test
    fun constPointerToArrayOfConst() {
        assertEquals("const int(*const)[10]", Const(PointerType(ArrayType(10, Const(SignedIntType)))).toString())
    }

    @Test
    fun constPointerToFunction() {
        assertEquals(
            "int(*const)(int)",
            Const(PointerType(FunctionType(SignedIntType, SignedIntType))).toString()
        )
    }

    @Test
    fun arrayOfConstPointersToVoid() {
        assertEquals("void*const[10]", ArrayType(10, Const(VoidPointerType)).toString())
    }

    @Test
    fun arrayOfConstPointersToConstVoid() {
        assertEquals("const void*const[10]", ArrayType(10, Const(ConstVoidPointerType)).toString())
    }
}
