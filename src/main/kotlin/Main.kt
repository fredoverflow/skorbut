import freditor.SwingConfig
import ui.MainFrame
import java.awt.EventQueue

fun main() {
    SwingConfig.metalWithDefaultFont(SwingConfig.SANS_SERIF_PLAIN_16)
    EventQueue.invokeLater(::MainFrame)
}
