package org.jetbrains.format.combinators

import org.jetbrains.format.FormatSet

fun String.toSet(width: Int) = FormatSet.initial(width, this)

operator fun String.div(fs: FormatSet): FormatSet = this.toSet(fs.width) / fs
operator fun FormatSet.div(s: String): FormatSet = this / s.toSet(this.width)

operator fun String.minus(fs: FormatSet): FormatSet = this.toSet(fs.width) - fs
operator fun FormatSet.minus(s: String): FormatSet = this - s.toSet(this.width)

operator fun String.plus(fs: FormatSet): FormatSet = this.toSet(fs.width) + fs
operator fun FormatSet.plus(s: String): FormatSet = this + s.toSet(this.width)

operator fun String.mod(fs: FormatSet): FormatSet = this.toSet(fs.width) % fs
operator fun FormatSet.mod(s: String): FormatSet = this % s.toSet(this.width)

infix fun FormatSet.choiceUpd(fs: FormatSet) = addAll(fs)
