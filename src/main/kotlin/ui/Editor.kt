package ui

import freditor.FreditorUI
import freditor.JavaIndenter

import java.io.File
import java.io.IOException
import java.security.MessageDigest

class Editor : FreditorUI(Flexer, JavaIndenter.instance, 0, 25) {
    companion object {
        val directory = "${System.getProperty("user.home")}${File.separator}skorbut${File.separator}"
        val filename = "${directory}!skorbut.txt"
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
        val dir = File(directory)
        if (dir.mkdir()) {
            println("created directory $dir")
        }
    }

    private fun tryToSaveCodeAs(pathname: String) {
        try {
            println("saving code as $pathname")
            saveToFile(pathname)
        } catch (ex: IOException) {
            println(ex)
        }
    }

    private fun backupFilename(): String {
        val sha1 = MessageDigest.getInstance("SHA")
        val text = text.toByteArray(Charsets.ISO_8859_1)
        val hash = sha1.digest(text)
        val builder = StringBuilder(directory)
        for (byte in hash) {
            val x = byte.toInt()
            builder.append("0123456789abcdef"[x.ushr(4).and(15)])
            builder.append("0123456789abcdef"[x.and(15)])
        }
        return builder.append(".txt").toString()
    }
}
