package ui

import common.Diagnostic
import freditor.Fronts
import freditor.LineNumbers
import interpreter.Interpreter
import semantic.Linter
import syntax.parser.autocompleteIdentifier
import syntax.tree.Node

import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.awt.event.WindowAdapter
import java.awt.event.WindowEvent
import java.util.concurrent.ArrayBlockingQueue

import javax.swing.*
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

object StopTheProgram : Exception()

class MainFrame : JFrame() {
    private val queue = ArrayBlockingQueue<String>(1)
    private var interpreter = Interpreter("int main(){return 0;}")

    private val memoryUI = MemoryUI(interpreter.memory)
    private val syntaxTree = JTree()
    private val visualizer = JTabbedPane()

    private val editor = Editor()
    private val slider = JSlider(0, 11, 0)
    private val timer = Timer(1000) { queue.offer("into") }

    private val start = JButton("start")
    private val into = JButton("step into (F5)")
    private val over = JButton("step over (F6)")
    private val r3turn = JButton("step return (F7)")
    private val stop = JButton("stop")
    private val buttons = JPanel()

    private val scrolledDiagnostics = JScrollPane()
    private val consoleUI = JTextArea()
    private val output = JTabbedPane()

    private val controls = JPanel()

    private var targetStackDepth = Int.MAX_VALUE
    private var lastReceivedPosition = 0

    init {
        title = editor.autosaver.pathname

        val scrolledMemory = JScrollPane(memoryUI)
        scrolledMemory.preferredSize = Dimension(500, 500)

        syntaxTree.font = Fronts.sansSerif
        val scrolledSyntaxTree = JScrollPane(syntaxTree)
        scrolledSyntaxTree.preferredSize = Dimension(500, 500)

        visualizer.addTab("memory", scrolledMemory)
        visualizer.addTab("syntax tree", scrolledSyntaxTree)
        visualizer.addChangeListener {
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
        into.isEnabled = false
        buttons.add(into)
        over.isEnabled = false
        buttons.add(over)
        r3turn.isEnabled = false
        buttons.add(r3turn)
        stop.isEnabled = false
        buttons.add(stop)

        val diagnosticsPanel = JPanel()
        diagnosticsPanel.layout = BorderLayout()
        diagnosticsPanel.add(scrolledDiagnostics)

        consoleUI.font = Fronts.monospaced
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
        listenToKeyboard()
        listenToConsole()

        defaultCloseOperation = EXIT_ON_CLOSE
        addWindowListener(object : WindowAdapter() {
            override fun windowClosing(event: WindowEvent) {
                editor.autosaver.save()
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
            if (node is Node) {
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
            editor.autosaver.save()
            editor.clearDiagnostics()
            editor.requestFocusInWindow()
            queue.clear()
            tryCompile(andRun = true)
        }

        into.addActionListener {
            editor.requestFocusInWindow()
            queue.offer("into")
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

    private fun listenToKeyboard() {
        editor.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                when (event.keyCode) {
                    KeyEvent.VK_SPACE -> if (event.isControlDown) {
                        autocompleteIdentifier()
                    }
                    KeyEvent.VK_F5 -> into.doClick()
                    KeyEvent.VK_F6 -> over.doClick()
                    KeyEvent.VK_F7 -> r3turn.doClick()
                }
            }
        })
    }

    private fun autocompleteIdentifier() {
        try {
            val suffixes = autocompleteIdentifier(editor.textBeforeSelection)
            if (suffixes.size == 1) {
                editor.insert(suffixes[0])
            } else {
                println(suffixes.sorted().joinToString(", "))
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
            updateDiagnostics(arrayListOf(diagnostic))
        }
    }

    private fun tryCompile(andRun: Boolean) {
        try {
            compile()
            if (andRun) run()
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
            updateDiagnostics(arrayListOf(diagnostic))
        } catch (other: Throwable) {
            showDiagnostic(other.message ?: "null")
            other.printStackTrace()
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
        into.isEnabled = true
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
                (parent as Node).forEachChild { ++count }
                return count
            }

            override fun getChild(parent: Any, index: Int): Any? {
                var child: Node? = null
                var i = 0
                (parent as Node).forEachChild {
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
                (parent as Node).forEachChild {
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

    private fun showDiagnostic(diagnostic: Diagnostic) {
        showDiagnostic(diagnostic.position, diagnostic.message)
    }

    private fun showDiagnostic(position: Int, message: String) {
        editor.setCursorTo(position)
        showDiagnostic(message)
    }

    private fun showDiagnostic(message: String) {
        editor.requestFocusInWindow()
        editor.showDiagnostic(message)
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
        lastReceivedPosition = position
        val entry = if (interpreter.stackDepth <= targetStackDepth) {
            // Step into mode
            SwingUtilities.invokeLater {
                editor.setCursorTo(position)
            }
            // Block until the next button press
            queue.take()
        } else {
            // Step over/return mode
            // Don't block, but consume potential button presses, especially stop
            queue.poll()
        }
        when (entry) {
            "into" -> {
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

    private fun tryExecute() {
        Thread {
            try {
                memoryUI.active = true
                targetStackDepth = Int.MAX_VALUE
                lastReceivedPosition = 0
                interpreter.run()
            } catch (stop: StopTheProgram) {
                memoryUI.active = false
            } catch (diagnostic: Diagnostic) {
                memoryUI.active = false
                SwingUtilities.invokeLater {
                    showDiagnostic(diagnostic)
                }
            } catch (other: Throwable) {
                memoryUI.active = false
                SwingUtilities.invokeLater {
                    showDiagnostic(lastReceivedPosition, other.message ?: "null")
                    other.printStackTrace()
                }
            } finally {
                SwingUtilities.invokeLater {
                    start.isEnabled = true
                    into.isEnabled = false
                    over.isEnabled = false
                    r3turn.isEnabled = false
                    stop.isEnabled = false
                    timer.stop()
                }
            }
        }.start()
    }
}
