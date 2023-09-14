package ui

import common.Diagnostic
import freditor.*
import interpreter.Interpreter
import interpreter.Memory
import semantic.Linter
import semantic.TypeChecker
import syntax.lexer.Lexer
import syntax.lexer.keywords
import syntax.parser.Parser
import syntax.parser.autocompleteIdentifier
import syntax.parser.translationUnit
import syntax.tree.*
import java.awt.BorderLayout
import java.awt.Dimension
import java.awt.EventQueue
import java.awt.Toolkit
import java.awt.event.KeyAdapter
import java.awt.event.KeyEvent
import java.util.concurrent.ArrayBlockingQueue
import java.util.function.Consumer
import javax.swing.*
import javax.swing.event.TreeModelListener
import javax.swing.tree.TreeModel
import javax.swing.tree.TreePath

private val NAME = Regex("""[A-Z_a-z][0-9A-Z_a-z]*""")

object StopTheProgram : Exception()

fun <T : JComponent> T.sansSerif(): T {
    this.font = Fronts.sansSerif
    return this
}

class MainFrame : JFrame() {
    private val queue = ArrayBlockingQueue<String>(1)
    private var interpreter = Interpreter("int main(){return 0;}")

    private val memoryUI = MemoryUI(Memory(emptySet(), emptyList()))
    private val scrolledMemory = JScrollPane(memoryUI)
    private val syntaxTree = JTree().sansSerif()
    private val scrolledSyntaxTree = JScrollPane(syntaxTree)
    private val visualizer = JTabbedPane().sansSerif()

    private val tabbedEditors = TabbedEditors("skorbut", Flexer, JavaIndenter.instance) { freditor ->
        val editor = FreditorUI(freditor, 0, 25)

        editor.addKeyListener(object : KeyAdapter() {
            override fun keyPressed(event: KeyEvent) {
                when (event.keyCode) {
                    KeyEvent.VK_SPACE -> if (event.isControlDown) {
                        autocompleteIdentifier()
                    }

                    KeyEvent.VK_R -> if (FreditorUI.isControlRespectivelyCommandDown(event) && event.isAltDown) {
                        renameSymbol()
                    }

                    KeyEvent.VK_F1 -> showType()

                    KeyEvent.VK_F3 -> jumpToDeclarationAndFindUsages()

                    KeyEvent.VK_F5 -> into.doClick()
                    KeyEvent.VK_F6 -> over.doClick()
                    KeyEvent.VK_F7 -> r3turn.doClick()
                }
            }
        })

        editor.onRightClick = Consumer {
            editor.clearDiagnostics()
            showType()
        }

        editor
    }

    private val editor
        get() = tabbedEditors.selectedEditor

    private val slider = JSlider(0, 11, 0).sansSerif()
    private val timer = Timer(1000) { queue.offer("into") }

    private val start = JButton("start").sansSerif()
    private val into = JButton("step into (F5)").sansSerif()
    private val over = JButton("step over (F6)").sansSerif()
    private val r3turn = JButton("step return (F7)").sansSerif()
    private val stop = JButton("stop").sansSerif()
    private val buttons = JPanel()

    private val scrolledDiagnostics = JScrollPane()
    private val consoleUI = JTextArea()
    private val output = JTabbedPane().sansSerif()

    private val controls = JPanel()

    private var targetStackDepth = Int.MAX_VALUE
    private var lastReceivedPosition = 0

    init {
        title = "skorbut version ${Release.compilationDate(MainFrame::class.java)} @ ${editor.file.parent}"

        scrolledMemory.preferredSize = Dimension(500, 500)
        scrolledSyntaxTree.preferredSize = Dimension(500, 500)

        visualizer.addTab("memory", scrolledMemory)
        visualizer.addTab("syntax tree", scrolledSyntaxTree)
        visualizer.addChangeListener {
            if (visualizer.selectedComponent === scrolledMemory) {
                memoryUI.update()
            } else {
                hideDirtySyntaxTree()
            }
        }

        if (editor.length() == 0) {
            editor.load(helloWorld)
        }
        tabbedEditors.tabs.addChangeListener {
            updateDiagnostics(emptyList())
            hideDirtySyntaxTree()
        }

        val horizontalSplit = JSplitPane(JSplitPane.HORIZONTAL_SPLIT, visualizer, tabbedEditors.tabs)
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
        listenToConsole()

        defaultCloseOperation = EXIT_ON_CLOSE
        tabbedEditors.saveOnExit(this)
        pack()
        isVisible = true
        editor.requestFocusInWindow()
    }

    private fun hideDirtySyntaxTree() {
        if (visualizer.selectedComponent === scrolledSyntaxTree) {
            if (!isRunning() && !tryCompile()) {
                visualizer.selectedComponent = scrolledMemory
            }
        }
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
                    JOptionPane.showMessageDialog(
                        this@MainFrame,
                        "You probably forgot one STEP",
                        "Nobody is waiting for input yet",
                        JOptionPane.ERROR_MESSAGE
                    )
                }
            }
        })
    }

    private fun updateConsole() {
        consoleUI.text = interpreter.console.getText()
        output.selectedIndex = 1
        requestFocusInBlockedConsoleOrEditor()
    }

    private fun requestFocusInBlockedConsoleOrEditor() {
        if (interpreter.console.isBlocked()) {
            consoleUI.requestFocusInWindow()
        } else {
            editor.requestFocusInWindow()
        }
    }

    private fun listenToSlider() {
        slider.addChangeListener {
            requestFocusInBlockedConsoleOrEditor()
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
            editor.indent()
            editor.saveWithBackup()
            editor.clearDiagnostics()
            editor.requestFocusInWindow()
            queue.clear()
            if (tryCompile()) {
                run()
            }
        }

        into.addActionListener {
            requestFocusInBlockedConsoleOrEditor()
            queue.offer("into")
        }

        over.addActionListener {
            requestFocusInBlockedConsoleOrEditor()
            queue.offer("over")
        }

        r3turn.addActionListener {
            requestFocusInBlockedConsoleOrEditor()
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

    private fun renameSymbol() {
        if (isRunning() || !tryCompile()) return

        val symbol = interpreter.typeChecker.symbolAt(editor.cursor()) ?: return

        val oldName = symbol.name.text
        val input = JOptionPane.showInputDialog(
            this,
            oldName,
            "rename symbol",
            JOptionPane.QUESTION_MESSAGE,
            null,
            null,
            oldName
        ) ?: return

        val newName = input.toString().trim()
        if (!NAME.matches(newName) || newName in keywords) return

        val positions = IntArray(1 + symbol.usages.size)
        positions[0] = symbol.name.start
        symbol.usages.forEachIndexed { index, usage ->
            positions[1 + index] = usage.name.start
        }
        editor.rename(oldName, newName, positions)
        tryCompile()
    }

    private fun detectRoot(node: Node) {
        val root = node.root()
        if (editor.cursor() in root.start until root.end) {
            root.error(" $node ", -1)
        }
    }

    private fun showType() {
        try {
            val translationUnit = Parser(Lexer(editor.text)).translationUnit()
            TypeChecker(translationUnit)

            translationUnit.walk({}) { node ->
                if (node is Expression) {
                    detectRoot(node)
                } else if (node is FunctionDefinition) {
                    detectRoot(node)
                    node.parameters.forEach(::detectRoot)
                } else if (node is NamedDeclarator) {
                    detectRoot(node)
                    if (node.declarator is Declarator.Initialized) {
                        node.declarator.init.walk({}, ::detectRoot)
                    }
                }
            }
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
        }
    }

    private fun jumpToDeclarationAndFindUsages() {
        if (isRunning() || !tryCompile()) return

        val symbol = interpreter.typeChecker.symbolAt(editor.cursor()) ?: return

        val symbolStart = symbol.name.start
        editor.setCursorTo(symbolStart)

        val name = symbol.name.text
        val diagnostics = symbol.usages.map { usage ->
            val usageStart = usage.name.start
            val line = 1 + editor.lineOfPosition(usageStart)
            Diagnostic(usageStart, "usage of $name on line $line", symbolStart)
        }
        updateDiagnostics(diagnostics)
    }

    private fun tryCompile(): Boolean {
        try {
            compile()
            return true
        } catch (diagnostic: Diagnostic) {
            showDiagnostic(diagnostic)
            updateDiagnostics(arrayListOf(diagnostic))
        } catch (other: Throwable) {
            showDiagnostic(other.message ?: "null")
            other.printStackTrace()
        }
        return false
    }

    private fun compile() {
        interpreter = Interpreter(editor.text)
        updateSyntaxTreeModel()

        val linter = Linter(interpreter.translationUnit)
        updateDiagnostics(linter.getWarnings())
    }

    private fun run() {
        interpreter.onMemorySet = { memory ->
            EventQueue.invokeLater {
                memoryUI.memory = memory
            }
        }
        interpreter.before = ::pauseAt
        interpreter.after = {
            EventQueue.invokeAndWait {
                if (visualizer.selectedComponent === scrolledMemory) {
                    memoryUI.update()
                }
                if (interpreter.console.isDirty) {
                    updateConsole()
                }
            }
        }
        consoleUI.text = ""
        interpreter.console.update = { EventQueue.invokeAndWait(::updateConsole) }

        tabbedEditors.tabs.isEnabled = false
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
            override fun getRoot(): Any {
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
        editor.setCursorTo(diagnostic.position)
        editor.requestFocusInWindow()
        editor.showDiagnostic(diagnostic.message, diagnostic.position, diagnostic.columnDelta)
    }

    private fun showDiagnostic(position: Int, message: String) {
        editor.setCursorTo(position)
        editor.requestFocusInWindow()
        editor.showDiagnostic(message)
    }

    private fun showDiagnostic(message: String) {
        editor.requestFocusInWindow()
        editor.showDiagnostic(message)
    }

    private fun updateDiagnostics(diagnostics: List<Diagnostic>) {
        val list = JList(diagnostics.toTypedArray()).sansSerif()
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
            EventQueue.invokeLater {
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
                targetStackDepth = Int.MAX_VALUE
                lastReceivedPosition = 0
                interpreter.run(editor.cursor(), editor.length())
            } catch (_: StopTheProgram) {
            } catch (diagnostic: Diagnostic) {
                EventQueue.invokeLater {
                    showDiagnostic(diagnostic)
                }
            } catch (other: Throwable) {
                EventQueue.invokeLater {
                    showDiagnostic(lastReceivedPosition, other.message ?: "null")
                    other.printStackTrace()
                }
            } finally {
                EventQueue.invokeLater {
                    tabbedEditors.tabs.isEnabled = true
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

const val helloWorld = """void demo()
{
    char a[] = "hi";
    char * p = a;
    ++p;
    ++p;
    ++p;
}
"""
