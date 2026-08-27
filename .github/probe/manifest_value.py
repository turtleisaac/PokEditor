"""Reads one main-section attribute from a jar manifest on stdin.

A manifest is wrapped to 72 bytes and continued with a leading single space, and the break can
fall in the middle of a token: the Add-Exports value this repository ships spans three lines and
currently splits java.desktop/sun.awt.shell across two of them. Which tokens get split moves
whenever the value changes, so reading the raw file is unreliable in both directions -- a package
that is present can be unfindable, and splitting one yields a garbage token rather than an
obvious error. Unfolding first is the only way to read the value as the JVM does.

Only the main section is considered: per-entry sections follow the first blank line and may
repeat attribute names.
"""
import sys

name = sys.argv[1]
text = sys.stdin.buffer.read().decode("utf-8", "replace").replace("\r\n", "\n")
main_section = text.split("\n\n", 1)[0]

unfolded = []
for line in main_section.split("\n"):
    if line.startswith(" ") and unfolded:
        unfolded[-1] += line[1:]
    else:
        unfolded.append(line)

for line in unfolded:
    if line.startswith(name + ":"):
        print(line.split(":", 1)[1].strip())
        break
