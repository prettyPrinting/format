package org.jetbrains.format.util

import org.junit.Test
import org.junit.Assert

/**
 * User: anlun
 */

class UtilTest {
    @Test fun toLines_Empty() {
        val str = ""
        val lines = str.toLines()
        Assert.assertEquals("Incorrect lines for empty string.", 0, lines.size)
    }

    @Test fun toLines_2Lines() {
        val str = "a\nb"
        val expected = listOf("a", "b")
        val lines = str.toLines()

        Assert.assertEquals("Incorrect lines for 2 line string.", expected, lines)
    }

    @Test fun toLines_3LinesWithLastEmpty() {
        val str = "a\nb\n"
        val expected = listOf("a", "b", "")
        val lines = str.toLines()

        Assert.assertEquals("Incorrect lines for 3 line string with last symbol equals to newline symbol.",
            expected, lines)
    }

    @Test fun toLines_3LinesWithFirstEmpty() {
        val str = "\na\nb"
        val expected = listOf("", "a", "b")
        val lines = str.toLines()

        Assert.assertEquals("Incorrect lines for 3 line string with leading newline symbol.",
            expected, lines)
    }

    @Test fun getIndentation_FirstLineEmpty() {
        val str = "\nabc\nfoo\nbar"
        val expected = 0

        Assert.assertEquals("Incorrect string indentation.", expected, str.getIndentation())
    }

    @Test fun getIndentation_Test() {
        val str = " a\nabc\nfoo\nbar"
        val expected = 1

        Assert.assertEquals("Incorrect string indentation.", expected, str.getIndentation())
    }

    @Test fun startWhitespaceLength_EmptyList() {
        val l: List<String> = listOf()
        val expected = 0

        Assert.assertEquals("Incorrect start whitespace length.", expected, startWhitespaceLength(l))
    }

    @Test fun startWhitespaceLength_Test1() {
        val l = listOf("  a", "   b", "  c", "     d")
        val expected = 2

        Assert.assertEquals("Incorrect start whitespace length.", expected, startWhitespaceLength(l))
    }

    @Test fun startWhitespaceLength_Test2() {
        val l = listOf("  a", "   b", "  c", "     d", "e")
        val expected = 0

        Assert.assertEquals("Incorrect start whitespace length.", expected, startWhitespaceLength(l))
    }

    @Test fun startWhitespaceLength_EmptyStartLine() {
        val l = listOf(" ", "   b", "  c", "     d")
        val expected = 2

        Assert.assertEquals("Incorrect start whitespace length.", expected, startWhitespaceLength(l))
    }
}