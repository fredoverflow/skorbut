package ui

import common.Diagnostic
import freditor.LineNumbers
import interpreter.Interpreter
import semantic.Linter
import syntax.ASTNode
import java.awt.*
import java.awt.event.*
import java.util.concurrent.ArrayBlockingQueue
import javax.swing.*
import javax.swing.event.*
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

object StopTheProgram : Exception()

class MainFrame : JFrame(Editor.filename) {
    private val queue = ArrayBlockingQueue<String>(1)
    private var interpreter = Interpreter("int main(){return 0;}")

    private val memoryUI = MemoryUI(interpreter.memory)
    private val syntaxTree = JTree()
    private val visualizer = JTabbedPane()

    private val editor = Editor()
    private val slider = JSlider(0, 11, 0)
    private val timer = Timer(1000, { queue.offer("step") })

    private val start = JButton("start")
    private val step = JButton("step")
    private val over = JButton("over")
    private val r3turn = JButton("return")
    private val stop = JButton("stop")
    private val buttons = JPanel()

    private val scrolledDiagnostics = JScrollPane()
    private val consoleUI = JTextArea()
    private val output = JTabbedPane()

    private val controls = JPanel()

    private var targetStackDepth = Int.MAX_VALUE

    init {
        val scrolledMemory = JScrollPane(memoryUI)
        scrolledMemory.preferredSize = Dimension(500, 500)

        syntaxTree.font = syntaxTree.font.deriveFont(globalFont.size.toFloat())
        val scrolledSyntaxTree = JScrollPane(syntaxTree)
        scrolledSyntaxTree.preferredSize = Dimension(500, 500)

        visualizer.addTab("memory", scrolledMemory)
        visualizer.addTab("syntax tree", scrolledSyntaxTree)
        visualizer.addChangeListener { _ ->
            if (visualizer.selectedComponent === scrolledSyntaxTree) {
                tryCompile(andRun = false)
            }
        }

        val editorWithLineNumbers = JPanel()
        editorWithLineNumbers.layout = BoxLayout(editorWithLineNumbers, BoxLayout.X_AXIS)
        editorWithLineNumbers.add(LineNumbers(editor))
        editorWithLineNumbers.add(editor)
        editor.setComponentToRepaint(editorWithLineNumbers)

        val horizontalSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, visualizer, editorWithLineNumbers)
        horizontalSplit.preferredSize = Dimension(1000, 500)

        slider.majorTickSpacing = 1
        slider.paintLabels = true

        buttons.add(start)
        step.isEnabled = false
        buttons.add(step)
        over.isEnabled = false
        buttons.add(over)
        r3turn.isEnabled = false
        buttons.add(r3turn)
        stop.isEnabled = false
        buttons.add(stop)
        buttons.maximumSize = buttons.preferredSize

        val diagnosticsPanel = JPanel()
        diagnosticsPanel.layout = BorderLayout()
        diagnosticsPanel.add(scrolledDiagnostics)

        consoleUI.font = globalFont
        consoleUI.isEditable = false

        output.addTab("diagnostics", scrolledDiagnostics)
        output.addTab("console", JScrollPane(consoleUI))
        output.preferredSize = Dimension(0, Toolkit.getDefaultToolkit().screenSize.height / 5)

        controls.layout = BoxLayout(controls, BoxLayout.Y_AXIS)
        controls.add(slider)
        controls.add(buttons)
        controls.add(output)

        val verticalSplit = JSplitPane(JSplitPane.VERTICAL_SPLIT, horizontalSplit, controls)
        verticalSplit.resizeWeight = 1.0
        add(verticalSplit)

        listenToSyntaxTree()
        listenToSlider()
        listenToButtons()
        listenToConsole()

        defaultCloseOperation = JFrame.EXIT_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(event: WindowEvent) {
                editor.tryToSaveCode()
            }
        })
        pack()
        isVisible = true
        // extendedState = Frame.MAXIMIZED_BOTH
        editor.requestFocusInWindow()
    }

    private fun listenToSyntaxTree() {
        syntaxTree.addTreeSelectionListener {
            val node = it.newLeadSelectionPath?.lastPathComponent
            if (node is ASTNode) {
                editor.setCursorTo(node.root().start)
            }
            editor.requestFocusInWindow()
        }
    }

    private fun listenToConsole() {
        consoleUI.addKeyListener(object : KeyAdapter() {
            override fun keyTyped(event: KeyEvent) {
                if (interpreter.console.isBlocked()) {
                    interpreter.console.keyTyped(event.keyChar)
                    updateConsole()
                } else {
                    JOptionPane.showMessageDialog(this@MainFrame, "You probably forgot one STEP", "Nobody is waiting for input yet", JOptionPane.ERROR_MESSAGE)
                    editor.requestFocusInWindow()
                }
            }
        })
    }

    private fun updateConsole() {
        consoleUI.text = interpreter.console.getText()
        output.selectedIndex = 1
        if (interpreter.console.isBlocked()) {
            consoleUI.requestFocusInWindow()
        } else {
            editor.requestFocusInWindow()
        }
    }

    private fun listenToSlider() {
        slider.addChangeListener {
            editor.requestFocusInWindow()
            configureTimer()
        }
    }

    private fun configureTimer() {
        val d = delay()
        if (d < 0) {
            timer.stop()
        } else {
            timer.initialDelay = d
            timer.delay = d
            if (isRunning()) {
                timer.restart()
            }
        }
    }

    private fun delay(): Int {
        val wait = slider.value
        return if (wait == 0) -1 else 1.shl(slider.maximum - wait)
    }

    private fun isRunning(): Boolean = !start.isEnabled

    private fun listenToButtons() {
        start.addActionListener {
            editor.tryToSaveCode()
            editor.requestFocusInWindow()
            queue.clear()
            tryCompile(andRun = true)
        }

        step.addActionListener {
            editor.requestFocusInWindow()
            queue.offer("step")
        }

        over.addActionListener {
            editor.requestFocusInWindow()
            queue.offer("over")
        }

        r3turn.addActionListener {
            editor.requestFocusInWindow()
            queue.offer("return")
        }

        stop.addActionListener {
            editor.requestFocusInWindow()
            timer.stop()
            queue.clear()
            queue.put("stop")
            interpreter.console.stop()
        }
    }

    private fun tryCompile(andRun: Boolean) {
        try {
            compile()
            if (andRun) run()
        } catch (diagnostic: Diagnostic) {
            editor.setCursorTo(diagnostic.position)
            updateDiagnostics(arrayListOf(diagnostic))
        } catch (other: Throwable) {
            other.printStackTrace()
            JOptionPane.showMessageDialog(this, other.message, "Throwable", JOptionPane.ERROR_MESSAGE)
        }
    }

    private fun compile() {
        interpreter = Interpreter(editor.text)
        updateSyntaxTreeModel()

        val linter = Linter(interpreter.translationUnit)
        updateDiagnostics(linter.getWarnings())
    }

    private fun run() {
        interpreter.before = { pauseAt(it) }
        interpreter.after = {
            SwingUtilities.invokeAndWait {
                memoryUI.update()
                if (interpreter.console.isDirty) {
                    updateConsole()
                }
            }
        }
        interpreter.console.update = { SwingUtilities.invokeAndWait { updateConsole() } }

        memoryUI.memory = interpreter.memory
        start.isEnabled = false
        step.isEnabled = true
        over.isEnabled = true
        r3turn.isEnabled = true
        stop.isEnabled = true
        configureTimer()
        tryExecute()
    }

    private fun updateSyntaxTreeModel() {
        syntaxTree.model = object : TreeModel {
            override fun getRoot(): Any? {
                return interpreter.translationUnit
            }

            override fun getChildCount(parent: Any): Int {
                var count = 0
                (parent as ASTNode).forEachChild { ++count }
                return count
            }

            override fun getChild(parent: Any, index: Int): Any? {
                var child: ASTNode? = null
                var i = 0
                (parent as ASTNode).forEachChild {
                    if (i == index) {
                        child = it
                    }
                    ++i
                }
                return child
            }

            override fun getIndexOfChild(parent: Any, child: Any): Int {
                var index = -1
                var i = 0
                (parent as ASTNode).forEachChild {
                    if (it === child) {
                        index = i
                    }
                    ++i
                }
                return index
            }

            override fun isLeaf(node: Any): Boolean {
                return getChildCount(node) == 0
            }

            override fun addTreeModelListener(l: TreeModelListener?) {
            }

            override fun removeTreeModelListener(l: TreeModelListener?) {
            }

            override fun valueForPathChanged(path: TreePath?, newValue: Any?) {
            }
        }
    }

    private fun updateDiagnostics(diagnostics: List<Diagnostic>) {
        val list = JList(diagnostics.toTypedArray())
        list.addListSelectionListener { event ->
            if (!event.valueIsAdjusting) {
                val index = list.selectedIndex
                if (index != -1) {
                    val diagnostic = diagnostics[index]
                    updateCaretPosition(diagnostic)
                    editor.requestFocusInWindow()
                    list.clearSelection()
                }
            }
        }
        scrolledDiagnostics.setViewportView(list)
        if (diagnostics.isEmpty()) {
            output.setTitleAt(0, "diagnostics")
        } else {
            output.setTitleAt(0, "diagnostics (${diagnostics.size})")
            output.selectedIndex = 0
        }
    }

    private fun updateCaretPosition(diagnostic: Diagnostic) {
        if (editor.cursor() != diagnostic.position) {
            editor.setCursorTo(diagnostic.position)
        } else if (diagnostic.secondPosition != -1) {
            editor.setCursorTo(diagnostic.secondPosition)
        }
    }

    private fun pauseAt(position: Int) {
        SwingUtilities.invokeLater {
            editor.setCursorTo(position)
        }
        when (fetchEntryFromQueue()) {
            "step" -> {
                targetStackDepth = Int.MAX_VALUE
            }
            "over" -> {
                targetStackDepth = interpreter.stackDepth
            }
            "return" -> {
                targetStackDepth = interpreter.stackDepth - 1
            }
            "stop" -> {
                timer.stop()
                throw StopTheProgram
            }
        }
    }

    private fun fetchEntryFromQueue(): String? {
        return if (interpreter.stackDepth <= targetStackDepth) {
            // Normal mode
            // Block until the next button press
            queue.take()
        } else {
            // Step over/step return mode
            // Don't block, but consume potential button presses, especially stop
            queue.poll()
        }
    }

    private fun tryExecute() {
        Thread {
            try {
                memoryUI.active = true
                targetStackDepth = Int.MAX_VALUE
                interpreter.run()
            } catch (stop: StopTheProgram) {
                memoryUI.active = false
            } catch (diagnostic: Diagnostic) {
                memoryUI.active = false
                SwingUtilities.invokeLater {
                    editor.setCursorTo(diagnostic.position)
                    JOptionPane.showMessageDialog(this, diagnostic.message, "Runtime error", JOptionPane.ERROR_MESSAGE)
                }
            } catch (other: Throwable) {
                memoryUI.active = false
                SwingUtilities.invokeLater {
                    other.printStackTrace()
                    JOptionPane.showMessageDialog(this, other.message, "Throwable", JOptionPane.ERROR_MESSAGE)
                }
            } finally {
                SwingUtilities.invokeLater {
                    start.isEnabled = true
                    step.isEnabled = false
                    over.isEnabled = false
                    r3turn.isEnabled = false
                    stop.isEnabled = false
                    timer.stop()
                }
            }
        }.start()
    }
}
