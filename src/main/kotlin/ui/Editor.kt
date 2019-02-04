package ui

import freditor.FreditorUI
import freditor.JavaIndenter

import java.io.File
import java.io.IOException
import java.security.MessageDigest

class Editor : FreditorUI(Flexer.instance, JavaIndenter.instance, 0, 25) {
    companion object {
        val directory = "${System.getProperty("user.home")}/skorbut"
        val filenamePrefix = "$directory/skorbut"
        val filenameSuffix = ".txt"
        val filename = "$filenamePrefix$filenameSuffix"
    }

    init {
        try {
            loadFromFile(filename)
        } catch (ex: IOException) {
            loadFromString("""int main()
{
    char a[] = "hello";
    char * p = a;
    printf("%s %u\n", a, sizeof a);
    printf("%s %u\n", p, sizeof p);
    // ++a; arrays cannot be incremented
    ++p; // pointers CAN be incremented
    return 0;
}
""")
        }
    }

    fun tryToSaveCode() {
        createDirectory()
        tryToSaveCodeAs(filename)
        tryToSaveCodeAs(backupFilename())
    }

    private fun createDirectory() {
        if (File(directory).mkdir()) {
            println("created directory $directory")
        }
    }

    private fun tryToSaveCodeAs(pathname: String) {
        try {
            println("saving code as $pathname")
            saveToFile(pathname)
        } catch (ex: Throwable) {
            println(ex)
        }
    }

    private fun backupFilename(): String {
        val hash = MessageDigest.getInstance("SHA").digest(text.toByteArray())
        val sb = StringBuilder(filenamePrefix)
        sb.append('_')
        for (b in hash) {
            sb.append("0123456789abcdef"[b.toInt().shr(4).and(0xf)])
            sb.append("0123456789abcdef"[b.toInt().and(0xf)])
        }
        return sb.append(filenameSuffix).toString()
    }
}
