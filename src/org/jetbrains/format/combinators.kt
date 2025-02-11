package org.jetbrains.format.combinators

import org.jetbrains.format.FormatSet

fun String.toSet(width: Int) = FormatSet.initial(width, this)

operator fun String.div(fs: FormatSet): FormatSet = this.toSet(fs.width) / fs
operator fun FormatSet.div(s: String): FormatSet = this / s.toSet(this.width)

operator fun String.minus(fs: FormatSet): FormatSet = this.toSet(fs.width) - fs
operator fun FormatSet.minus(s: String): FormatSet = this - s.toSet(this.width)

operator fun String.plus(fs: FormatSet): FormatSet = this.toSet(fs.width) + fs
operator fun FormatSet.plus(s: String): FormatSet = this + s.toSet(this.width)

operator fun String.rem(fs: FormatSet): FormatSet = this.toSet(fs.width).rem(fs)
operator fun FormatSet.rem(s: String): FormatSet = this.rem(s.toSet(this.width))

infix fun FormatSet.choiceUpd(fs: FormatSet) = addAll(fs)
