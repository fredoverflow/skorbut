import freditor.SwingConfig
import ui.MainFrame
import javax.swing.SwingUtilities

fun main(args: Array<String>) {
    SwingConfig.metalWithDefaultFont(SwingConfig.SANS_SERIF_PLAIN_16)
    SwingUtilities.invokeLater { MainFrame() }
}
