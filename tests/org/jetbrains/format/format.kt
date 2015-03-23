package org.jetbrains.format

import org.junit.Test
import org.jetbrains.format.toFormat
import org.jetbrains.format.Format
import kotlin.test.assertEquals

/**
 * User: anlun
 */

public class FormatTest {
    Test fun toString_Empty() {
        val str = ""
        val fmt = str.toFormat()
        val fmtToStr = fmt.toString()

        assertEquals(str, fmtToStr, "Incorrect one line string to format transformation (by textToFormat).")
    }

    Test fun toString_1Line() {
        val str = "Hello test!"
        val fmt = Format.line(str)
        val fmtToStr = fmt.toString()

        assertEquals(str, fmtToStr, "Incorrect one line string to format transformation (by lineToFormat).")
    }

    Test fun toString_1LineByTextFunction() {
        val str = "Hello test!"
        val fmt = str.toFormat()
        val fmtToStr = fmt.toString()

        assertEquals(str, fmtToStr, "Incorrect one line string to format transformation (by textToFormat).")
    }

    Test fun toStringTest_2LineByTextFunction() {
        val str = "Hello\ntest!"
        val fmt = str.toFormat()
        val fmtToStr = fmt.toString()

        assertEquals(str, fmtToStr, "Incorrect multiline string to format transformation (by textToFormat).")
    }


    Test fun getIndented_PositiveIndent() {
        val str = "Hello\ntest!";
        val indentSize = 2
        val expectedStr = "  Hello\n  test!"

        val fmt = str.toFormat()
        val indentedFmt = fmt.getIndented(indentSize)
        val indentedFmtText = indentedFmt.toString()

        assertEquals(expectedStr, indentedFmtText, "Incorrect indentation of format (indent size: $indentSize).")
    }

    Test fun getIndented_ZeroIndent() {
        val str = "Hello\ntest!";
        val indentSize = 0
        val expectedStr = str

        val fmt = str.toFormat()
        val indentedFmt = fmt.getIndented(indentSize)
        val indentedFmtText = indentedFmt.toString()

        assertEquals(expectedStr, indentedFmtText, "Incorrect indentation of format (indent size: $indentSize).")
    }

    Test fun addAbove_Test() {
        val str1 = "Hello\ntest!"
        val str2 = "Goodbye\ntest!"
        val expectedStr = "$str1\n$str2"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        val fmtString = resultFmt.toString()

        assertEquals(expectedStr, fmtString, "Incorrect above addition of formats.")
    }

    Test fun addAbove_SecondEmpty() {
        val str1 = "Hello\ntest!"
        val str2 = ""
        val expectedStr = "$str1"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        val fmtString = resultFmt.toString()

        assertEquals(expectedStr, fmtString, "Incorrect above addition of formats (with empty one).")
    }

    Test fun addBeside_Test() {
        val str1 = "Test "
        val str2 = "Goodbye\ntest!"
        val expectedStr =
               "Test Goodbye\n" +
               "     test!"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        val fmtString = resultFmt.toString()

        assertEquals(expectedStr, fmtString, "Incorrect beside addition of formats.")
    }

    Test fun addBeside_FirstEmpty() {
        val str1 = ""
        val str2 = "Hello\ntest!"
        val expectedStr = "$str2"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        val fmtString = resultFmt.toString()

        assertEquals(expectedStr, fmtString, "Incorrect beside addition of formats (with empty one).")
    }

    Test fun addBeside_SecondSingle() {
        val str1 = "56\n  E\n  E"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        assertEquals(3, resultFmt.middleWidth, "Incorrect middle width calculation.")
    }

    Test fun addBeside_FirstSingleSecondSingle() {
        val str1 = "56"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        assertEquals(3, resultFmt.middleWidth, "Incorrect middle width calculation.")
    }

    Test fun addAbove_SecondSingle() {
        val str1 = "56\n  E\n  E"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        assertEquals(3, resultFmt.middleWidth, "Incorrect middle width calculation.")
    }

    Test fun addAbove_FirstSingleSecondSingle() {
        val str1 = "56"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        assertEquals(2, resultFmt.middleWidth, "Incorrect middle width calculation.")
    }

    Test fun addFill_SecondSingle() {
        val str1 = "56\n  E\n  E"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 + fmt2
        assertEquals(3, resultFmt.middleWidth, "Incorrect middle width calculation.")
    }

    Test fun addFill_FirstSingleSecondSingle() {
        val str1 = "56"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 + fmt2
        assertEquals(3, resultFmt.middleWidth, "Incorrect middle width calculation.")
    }

    Test fun addFillStyle_0FillConstant() {
        val str1 = "Test "
        val str2 = "Goodbye\ntest!"
        val expectedStr =
                "Test Goodbye\n" +
                "test!"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 + fmt2
        val fmtString = resultFmt.toString()

        assertEquals(expectedStr, fmtString, "Incorrect fill-style addition of formats.")
    }

    Test fun addFillStyle_2FillConstant() {
        val str1 = "Test "
        val str2 = "Goodbye\ntest!"
        val fillConstant = 2
        val expectedStr =
                "Test Goodbye\n" +
                "  test!"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1.addFillStyle(fmt2, fillConstant)
        val fmtString = resultFmt.toString()

        assertEquals(expectedStr, fmtString, "Incorrect fill-style addition (with fill constant: $fillConstant) of formats.")
    }

    Test fun addEmptyLine_1() {
        val fmt1 = "a".toFormat().addEmptyLine()
        val fmt2 = "d".toFormat()
        val resFmt = fmt1.addAbove(fmt2)
        assertEquals(3, resFmt.height, "Incorrect empty line addition.")
    }

    Test fun addEmptyLine_2() {
        val fmt1 = "a\nb".toFormat().addEmptyLine()
        val fmt2 = "d".toFormat()
        val resFmt = fmt1.addAbove(fmt2)
        assertEquals(4, resFmt.height, "Incorrect empty line addition.")
    }

    Test fun addEmptyLine_3() {
        val fmt1 = "".toFormat().addEmptyLine()
        val fmt2 = "d".toFormat()
        val resFmt = fmt1.addAbove(fmt2)
        assertEquals(2, resFmt.height, "Incorrect empty line addition.")
    }

    Test fun addFillStyle_SizeTest() {
        val fmt1 = "a\nbcd\ne".toFormat()
        val fmt2 = "ab\nc"    .toFormat()
        val resFmt = fmt1.addFillStyle(fmt2, 3)
        assertEquals(4, resFmt.totalWidth)
    }

    Test fun middleWidth() {
        val fmt = "ab\nc"    .toFormat()
        assertEquals(2, fmt.middleWidth)
    }
}