package org.jetbrains.format

import org.junit.Test
import org.junit.Assert

/**
 * User: anlun
 */

class FormatTest {
    @Test fun toString_Empty() {
        val str = ""
        val fmt = str.toFormat()
        val fmtToStr = fmt.toString()

        Assert.assertEquals("Incorrect one line string to format transformation (by textToFormat).", str, fmtToStr)
    }

    @Test fun toString_1Line() {
        val str = "Hello test!"
        val fmt = Format.line(str)
        val fmtToStr = fmt.toString()

        Assert.assertEquals("Incorrect one line string to format transformation (by lineToFormat).", str, fmtToStr)
    }

    @Test fun toString_1LineByTextFunction() {
        val str = "Hello test!"
        val fmt = str.toFormat()
        val fmtToStr = fmt.toString()

        Assert.assertEquals("Incorrect one line string to format transformation (by textToFormat).", str, fmtToStr)
    }

    @Test fun toStringTest_2LineByTextFunction() {
        val str = "Hello\ntest!"
        val fmt = str.toFormat()
        val fmtToStr = fmt.toString()

        Assert.assertEquals("Incorrect multiline string to format transformation (by textToFormat).", str, fmtToStr)
    }


    @Test fun getIndented_PositiveIndent() {
        val str = "Hello\ntest!";
        val indentSize = 2
        val expectedStr = "  Hello\n  test!"

        val fmt = str.toFormat()
        val indentedFmt = fmt.getIndented(indentSize)
        val indentedFmtText = indentedFmt.toString()

        Assert.assertEquals("Incorrect indentation of format (indent size: $indentSize).", expectedStr, indentedFmtText)
    }

    @Test fun getIndented_ZeroIndent() {
        val str = "Hello\ntest!";
        val indentSize = 0
        val expectedStr = str

        val fmt = str.toFormat()
        val indentedFmt = fmt.getIndented(indentSize)
        val indentedFmtText = indentedFmt.toString()

        Assert.assertEquals("Incorrect indentation of format (indent size: $indentSize).", expectedStr, indentedFmtText)
    }

    @Test fun addAbove_Test() {
        val str1 = "Hello\ntest!"
        val str2 = "Goodbye\ntest!"
        val expectedStr = "$str1\n$str2"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        val fmtString = resultFmt.toString()

        Assert.assertEquals("Incorrect above addition of formats.", expectedStr, fmtString)
    }

    @Test fun addAbove_SecondEmpty() {
        val str1 = "Hello\ntest!"
        val str2 = ""
        val expectedStr = "$str1"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        val fmtString = resultFmt.toString()

        Assert.assertEquals("Incorrect above addition of formats (with empty one).", expectedStr, fmtString)
    }

    @Test fun addBeside_Test() {
        val str1 = "Test "
        val str2 = "Goodbye\ntest!"
        val expectedStr =
               "Test Goodbye\n" +
               "     test!"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        val fmtString = resultFmt.toString()

        Assert.assertEquals("Incorrect beside addition of formats.", expectedStr, fmtString)
    }

    @Test fun addBeside_FirstEmpty() {
        val str1 = ""
        val str2 = "Hello\ntest!"
        val expectedStr = "$str2"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        val fmtString = resultFmt.toString()

        Assert.assertEquals("Incorrect beside addition of formats (with empty one).", expectedStr, fmtString)
    }

    @Test fun addBeside_SecondSingle() {
        val str1 = "56\n  E\n  E"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        Assert.assertEquals("Incorrect middle width calculation.", 3, resultFmt.middleWidth)
    }

    @Test fun addBeside_FirstSingleSecondSingle() {
        val str1 = "56"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 / fmt2
        Assert.assertEquals("Incorrect middle width calculation.", 3, resultFmt.middleWidth)
    }

    @Test fun addAbove_SecondSingle() {
        val str1 = "56\n  E\n  E"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        Assert.assertEquals("Incorrect middle width calculation.", 3, resultFmt.middleWidth)
    }

    @Test fun addAbove_FirstSingleSecondSingle() {
        val str1 = "56"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 - fmt2
        Assert.assertEquals("Incorrect middle width calculation.", 2, resultFmt.middleWidth)
    }

    @Test fun addFill_SecondSingle() {
        val str1 = "56\n  E\n  E"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 + fmt2
        Assert.assertEquals("Incorrect middle width calculation.", 3, resultFmt.middleWidth)
    }

    @Test fun addFill_FirstSingleSecondSingle() {
        val str1 = "56"
        val fmt1 = str1.toFormat()
        val str2 = "E"
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 + fmt2
        Assert.assertEquals("Incorrect middle width calculation.", 3, resultFmt.middleWidth)
    }

    @Test fun addFillStyle_0FillConstant() {
        val str1 = "Test "
        val str2 = "Goodbye\ntest!"
        val expectedStr =
                "Test Goodbye\n" +
                "test!"

        val fmt1 = str1.toFormat()
        val fmt2 = str2.toFormat()
        val resultFmt = fmt1 + fmt2
        val fmtString = resultFmt.toString()

        Assert.assertEquals("Incorrect fill-style addition of formats.", expectedStr, fmtString)
    }

    @Test fun addFillStyle_2FillConstant() {
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

        Assert.assertEquals("Incorrect fill-style addition (with fill constant: $fillConstant) of formats.",
            expectedStr, fmtString)
    }

    @Test fun addEmptyLine_1() {
        val fmt1 = "a".toFormat().addEmptyLine()
        val fmt2 = "d".toFormat()
        val resFmt = fmt1.addAbove(fmt2)
        Assert.assertEquals("Incorrect empty line addition.", 3, resFmt.height)
    }

    @Test fun addEmptyLine_2() {
        val fmt1 = "a\nb".toFormat().addEmptyLine()
        val fmt2 = "d".toFormat()
        val resFmt = fmt1.addAbove(fmt2)
        Assert.assertEquals("Incorrect empty line addition.", 4, resFmt.height)
    }

    @Test fun addEmptyLine_3() {
        val fmt1 = "".toFormat().addEmptyLine()
        val fmt2 = "d".toFormat()
        val resFmt = fmt1.addAbove(fmt2)
        Assert.assertEquals("Incorrect empty line addition.", 2, resFmt.height)
    }

    @Test fun addFillStyle_SizeTest() {
        val fmt1 = "a\nbcd\ne".toFormat()
        val fmt2 = "ab\nc"    .toFormat()
        val resFmt = fmt1.addFillStyle(fmt2, 3)
        Assert.assertEquals(4, resFmt.totalWidth)
    }

    @Test fun middleWidth() {
        val fmt = "ab\nc"    .toFormat()
        Assert.assertEquals(2, fmt.middleWidth)
    }
}