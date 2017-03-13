package syntax

import interpreter.Value
import semantic.Symbol
import semantic.types.Later
import semantic.types.Type

abstract class Expression : ASTNode() {
    var type: Type = Later
    var value: Value? = null

    var isLocator: Boolean = false

    override fun toString(): String = "${super.toString()} : $type ${value?.show() ?: ""}"
}

abstract class Unary(val operator: Token, val operand: Expression) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        action(operand)
    }

    override fun root(): Token = operator
}

abstract class Binary(val left: Expression, val operator: Token, val right: Expression) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        action(left)
        action(right)
    }

    override fun root(): Token = operator
}

class Constant(val constant: Token) : Expression() {
    override fun root(): Token = constant
}

class StringLiteral(val literal: Token) : Expression() {
    override fun root(): Token = literal
}

class Identifier(val name: Token) : Expression() {
    lateinit var symbol: Symbol

    override fun root(): Token = name
}

class PrintfCall(val printf: Token, val format: Token, val arguments: List<Expression>) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        arguments.forEach(action)
    }

    override fun root(): Token = printf

    override fun toString(): String = "printf ${format.source} : $type"
}

class ScanfCall(val scanf: Token, val format: Token, val arguments: List<Expression>) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        arguments.forEach(action)
    }

    override fun root(): Token = scanf

    override fun toString(): String = "scanf ${format.source} : $type"
}

class Postfix(x: Expression, f: Token) : Unary(f, x)

class Subscript(x: Expression, f: Token, y: Expression) : Binary(x, f, y) {
    override fun toString(): String = "[] : $type"
}

class FunctionCall(val function: Expression, val arguments: List<Expression>) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        action(function)
        arguments.forEach(action)
    }

    override fun root(): Token = function.root()

    override fun toString(): String = "() : $type"
}

class DirectMemberAccess(val left: Expression, val dot: Token, val right: Token) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        action(left)
    }

    override fun root(): Token = dot
}

class IndirectMemberAccess(val left: Expression, val arrow: Token, val right: Token) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        action(left)
    }

    override fun root(): Token = arrow
}

class Prefix(f: Token, x: Expression) : Unary(f, x)

class Reference(f: Token, x: Expression) : Unary(f, x)

class Dereference(f: Token, x: Expression) : Unary(f, x)

class UnaryPlus(f: Token, x: Expression) : Unary(f, x)

class UnaryMinus(f: Token, x: Expression) : Unary(f, x)

class BitwiseNot(f: Token, x: Expression) : Unary(f, x)

class LogicalNot(f: Token, x: Expression) : Unary(f, x)

class SizeofType(val operator: Token, val specifiers: DeclarationSpecifiers, val declarator: Declarator) : Expression() {
    var operandType: Type = Later

    override fun root(): Token = operator
}

class SizeofExpression(f: Token, x: Expression) : Unary(f, x)

class Multiplicative(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class Plus(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class Minus(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class Shift(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class RelationalEquality(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class Bitwise(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class Logical(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class Conditional(val condition: Expression, val question: Token, val th3n: Expression, val colon: Token, val e1se: Expression) : Expression() {
    override fun forEachChild(action: (ASTNode) -> Unit) {
        action(condition)
        action(th3n)
        action(e1se)
    }

    override fun root(): Token = question
}

class Assignment(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class PlusAssignment(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class MinusAssignment(x: Expression, f: Token, y: Expression) : Binary(x, f, y)

class Comma(x: Expression, f: Token, y: Expression) : Binary(x, f, y)
