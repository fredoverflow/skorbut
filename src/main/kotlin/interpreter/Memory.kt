package interpreter

import semantic.Symbol
import semantic.types.ArrayType
import semantic.types.SignedCharType
import semantic.types.StructType
import semantic.types.Type
import syntax.lexer.fakeIdentifier
import syntax.lexer.missingIdentifier
import syntax.tree.NamedDeclarator

fun Iterable<String>.synthesizeStringConstantsType(): StructType {
    val symbols = ArrayList<Symbol>()
    val type = StructType(fakeIdentifier("strings"), symbols)
    var offset = 0
    for (str in this) {
        val len = str.length + 1
        symbols.add(Symbol(missingIdentifier, ArrayType(len, SignedCharType), offset))
        offset += len
    }
    return type
}

fun Iterable<NamedDeclarator>.synthesizeStaticVariablesType(): StructType {
    val symbols = ArrayList<Symbol>()
    val type = StructType(fakeIdentifier("statics"), symbols)
    for (namedDeclarator in this) {
        with(namedDeclarator) {
            symbols.add(Symbol(name, this.type, offset))
        }
    }
    return type
}

class Memory(stringLiterals: Iterable<String>, variables: Iterable<NamedDeclarator>) {
    val stringObjects = HashMap<String, Object>()

    val stringConstants = Segment(stringLiterals.synthesizeStringConstantsType())
    val staticVariables = Segment(variables.synthesizeStaticVariablesType())
    val stack = ArrayList<Segment>()
    val heap = ArrayList<Segment>()

    init {
        var stringOffset = 0
        for (stringLiteral in stringLiterals) {
            stringObjects.put(stringLiteral, Object(stringConstants, stringOffset, ArrayType(stringLiteral.length + 1, SignedCharType), 0, 1))
            for (x in stringLiteral) {
                stringConstants[stringOffset++] = Value.signedChar(x.toInt())
            }
            stringConstants[stringOffset++] = Value.NUL
        }
        stringConstants.readOnlyErrorMessage = "attempt to modify a string literal"
    }

    fun currentStackFrame() = stack.last()

    fun makeObject(symbol: Symbol): Object {
        return with(symbol) {
            if (offset < 0) {
                Object(staticVariables, offset + Int.MIN_VALUE, type, 0, 1)
            } else {
                Object(currentStackFrame(), offset, type, 0, 1)
            }
        }
    }

    inline fun functionScoped(stackFrameType: StructType, action: () -> Unit) {
        stack.add(Segment(stackFrameType))
        try {
            action()
        } finally {
            stack.last().kill()
            stack.removeAt(stack.lastIndex)
        }
    }

    fun malloc(type: Type): PointerValue {
        val segment = Segment(type)
        heap.add(segment)
        return PointerValue(Object(segment, 0, type, 0, 1))
    }

    fun malloc(arrayType: ArrayType): PointerValue {
        val segment = Segment(arrayType)
        heap.add(segment)
        return PointerValue(Object(segment, 0, arrayType.elementType, 0, arrayType.length))
    }

    fun free(pointer: PointerValue) {
        val obj = pointer.referenced
        val segment = obj.segment

        if (!segment.alive) throw AssertionError("dangling pointer")
        if (segment !in heap) throw AssertionError("free on non-heap segment")
        if (obj.offset != 0) throw AssertionError("free in the middle of segment")

        segment.kill()
        heap.remove(segment)
    }

    fun realloc(pointer: PointerValue, type: Type): PointerValue {
        val oldSegment = pointer.referenced.segment
        val newSegment = Segment(type)
        val smallerCount = Math.min(oldSegment.count(), newSegment.count())
        for (i in 0 until smallerCount) {
            newSegment[i] = oldSegment[i]
        }
        free(pointer)
        heap.add(newSegment)
        return PointerValue(Object(newSegment, 0, type, 0, 1))
    }

    fun realloc(pointer: PointerValue, arrayType: ArrayType): PointerValue {
        val oldSegment = pointer.referenced.segment
        val newSegment = Segment(arrayType)
        val smallerCount = Math.min(oldSegment.count(), newSegment.count())
        for (i in 0 until smallerCount) {
            newSegment[i] = oldSegment[i]
        }
        free(pointer)
        heap.add(newSegment)
        return PointerValue(Object(newSegment, 0, arrayType.elementType, 0, arrayType.length))
    }

    fun swap(a: Object, b: Object) {
        for (i in 0 until a.type.count()) {
            val x = a.segment[a.offset + i]
            val y = b.segment[b.offset + i]
            a.segment[a.offset + i] = y
            b.segment[b.offset + i] = x
        }
    }
}
