package ui

import org.fife.ui.rsyntaxtextarea.FileLocation
import org.fife.ui.rsyntaxtextarea.SyntaxConstants
import org.fife.ui.rsyntaxtextarea.TextEditorPane
import java.awt.Desktop
import java.awt.event.ActionEvent
import java.io.File
import java.util.Calendar
import javax.swing.AbstractAction
import javax.swing.KeyStroke

class Editor : TextEditorPane() {
    companion object {
        val directory = "${System.getProperty("user.home")}/skorbut"
        val filenamePrefix = "$directory/skorbut"
        val filenameSuffix = ".txt"
        val filename = "$filenamePrefix$filenameSuffix"
    }

    private fun backupFilename(): String {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR) % 100
        val month = calendar.get(Calendar.MONTH) + 1
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)
        val second = calendar.get(Calendar.SECOND)

        val timestamp = "_%02d%02d%02d_%02d%02d%02d".format(year, month, day, hour, minute, second)
        return "$filenamePrefix$timestamp$filenameSuffix"
    }

    init {
        syntaxEditingStyle = SyntaxConstants.SYNTAX_STYLE_C
        antiAliasingEnabled = true
        tabSize = 4
        tabsEmulated = true
        isDirty = false
        animateBracketMatching = false
        font = globalFont
        tryLoadCode()
    }

    private fun tryLoadCode() {
        if (File(directory).mkdir()) {
            println("created directory $directory")
        } else {
            load(FileLocation.create(filename), null)
        }
        if (text.isEmpty()) {
            text = """int main()
{
    char a[] = "hello";
    char * p = a;
    printf("%s %u\n", a, sizeof a);
    printf("%s %u\n", p, sizeof p);
 // ++a; // arrays cannot be incremented
    ++p; // pointers CAN be incremented
    return 0;
}
"""
        }
        // see http://fifesoft.com/forum/viewtopic.php?f=10&t=695
        discardAllEdits()
    }

    fun trySaveCode() {
        if (isDirty) {
            trySaveCodeAs(filename)
            trySaveCodeAs(backupFilename())
        }
    }

    private fun trySaveCodeAs(name: String) {
        try {
            println("saving code as $name")
            saveAs(FileLocation.create(name))
        } catch (ex: Throwable) {
            println(ex)
        }
    }

    // A nice wrapper around Javas's absolutely awful Key Binding API
    fun configureHotkey(hotkey: String, callback: () -> Unit) {
        val action = object : AbstractAction() {
            override fun actionPerformed(ignored: ActionEvent) {
                callback()
            }
        }
        inputMap.put(KeyStroke.getKeyStroke(hotkey), action)
        // Yes, we abuse the (unique) action object as an intermediate key :)
        actionMap.put(action, action)
    }

    fun indent() {
        val line = caretLineNumber
        text = indent(text)
        val nextLine = lineOffset(line + 1)
        if (nextLine != null) {
            caretPosition = nextLine - 1
        }
    }

    private fun lineOffset(line: Int): Int? {
        // see http://www.coderanch.com/t/572006/GUI/java/JTextarea-line
        return document.defaultRootElement.getElement(line)?.startOffset
    }
}
