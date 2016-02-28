package org.jetbrains.format.util

import org.junit.Test
import kotlin.test.assertEquals

import kotlin.test.assertFalse
import kotlin.test.assertTrue


/**
 * User: anlun
 */

class UtilTest {
    @Test fun toLines_Empty() {
        val str = ""
        val lines = str.toLines()
        assertEquals(0, lines.size, "Incorrect lines for empty string.")
    }

    @Test fun toLines_2Lines() {
        val str = "a\nb"
        val expected = listOf("a", "b")
        val lines = str.toLines()

        assertEquals(expected, lines, "Incorrect lines for 2 line string.")
    }

    @Test fun toLines_3LinesWithLastEmpty() {
        val str = "a\nb\n"
        val expected = listOf("a", "b", "")
        val lines = str.toLines()

        assertEquals(expected, lines, "Incorrect lines for 3 line string with last symbol equals to newline symbol.")
    }

    @Test fun toLines_3LinesWithFirstEmpty() {
        val str = "\na\nb"
        val expected = listOf("", "a", "b")
        val lines = str.toLines()

        assertEquals(expected, lines, "Incorrect lines for 3 line string with leading newline symbol.")
    }

    @Test fun getIndentation_FirstLineEmpty() {
        val str = "\nabc\nfoo\nbar"
        val expected = 0

        assertEquals(expected, str.getIndentation(), "Incorrect string indentation.")
    }

    @Test fun getIndentation_Test() {
        val str = " a\nabc\nfoo\nbar"
        val expected = 1

        assertEquals(expected, str.getIndentation(), "Incorrect string indentation.")
    }

    @Test fun startWhitespaceLength_EmptyList() {
        val l: List<String> = listOf()
        val expected = 0

        assertEquals(expected, startWhitespaceLength(l), "Incorrect start whitespace length.")
    }

    @Test fun startWhitespaceLength_Test1() {
        val l = listOf("  a", "   b", "  c", "     d")
        val expected = 2

        assertEquals(expected, startWhitespaceLength(l), "Incorrect start whitespace length.")
    }

    @Test fun startWhitespaceLength_Test2() {
        val l = listOf("  a", "   b", "  c", "     d", "e")
        val expected = 0

        assertEquals(expected, startWhitespaceLength(l), "Incorrect start whitespace length.")
    }

    @Test fun startWhitespaceLength_EmptyStartLine() {
        val l = listOf(" ", "   b", "  c", "     d")
        val expected = 2

        assertEquals(expected, startWhitespaceLength(l), "Incorrect start whitespace length.")
    }
}