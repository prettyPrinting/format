package org.jetbrains.format.util

import java.util.ArrayList

class LengthBuilder (
    var length : Int = 0
): Appendable {
    override fun append(csq: CharSequence): Appendable {
        length += csq.length
        return this
    }
    override fun append(csq: CharSequence, start: Int, end: Int): Appendable {
        if (start < 0 || end < 0 || end < start) throw IndexOutOfBoundsException()
        length = end - start
        return this
    }
    override fun append(_: Char): Appendable {
        length++
        return this
    }
}

fun String.getIndentation  (): Int = takeWhileTo(LengthBuilder(), { c -> c == ' '  }).length
fun String.countLeadingTabs(): Int = takeWhileTo(LengthBuilder(), { c -> c == '\t' }).length

/** Calculates minimum indentation for lines. */
fun startWhitespaceLength(lines: List<String>): Int {
    val nonEmptyLines = lines.filter { l -> l.trim().length != 0 }
    if (nonEmptyLines.size <= 0) { return 0 }

    val firstLineLength = nonEmptyLines[0].size
    val result = nonEmptyLines.fold(firstLineLength) { (r, line) ->
        val leadingWhitespaceCount = line.getIndentation()
        Math.min(r, leadingWhitespaceCount)
    }
    return Math.max(result, 0)
}


fun String.toLines(): List<String> {
    if (length <= 0) return ArrayList<String>()

    val replaced = ("#" + this + "#").replace("\n", "#\n#")
    val splitted = replaced.split("\n")
    return splitted map { p -> p.substring(1, p.length - 1) }
}