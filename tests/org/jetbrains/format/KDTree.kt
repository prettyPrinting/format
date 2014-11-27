package org.jetbrains.format

import org.junit.Test
import kotlin.test.assertEquals
import org.jetbrains.format.research.KDTree

/**
 * User: anlun
 */

class KDTreeTest {
    private val comparator: (Int, Int) -> Boolean = { (a, b) -> a < b }
    private val n = 2

    Test fun factorizeInsert_1() {
        val tree = KDTree<Int>(1, array(0), array(n))
        tree.factorizeInsert(array(1),  0, comparator)
        tree.factorizeInsert(array(1), -1, comparator)
        tree.factorizeInsert(array(0), -2, comparator)
        val list = tree.getValues()
        assertEquals(1, list.size)
    }

    Test fun factorizeInsert_2() {
        val tree = KDTree<Int>(2, array(0, 0), array(n, n))
        tree.factorizeInsert(array(1, 1),  0, comparator)
        tree.factorizeInsert(array(1, 1), -1, comparator)
        tree.factorizeInsert(array(0, 1), -2, comparator)
        val list = tree.getValues()
        assertEquals(1, list.size)
    }

    Test fun factorizeInsert_2_1() {
        val tree = KDTree<Int>(2, array(0, 0), array(n, n))
        tree.factorizeInsert(array(1, 1),  0, comparator)
        tree.factorizeInsert(array(0, 1), -1, comparator)
        tree.factorizeInsert(array(1, 0), -1, comparator)
        tree.factorizeInsert(array(0, 0), -2, comparator)
        val list = tree.getValues()
        assertEquals(1, list.size)
    }

    Test fun factorizeInsert_2_3() {
        val tree = KDTree<Int>(2, array(0, 0), array(n, n))
        tree.factorizeInsert(array(1, 1),  0, comparator)
        tree.factorizeInsert(array(0, 1), -1, comparator)
        tree.factorizeInsert(array(1, 0), -1, comparator)
        tree.factorizeInsert(array(0, 0), -2, comparator)
        tree.factorizeInsert(array(1, 1), -3, comparator)
        val list = tree.getValues()
        assertEquals(2, list.size)
    }

    /*
    Test fun insert() {
        val k = 50
        val n = 100
        val tree = KDTree<Int>(1, array(0), array(k))

        val comparator: (Int, Int) -> Boolean = { (a, b) -> a < b }
        for (i in 0..n) {
            tree.factorizeInsert(array(i % k), 0, comparator)
        }
        tree.factorizeInsert(array(0),  10, comparator)
        tree.factorizeInsert(array(0), -10, comparator)

        val list = tree.getValues()
        for (e in list) {
            println(e)
        }
    }
    */
}