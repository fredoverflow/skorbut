package ui

import java.awt.Font

val globalFont = Font(Font.MONOSPACED, Font.PLAIN, if (freditor.Front.size == "large") 24 else 16)
