package org.jetbrains.format

import org.junit.Test
import kotlin.test.assertEquals
import org.jetbrains.format.toFormat
import org.jetbrains.format.FormatLine

/**
 * User: anlun
 */
class FormatSetTest {
    private fun formatLine(): FormatLine {
        val width = 100
        val fl = FormatLine(width)
        fl.add("A\nB".toFormat())
        fl.add("C".toFormat())
        fl.add("AB\nCE".toFormat())
        fl.add("AD".toFormat())
        return fl
    }

    @Test fun formatLine_1() {
        val fl = formatLine()
        assertEquals(2, fl.size())
    }

    @Test fun formatLine_iterate() {
        var count = 0
        for (f in formatLine()) {
            count++
        }
        assertEquals(2, count)
    }
}
