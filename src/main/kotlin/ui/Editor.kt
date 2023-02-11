package ui

import freditor.Autosaver
import freditor.FreditorUI
import freditor.JavaIndenter

class Editor : FreditorUI(Flexer, JavaIndenter.instance, 0, 25) {
    val autosaver: Autosaver = newAutosaver("skorbut")

    init {
        autosaver.loadOrDefault(
            """int main()
{
    char a[] = "hello";
    char * p = a;
    printf("%s %u\n", a, sizeof a);
    printf("%s %u\n", p, sizeof p);
    // ++a; arrays cannot be incremented
    ++p; // pointers CAN be incremented
    return 0;
}
"""
        )
    }
}
