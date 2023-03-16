package semantic

import interpreter.ArithmeticValue
import interpreter.BasicBlock
import interpreter.ImplicitContinue
import interpreter.returnType
import semantic.types.FunctionType
import semantic.types.VoidType
import syntax.lexer.Token
import syntax.lexer.TokenKind.*
import syntax.tree.*

class Linter(val translationUnit: TranslationUnit) : LinterBase() {
    init {
        detectLowHangingFruit()
        detectUnusedVariables()
        detectMissingReturns()
        detectUnreachableCode()
    }

    private fun detectLowHangingFruit() {
        translationUnit.walk({}) {
            when (it) {
                is Comma -> {
                    it.left.detectOperatorWithoutEffect()
                }

                is ExpressionStatement -> {
                    if (it.expression is FunctionCall) {
                        if (it.expression.type !== VoidType) {
                            it.expression.warn("ignored function result")
                        }
                    } else {
                        it.expression.detectOperatorWithoutEffect()
                    }
                }

                is IfThenElse -> {
                    it.condition.detectSuspiciousCondition()
                }

                is While -> {
                    it.condition.detectSuspiciousCondition()
                }

                is Do -> {
                    it.condition.detectSuspiciousCondition()
                }

                is For -> {
                    it.condition?.run { detectSuspiciousCondition() }
                    it.update?.run { detectOperatorWithoutEffect() }
                }

                is Assert -> {
                    it.condition.detectSuspiciousCondition()
                }
            }
        }
    }

    private fun Expression.detectOperatorWithoutEffect() {
        val root = root()
        when (root.kind) {
            EQUAL_EQUAL -> {
                root.warn("== is comparison, did you mean = instead?")
            }

            IDENTIFIER -> when (type) {
                is FunctionType -> root.warn("missing () for function call")

                else -> root.warn("$root has no effect")
            }

            PLUS, HYPHEN -> when (this) {
                is Binary -> root.warn("$root has no effect, did you mean $root= instead?")

                else -> root.warn("$root has no effect")
            }

            OPENING_PAREN, SIZEOF, OPENING_BRACKET, DOT, HYPHEN_MORE,
            AMPERSAND, ASTERISK, TILDE, BANG, SLASH, PERCENT,
            LESS_LESS, MORE_MORE, LESS, MORE, LESS_EQUAL, MORE_EQUAL, BANG_EQUAL,
            CARET, BAR, AMPERSAND_AMPERSAND, BAR_BAR,
            DOUBLE_CONSTANT, FLOAT_CONSTANT, INTEGER_CONSTANT,
            CHARACTER_CONSTANT, STRING_LITERAL -> {
                root.warn("$root has no effect")
            }

            else -> {
            }
        }
    }

    private fun Expression.detectSuspiciousCondition() {
        when (this) {
            is Assignment -> {
                warn("= is assignment, did you mean == instead?")
            }

            is RelationalEquality -> {
                if (left is RelationalEquality) {
                    val op1 = left.operator
                    val op2 = this.operator
                    warn("a${op1}b${op2}c does not do what you think it does. You probably want a${op1}b && b${op2}c instead.")
                } else if (operator.kind == LESS || operator.kind == BANG_EQUAL) {
                    if (right is FunctionCall && right.function is Identifier && right.function.name.text == "strlen") {
                        warn("consider replacing SLOW i${operator.kind}strlen(s) with FAST s[i]")
                    }
                }
            }

            is Logical -> {
                left.detectSuspiciousCondition()
                right.detectSuspiciousCondition()
            }
        }
        val x = value
        if (x is ArithmeticValue && this !is Constant) {
            warn("condition is always ${x.isTrue()}")
        }
    }

    private fun detectUnusedVariables() {
        val unusedVariables = HashSet<Token>()
        translationUnit.walk({ node ->
            when (node) {
                is FunctionDefinition -> {
                    node.parameters.forEach { unusedVariables.add(it.name) }
                }

                is Declaration -> {
                    if (node.specifiers.storageClass != TYPEDEF) {
                        for (namedDeclarator in node.namedDeclarators) {
                            unusedVariables.add(namedDeclarator.name)
                            if (namedDeclarator.declarator is Declarator.Initialized) {
                                namedDeclarator.declarator.init.walk({}) {
                                    when (it) {
                                        is Identifier -> {
                                            unusedVariables.remove(it.symbol.name)
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }) { node ->
            when (node) {
                is Identifier -> {
                    unusedVariables.remove(node.symbol.name)
                }
            }
        }
        unusedVariables.forEach {
            it.warn("unused variable $it")
        }
    }

    private fun detectMissingReturns() {
        translationUnit.functions.forEach { it.detectMissingReturn() }
    }

    private fun FunctionDefinition.detectMissingReturn() {
        if (returnType() !== VoidType) {
            val exit = controlFlowGraph.values.last()
            if (exit.isReachable && exit.isOpen()) {
                root().warn("function ${name()} does not return a result on all code paths")
            }
        }
    }

    private fun detectUnreachableCode() {
        translationUnit.functions.forEach { it.detectUnreachableCode() }
    }

    private fun FunctionDefinition.detectUnreachableCode() {
        controlFlowGraph.values.asSequence()
            .filterNot(BasicBlock::isReachable)
            .flatMap(BasicBlock::getStatements)
            .filterNot { it.root().start < 0 }
            .firstOrNull()
            ?.apply {
                if (this is ImplicitContinue) {
                    root().warn("loop never repeats")
                } else {
                    root().warn("unreachable code")
                }
            }
    }
}
