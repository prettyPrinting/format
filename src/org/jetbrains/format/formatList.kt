package org.jetbrains.format

import java.util.ArrayList
import org.jetbrains.format.toFormat
import org.jetbrains.format.Format
import org.jetbrains.format.FormatSet

/**
 * User: anlun
 */

// List extensions
fun <T, B, R> List<T>.crossWith(yl: List<B>, f: (T, B) -> R): List<R> {
    val result = ArrayList<R>(size * yl.size)
    forEach { x ->
        yl.forEach { y ->
            result.add(f(x, y))
        }
    }

    return result
}

fun List<Format>.addAbove             (fl: List<Format>): List<Format> = crossWith(fl, {x, y -> x - y})
fun List<Format>.addBeside            (fl: List<Format>): List<Format> = crossWith(fl, {x, y -> x / y})
fun List<Format>.addFillStyle         (fl: List<Format>): List<Format> = crossWith(fl, {x, y -> x + y})
fun List<Format>.addAboveWithEmptyLine(fl: List<Format>): List<Format> = crossWith(fl, {x, y -> x % y})
fun List<Format>.addFillStyle(fl: List<Format>, shiftConstant: Int): List<Format> =
        crossWith(fl, {x, y -> x.addFillStyle(y, shiftConstant)})

fun List<Format>.minus(fl: List<Format>) = addAbove(fl)
fun List<Format>.minus(f : Format)       = addAbove(formatToList(f))

fun List<Format>.mod  (fl: List<Format>) = addAboveWithEmptyLine(fl)
fun List<Format>.mod  (f : Format)       = addAboveWithEmptyLine(formatToList(f))

fun List<Format>.div  (fl: List<Format>) = addBeside(fl)
fun List<Format>.div  (f : Format)       = addBeside(formatToList(f))

fun List<Format>.plus (fl: List<Format>) = addFillStyle(fl)
fun List<Format>.plus (f : Format)       = addFillStyle(formatToList(f))

fun FormatSet.emptyFormatList(): List<Format> = ArrayList()
fun initialFormatList(): List<Format> {
    val list = ArrayList<Format>(1)
    list.add(Format.empty)
    return list
}
fun formatToList(f: Format): List<Format> {
    val list = ArrayList<Format>(1)
    list.add(f)
    return list
}
fun String?.toFormatList() = formatToList(toFormat())