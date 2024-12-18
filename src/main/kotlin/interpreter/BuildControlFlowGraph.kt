package interpreter

import semantic.types.ArithmeticType
import syntax.lexer.missingIdentifier
import syntax.tree.*

val unusedEmptyHashMap = HashMap<ArithmeticValue, String>()

class State(
    val continueTarget: String, val breakTarget: String,
    val switchControlType: ArithmeticType?,
    val cases: HashMap<ArithmeticValue, String>, var default: String?
) {
    constructor() : this("", "", null, unusedEmptyHashMap, null)

    fun openLoop(continueTarget: String, breakTarget: String): State {
        return State(continueTarget, breakTarget, switchControlType, cases, default)
    }

    fun openSwitch(controlType: ArithmeticType, breakTarget: String): State {
        return State(continueTarget, breakTarget, controlType, HashMap(), null)
    }
}

class BuildControlFlowGraph(function: FunctionDefinition) {
    private val controlFlowGraph = LinkedHashMap<String, BasicBlock>()

    private var lastGeneratedLabel = -1
    private var currentLabelStr = ""
    private var currentBasicBlock = BasicBlock()

    private fun generateLabel(): String {
        return "${++lastGeneratedLabel}"
    }

    private fun insertLabel(label: String) {
        if (!currentBasicBlock.isEmpty()) {
            jumpIfOpen(label)
            currentBasicBlock = BasicBlock()
        }
        currentLabelStr = label
        controlFlowGraph[currentLabelStr] = currentBasicBlock
    }

    private fun add(statement: FlatStatement) {
        if (currentBasicBlock.isClosed()) {
            currentBasicBlock = BasicBlock()
            currentLabelStr = generateLabel()
            controlFlowGraph[currentLabelStr] = currentBasicBlock
        }
        currentBasicBlock.add(statement)
    }

    private fun jumpIfOpen(target: String) {
        if (currentBasicBlock.isOpen()) {
            add(Jump(missingIdentifier, target))
        }
    }

    init {
        currentLabelStr = generateLabel()
        insertLabel(currentLabelStr)

        function.body.flatten(State())

        val entry = controlFlowGraph.values.first()
        entry.exploreReachability { controlFlowGraph[it]!! }
        function.controlFlowGraph = controlFlowGraph
    }

    private fun List<Statement>.flatten(state: State) {
        for (statement in this) {
            statement.flatten(state)
        }
    }

    private fun Statement.flatten(state: State) {
        when (this) {
            is Block -> {
                statements.flatten(state)
            }

            is Declaration -> {
                add(FlatDeclaration(specifiers, namedDeclarators))
            }

            is ExpressionStatement -> {
                add(FlatExpressionStatement(expression))
            }

            is LabeledStatement -> {
                insertLabel(label.text)
                statement.flatten(state)
            }

            is Goto -> {
                add(Jump(goto, label.text))
            }

            is IfThenElse -> {
                if (e1se == null) {
                    val execute = generateLabel()
                    val done = generateLabel()

                    add(JumpIf(condition, execute, done))

                    insertLabel(execute)
                    th3n.flatten(state)

                    insertLabel(done)
                } else {
                    val executeThen = generateLabel()
                    val executeElse = generateLabel()
                    val done = generateLabel()

                    add(JumpIf(condition, executeThen, executeElse))

                    insertLabel(executeThen)
                    th3n.flatten(state)
                    jumpIfOpen(done)

                    insertLabel(executeElse)
                    e1se.flatten(state)

                    insertLabel(done)
                }
            }

            is Switch -> {
                val done = generateLabel()

                @Suppress("NAME_SHADOWING")
                val state = state.openSwitch(control.type.unqualified() as ArithmeticType, breakTarget = done)

                add(SwitchPlaceholder)
                val basicBlock = currentBasicBlock
                body.flatten(state)
                basicBlock.replaceSwitchPlaceholderWithRealSwitch(
                    HashSwitch(control, state.cases, state.default ?: done)
                )

                insertLabel(done)
            }

            is Case -> {
                if (state.switchControlType == null) {
                    case.error("case label must be nested inside a switch")
                }
                val caseLabel = generateLabel()
                insertLabel(caseLabel)
                val previous = state.cases.put(
                    state.switchControlType.integralPromotions().cast(choice.value as ArithmeticValue),
                    caseLabel
                )
                if (previous != null) {
                    case.error("duplicate case label")
                }
                body.flatten(state)
            }

            is Default -> {
                if (state.switchControlType == null) {
                    default.error("default label must be nested inside a switch")
                }
                if (state.default != null) {
                    default.error("duplicate default label")
                }
                val defaultLabel = generateLabel()
                insertLabel(defaultLabel)
                state.default = defaultLabel
                body.flatten(state)
            }

            is Do -> {
                val bodyStart = generateLabel()
                val checkCondition = generateLabel()
                val done = generateLabel()

                @Suppress("NAME_SHADOWING")
                val state = state.openLoop(continueTarget = checkCondition, breakTarget = done)

                insertLabel(bodyStart)
                body.flatten(state)

                insertLabel(checkCondition)
                add(JumpIf(condition, bodyStart, done))

                insertLabel(done)
            }

            is While -> {
                val checkCondition = generateLabel()
                val bodyStart = generateLabel()
                val done = generateLabel()

                @Suppress("NAME_SHADOWING")
                val state = state.openLoop(continueTarget = checkCondition, breakTarget = done)

                insertLabel(checkCondition)
                add(JumpIf(condition, bodyStart, done))

                insertLabel(bodyStart)
                body.flatten(state)
                add(ImplicitContinue(whi1e, checkCondition))

                insertLabel(done)
            }

            is For -> {
                when (init) {
                    is ExpressionStatement -> {
                        add(FlatExpressionStatement(init.expression))
                    }

                    is Declaration -> {
                        add(FlatDeclaration(init.specifiers, init.namedDeclarators))
                    }
                }

                val checkCondition = generateLabel()
                val loopStart = generateLabel()
                val updateCounter = generateLabel()
                val done = generateLabel()

                @Suppress("NAME_SHADOWING")
                val state = state.openLoop(continueTarget = updateCounter, breakTarget = done)

                if (condition != null) {
                    insertLabel(checkCondition)
                    add(JumpIf(condition, loopStart, done))

                    insertLabel(loopStart)
                    body.flatten(state)

                    insertLabel(updateCounter)
                    if (update != null) {
                        add(FlatExpressionStatement(update))
                    }
                    add(ImplicitContinue(f0r, checkCondition))

                    insertLabel(done)
                } else {
                    insertLabel(loopStart)
                    body.flatten(state)

                    insertLabel(updateCounter)
                    if (update != null) {
                        add(FlatExpressionStatement(update))
                    }
                    add(ImplicitContinue(f0r, loopStart))

                    insertLabel(done)
                }
            }

            is Continue -> {
                if (state.continueTarget.isEmpty()) {
                    continu3.error("continue must be nested inside a loop")
                }
                add(Jump(continu3, state.continueTarget))
            }

            is Break -> {
                if (state.breakTarget.isEmpty()) {
                    br3ak.error("break must be nested inside a loop or switch")
                }
                add(Jump(br3ak, state.breakTarget))
            }

            is Return -> {
                add(FlatReturn(r3turn, result))
            }

            is Assert -> {
                add(FlatAssert(condition))
            }

            else -> {
                error("no flatten for $this")
            }
        }
    }
}
