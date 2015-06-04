package org.jetbrains.format.util

import java.util.ArrayList

fun String.countWhileEqualsTo(ch: Char): Int {
    var count = 0
    for (c in this) if (c == ch) count++ else break
    return count
}

fun String.getIndentation  (): Int = countWhileEqualsTo(' ')
fun String.countLeadingTabs(): Int = countWhileEqualsTo('\t')

/** Calculates minimum indentation for lines. */
fun startWhitespaceLength(lines: List<String>): Int {
    val nonEmptyLines = lines.filter { l -> l.trim().length() != 0 }
    if (nonEmptyLines.size() <= 0) { return 0 }

    val firstLineLength = nonEmptyLines[0].length()
    val result = nonEmptyLines.fold(firstLineLength) { r, line ->
        val leadingWhitespaceCount = line.getIndentation()
        Math.min(r, leadingWhitespaceCount)
    }
    return Math.max(result, 0)
}


fun String.toLines(): List<String> {
    if (length() <= 0) return ArrayList()

    val replaced = ("#" + this + "#").replace("\n", "#\n#")
    val splitted = replaced.splitBy("\n")
    return splitted map { p -> p.substring(1, p.length() - 1) }
}