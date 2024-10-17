![swap](skorbut.png)

## What is Skorbut?

Skorbut is a simple teaching environment for a subset of C with a memory visualizer.
If you ever had trouble visualizing arrays and pointers, you have come to the right place.

*Skorbut* is German for *scurvy*, an illness that is caused by a lack of Vitamin C.
Skorbut also lacks C in the sense that it implements only a restricted subset of C.

## Getting started

Please take the time to **read the following instructions carefully.**
Most problems stem from skipping or misunderstanding important steps.

### ☕ Windows & macOS

1. Visit https://adoptium.net

2. Click "Latest release" button to download Java installer

3. Wait for download to finish

4. Open the `Downloads` folder (via Windows Explorer or Finder/Spotlight, respectively) and double-click `OpenJDK...` to start Java installer

5. Click Next, Next, Install, Finish

6. Click [skorbut.jar](https://raw.githubusercontent.com/fredoverflow/skorbut/release/skorbut.jar) to download Skorbut<br>
**If Skorbut fails to download**, continue with ⚠️ Troubleshooting *Windows*, or ⚠️ Troubleshooting *macOS*

7. Open the `Downloads` folder and double-click `skorbut.jar` to start Skorbut<br>
**If Skorbut fails to start**, continue with ⚠️ Troubleshooting *Windows*, or ⚠️ Troubleshooting *macOS*

### ⚠️ Troubleshooting *Windows*

Steps 1 through 5 (install Java) worked, but steps 6 (download Skorbut) or 7 (start Skorbut) failed? Then read on.

- Move your mouse over the script below
- A button appears in the top right corner of the script
- Click that button to copy the script
```cmd
cd Downloads
if exist skorbut.jar.zip erase skorbut.jar.zip
curl -o skorbut.jar https://raw.githubusercontent.com/fredoverflow/skorbut/release/skorbut.jar
echo java -version > skorbut.cmd
echo java -jar skorbut.jar >> skorbut.cmd
skorbut.cmd

```
- Press the Windows key (the key on the bottom left with the Windows logo ⊞ on it)
- Write `cmd` and confirm with Enter
- A terminal appears
- Right-click anywhere inside that terminal to paste and execute the script

From now on, simply double-click `skorbut.cmd` in the `Downloads` folder to start Skorbut.<br>
Feel free to move `skorbut.jar` and `skorbut.cmd` to the Desktop or any other folder you prefer.

### ⚠️ Troubleshooting *macOS*

Steps 1 through 5 (install Java) worked, but steps 6 (download Skorbut) or 7 (start Skorbut) failed? Then read on.

- Move your mouse over the script below
- A button appears in the top right corner of the script
- Click that button to copy the script
```sh
cd Downloads
curl -o skorbut.jar https://raw.githubusercontent.com/fredoverflow/skorbut/release/skorbut.jar
chmod +x skorbut.jar
echo java -version > skorbut.sh
echo java -jar skorbut.jar >> skorbut.sh
chmod +x skorbut.sh
./skorbut.sh

```
- Press `Command⌘ Space` (or click the magnifying glass 🔍 in the top right corner of the screen) to open Spotlight
- Write `terminal` and confirm with Enter
- A terminal appears
- Press `Command⌘ V` to paste and execute the script

From now on, simply double-click `skorbut.sh` in the `Downloads` folder to start Skorbut.<br>
Feel free to move `skorbut.jar` and `skorbut.sh` to the Desktop or any other folder you prefer.

### 🐧 Ubuntu, Linux Mint, Debian...

```sh
sudo apt install default-jdk
cd Downloads
curl -o skorbut.jar https://raw.githubusercontent.com/fredoverflow/skorbut/release/skorbut.jar
chmod +x skorbut.jar
echo java -version > skorbut.sh
echo java -jar -Dsun.java2d.opengl=True skorbut.jar >> skorbut.sh
chmod +x skorbut.sh
./skorbut.sh

```

From now on, simply double-click `skorbut.sh` in the `Downloads` folder to start Skorbut.<br>
Feel free to move `skorbut.jar` and `skorbut.sh` to the Desktop or any other folder you prefer.

### I would rather compile Skorbut from source!

```
git clone https://github.com/fredoverflow/freditor
cd freditor
mvn install
cd ..
git clone https://github.com/fredoverflow/skorbut
cd skorbut
mvn package
```

The executable `skorbut.jar` will be located inside the `target` folder.

## How do I save my code?

The code is automatically saved to a new file each time you click the start button.
The save folder is named `skorbut`, and it is located in your home directory.
The full path is displayed in the title bar.

## Does Skorbut support auto-indentation?

Yes, just hit Enter or Tab.

## What about auto-completion?

Ctrl+Space after an identifier auto-completes to the longest common prefix of all identifiers in scope.
For example, if `foo`, `bar` and `baz` are in scope, then `f` will be auto-completed to `foo`,
but `b` will only be auto-completed to `ba`, because both `bar` and `baz` start with `ba`.

Skorbut has scope-aware auto-completion for all identifiers *except* `struct` members;
those are auto-completed globally, i.e. after `.` or `->`, *all* `struct` members are considered for auto-completion.
(That's because auto-completion has no type information available yet. Changing this would be a Herculean task.)

## What features of C are currently missing?

Non-exhaustive list off the top of my head:

| Feature             | Priority |
| ------------------- | -------- |
| preprocessor        | very low |
| variadic functions  | very low |
| compound assignment | low      |
| null pointer        | medium   |
| union               | very low |
| return struct       | low      |

## Wait, no preprocessor? How do I `#include <stdio.h>`?

You don't need to include anything, the following standard library functions are already available:

- printf
- scanf
- puts
- putchar
- getchar
- malloc
- free
- realloc
- qsort
- bsearch
- strlen
- strcmp
- pow
- time

## What about my own header files?

Skorbut does not support multiple translation units, so there would be no point in supporting header files.

## How do I define constants without `#define`?

For integral constants, you can use anonymous enumerations like `enum { N = 10 };`

## Keyboard shortcuts

| Windows      | Effect                | Macintosh        |
| -----------: | :-------------------: | ---------------- |
| F1           | show type             | F1               |
| F3           | declaration<br>usages | F3               |
| F5           | step into             | F5               |
| F6           | step over             | F6               |
| F7           | step return           | F7               |
| Tab<br>Enter | auto-indent           | Tab<br>Enter     |
| Ctrl Space   | auto-complete         | Control (Shift) Space |
| Ctrl Alt R   | rename symbol         | Command Option R |
| Ctrl D       | delete line           | Command D        |
| Ctrl C       | copy                  | Command C        |
| Ctrl X       | cut                   | Command X        |
| Ctrl V       | paste                 | Command V        |
| Ctrl Z       | undo                  | Command Z        |
| Ctrl Y       | redo                  | Command Y        |
