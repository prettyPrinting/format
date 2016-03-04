package org.jetbrains.format

import org.junit.Test
import org.junit.Assert

/**
 * User: anlun
 */

public class FormatListTest {
    @Test fun crossWithTest_SecondEmpty() {
        val l1 = listOf(1)
        val l2: List<Int> = listOf()
        val func = { x: Int, y: Int -> x + y }
        val expectedList: List<Int> = listOf()

        val crossWith = l1.crossWith(l2, func)
        Assert.assertEquals("Incorrect crossWith in case of empty second list.", expectedList, crossWith)
    }

    @Test fun crossWith_Test() {
        val l1 = listOf(1, 2)
        val l2: List<Int> = listOf(2, 3, 4)
        val func = { x: Int, y: Int -> x + y }
        val expectedList: List<Int> = listOf(3, 4, 5, 4, 5, 6)

        val crossWith = l1.crossWith(l2, func)
        Assert.assertEquals("Incorrect crossWith.", expectedList, crossWith)
    }
}