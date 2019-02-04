package interpreter

import semantic.types.ArithmeticType
import syntax.tree.*

val unusedEmptyHashMap = HashMap<ArithmeticValue, String>()

class State(val continueTarget: String, val breakTarget: String,
            val switchControlType: ArithmeticType?,
            val cases: HashMap<ArithmeticValue, String>, var default: String?) {
    constructor() : this("", "", null, unusedEmptyHashMap, null) {
    }

    fun openLoop(continueTarget: String, breakTarget: String): State {
        return State(continueTarget, breakTarget, switchControlType, cases, default)
    }

    fun openSwitch(controlType: ArithmeticType, breakTarget: String): State {
        return State(continueTarget, breakTarget, controlType, HashMap(), null)
    }
}

class BuildControlFlowGraph(function: FunctionDefinition) {
    val controlFlowGraph = LinkedHashMap<String, BasicBlock>()

    var lastGeneratedLabel = -1
    var currentLabelStr = ""
    var currentBasicBlock = BasicBlock()

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
            // definitely unreachable code, maybe complain later?
            currentLabelStr = generateLabel()
            controlFlowGraph[currentLabelStr] = currentBasicBlock
        }
        currentBasicBlock.add(statement)
    }

    private fun jumpIfOpen(target: String) {
        if (currentBasicBlock.isOpen()) {
            add(Jump(target))
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
                add(Jump(label.text))
            }
            is IfThen -> {
                val execute = generateLabel()
                val done = generateLabel()

                add(JumpIf(condition, execute, done))

                insertLabel(execute)
                th3n.flatten(state)

                insertLabel(done)
            }
            is IfThenElse -> {
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
            is Switch -> {
                val done = generateLabel()

                val state = state.openSwitch(control.type as ArithmeticType, breakTarget = done)

                add(SwitchPlaceholder)
                val basicBlock = currentBasicBlock
                body.flatten(state)
                basicBlock.replaceSwitchPlaceholderWithRealSwitch(HashSwitch(control, state.cases, state.default ?: done))

                insertLabel(done)
            }
            is Case -> {
                if (state.switchControlType == null) {
                    case.error("case label must be nested inside a switch")
                }
                val caseLabel = generateLabel()
                insertLabel(caseLabel)
                val previous = state.cases.put(state.switchControlType.cast(choice.value as ArithmeticValue), caseLabel)
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

                val state = state.openLoop(continueTarget = checkCondition, breakTarget = done)

                insertLabel(checkCondition)
                add(JumpIf(condition, bodyStart, done))

                insertLabel(bodyStart)
                body.flatten(state)
                jumpIfOpen(checkCondition)

                insertLabel(done)
            }
            is For -> {
                if (init != null) {
                    add(FlatExpressionStatement(init))
                }

                val checkCondition = generateLabel()
                val loopStart = generateLabel()
                val updateCounter = generateLabel()
                val done = generateLabel()

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
                    add(Jump(checkCondition))

                    insertLabel(done)
                } else {
                    insertLabel(loopStart)
                    body.flatten(state)

                    insertLabel(updateCounter)
                    if (update != null) {
                        add(FlatExpressionStatement(update))
                    }
                    add(Jump(loopStart))

                    insertLabel(done)
                }
            }
            is Continue -> {
                if (state.continueTarget.isEmpty()) {
                    continu3.error("continue must be nested inside a loop")
                }
                add(Jump(state.continueTarget))
            }
            is Break -> {
                if (state.breakTarget.isEmpty()) {
                    br3ak.error("break must be nested inside a loop or switch")
                }
                add(Jump(state.breakTarget))
            }
            is Return -> {
                add(FlatReturn(result))
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
