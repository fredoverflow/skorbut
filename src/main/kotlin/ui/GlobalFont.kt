package ui

import java.awt.Font
import java.awt.Toolkit

val globalFont: Font = {
    val screenHeight = Toolkit.getDefaultToolkit().screenSize.height
    val fontSize = when {
        screenHeight < 1000 -> 16
        screenHeight < 1500 -> 24
        else -> 36
    }
    Font(Font.MONOSPACED, Font.PLAIN, fontSize)
}()
