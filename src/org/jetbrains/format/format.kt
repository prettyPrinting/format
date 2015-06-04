package org.jetbrains.format

import java.util.ArrayList
import java.util.Arrays
import org.jetbrains.format.util.toLines
import org.jetbrains.format.util.startWhitespaceLength
/**
 * User: anlun
 */

/**
 * @ref{}
 */
class Format private constructor(
  val         height: Int
, val firstLineWidth: Int
, val    middleWidth: Int // For formats which have height <= 2 middleWidth should be equal to firstLineWidth
, val  lastLineWidth: Int
, val         toText: (Int, String) -> String
) {
    companion object {
        public val empty: Format = Format(0, 0, 0, 0, { _, t -> t})
        public fun line(s: String): Format {
            val len = s.length()
            return Format(1, len, len, len, {_, t -> s + t})
        }

        public fun text(s: String?): Format = (s ?: "").toLines().fold(empty) {
            curFmt, line -> curFmt - line(line)
        }

        public fun text(s: String?, lengthToDrop: Int): Format {
            val lines = (s ?: "").toLines()
            if (lines.isEmpty()) { return empty }

            val head = lines[0]
            val tail = lines.drop(1)
            val lengthToDrop = Math.min(lengthToDrop, startWhitespaceLength(tail))
            val newTail = tail.map { line -> line.drop(lengthToDrop) }
            val newLines = ArrayList<String>(lines.size())
            newLines.add(head)
            newLines.addAll(newTail)

            return newLines.fold(empty, { curFmt, line -> curFmt - line(line) })
        }
    }

    public val totalWidth: Int = listOf(firstLineWidth, middleWidth, lastLineWidth).max() ?: middleWidth

    private fun sp(n: Int): String {
        val s = CharArray(n)
        Arrays.fill(s, ' ')
        return String(s)
    }

    override public fun toString()     : String = toText(0, "")
    public fun getTextErased(): Format = Format(height, firstLineWidth, middleWidth, lastLineWidth, { n, t -> ""})

    public fun sizeEqual(s: Format): Boolean =
           height         == s.height
        && firstLineWidth == s.firstLineWidth
        &&    middleWidth == s.middleWidth
        && lastLineWidth  == s.lastLineWidth

    public fun lower(s: Format): Boolean =
            height < s.height || (height == s.height &&  totalWidth < s.totalWidth)

    public fun getIndented(iSize: Int): Format =
        Format(height, iSize + firstLineWidth, iSize + middleWidth, iSize + lastLineWidth
             , { n, t -> sp(iSize) + toText(iSize + n, t) }
        )

    public fun addAbove(f: Format): Format {
        if (  height == 0) { return  f }
        if (f.height == 0) { return this }

        val newMiddleWidth : Int
        when {
            height == 1 && f.height == 1 -> {
                newMiddleWidth = firstLineWidth
            }
            height == 1 && f.height == 2 -> {
                newMiddleWidth = f.firstLineWidth
            }
            height == 1 && f.height >= 2 -> {
                newMiddleWidth = Math.max(f.firstLineWidth, f.middleWidth)
            }
            height == 2 && f.height == 1 -> {
                newMiddleWidth = lastLineWidth
            }
            height >= 2 && f.height == 1 -> {
                newMiddleWidth = Math.max(middleWidth, lastLineWidth)
            }
            else -> {
                newMiddleWidth = listOf(middleWidth, lastLineWidth, f.firstLineWidth, f.middleWidth).max()
                        ?: f.middleWidth
            }
        }
        //val newMiddleWidth = listOf(if (height > 1) Math.max(middleWidth, lastLineWidth) else 0
        //                          , f.firstLineWidth, f.middleWidth
        //                     ).max() ?: f.middleWidth

        val newHeight      = height + f.height
        val txtFunc = { n: Int, t: String -> toText(n, "\n" + sp(n) + f.toText(n, t)) }
        return Format(newHeight, firstLineWidth, newMiddleWidth, f.lastLineWidth, txtFunc)
    }

    public fun addBeside(f: Format): Format {
        if (  height == 0) { return  f }
        if (f.height == 0) { return this }

        val newMiddleWidth : Int
        when {
            height   == 1 && f.height <= 2 -> {
                newMiddleWidth = firstLineWidth + f.firstLineWidth
            }
            height   == 1 && f.height >  2 -> {
                newMiddleWidth = firstLineWidth + f.middleWidth
            }
            height   == 2 && f.height == 1 -> {
                newMiddleWidth = firstLineWidth
            }
            height   >  2 && f.height == 1 -> {
                newMiddleWidth = middleWidth
            }
            else -> {
                newMiddleWidth =
                        listOf(middleWidth, lastLineWidth + f.firstLineWidth, lastLineWidth + f.middleWidth).max()
                                ?: lastLineWidth + f.middleWidth
            }
        }

        val newHeight         = height + f.height - 1
        val newFirstLineWidth = firstLineWidth + (if (height == 1) f.firstLineWidth else 0)
        val newLastLineWidth  = lastLineWidth + f.lastLineWidth
        val txtFunc = { n: Int, t: String  -> toText(n, f.toText(n + lastLineWidth, t)) }
        return Format(newHeight, newFirstLineWidth, newMiddleWidth, newLastLineWidth, txtFunc)
    }

    public fun addFillStyle(f: Format, shiftConstant: Int): Format {
        if (  height == 0) { return  f }
        if (f.height == 0) { return this }

        val newHeight         = height + f.height - 1
        val newMiddleWidth : Int
        when {
            height   == 1 && f.height <= 2 -> {
                newMiddleWidth = firstLineWidth + f.firstLineWidth
            }
            height   == 1 && f.height >  2 -> {
                newMiddleWidth = shiftConstant + f.middleWidth
            }
            height   == 2 && f.height >  2 -> {
                newMiddleWidth = Math.max(lastLineWidth + f.firstLineWidth, shiftConstant + f.middleWidth)
            }
            height   == 2 && f.height == 1 -> {
                newMiddleWidth = firstLineWidth
            }
            height   >  2 && f.height == 1 -> {
                newMiddleWidth = middleWidth
            }
            height   >  2 && f.height == 2 -> {
                newMiddleWidth = Math.max(middleWidth, lastLineWidth + f.firstLineWidth)
            }
            else -> {
                val values = listOf(middleWidth, lastLineWidth + f.firstLineWidth, shiftConstant + f.middleWidth)
                newMiddleWidth = values.max() ?: shiftConstant + f.middleWidth
            }
        }

        val newFirstLineWidth = firstLineWidth + (if (height == 1) f.firstLineWidth else 0)
        val newLastLineWidth = f.lastLineWidth + (if (f.height == 1) lastLineWidth else shiftConstant)
        val txtFunc = { n: Int, t: String -> toText(n, f.toText(n + shiftConstant, t)) }
        return Format(newHeight, newFirstLineWidth, newMiddleWidth, newLastLineWidth, txtFunc)
    }
    public fun addFillStyle(f: Format): Format = addFillStyle(f, 0)
    public fun addEmptyLine(): Format = Format(height + 1, firstLineWidth, middleWidth, 0
                                            , { n, t -> toText(n, "\n" + t) }
                                        )

    public fun minus(f: Format): Format = addAbove(f)

    public fun mod(f: Format): Format {
        if (  height == 0) { return f }
        if (f.height == 0) { return this }

        return this - line("") - f
    }

    public fun div (f: Format): Format = addBeside(f)
    public fun plus(f: Format): Format = addFillStyle(f)
}

fun String?.toFormat(): Format = Format.text(this)