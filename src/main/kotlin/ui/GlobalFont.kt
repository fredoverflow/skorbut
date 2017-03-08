package ui

import java.awt.Font
import javax.swing.JOptionPane.*

val globalFont = Font(Font.MONOSPACED, Font.PLAIN, pickFontSize())

private fun pickFontSize(): Int {
    val title = "Welcome to skorbut"
    val prompt = "Please pick a font size:"
    val possibilities = arrayOf(10, 11, 12, 13, 14, 15, 16, 18, 20, 22, 24, 26, 28, 30, 32, 36, 40, 44, 48)
    val defaultChoice = 16
    val choice = showInputDialog(null, prompt, title, QUESTION_MESSAGE, null, possibilities, defaultChoice)
    val size = if (choice != null) choice as Int else defaultChoice
    return size
}
