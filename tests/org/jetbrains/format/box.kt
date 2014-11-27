package org.jetbrains.format.box

import org.junit.Test
import kotlin.test.assertEquals
import org.jetbrains.format.box.Box
import org.jetbrains.format.box.toBox

/**
 * User: anlun
 */

public class BoxTest {
    Test fun stringToBox_Empty() {
        val str = ""
        val expected = Box(0, 0)
        val result = str.toBox(1)

        assertEquals(expected, result, "Incorrect Box for empty string.")
    }

    Test fun stringToBox_Test() {
        val str = "Hello\nBox"
        val startLineOffset = 0
        val expected = Box(5, 2)
        val result = str.toBox(startLineOffset)

        assertEquals(expected, result, "Incorrect Box for string.")
    }
}