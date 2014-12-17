package org.jetbrains.format.research

/**
 * User: anlun
 */
open public class KDTree<T>(
  val     k: Int
, val start: Array<Int>
, val   end: Array<Int>
) {
    private val subSpaceArray: Array<KDTree<T>?> = Array(power(2, k)) { _ -> null }
    private fun power(x: Int, a: Int): Int =  Math.pow(x.toDouble(), a.toDouble()).toInt()
    private fun middle(x: Int, y: Int) : Int = (x + y) / 2

    private fun subSpace(v: Array<Int>): KDTree<T>? = subSpaceArray[subSpacePos(v)]
    private fun subSpacePos(v: Array<Int>): Int {
        var pos   = 0
        var index = 1
        for (i in k-1 downTo 0) {
            if (v[i] >= middle(start[i], end[i])) { pos += index }
            index *= 2
        }
        return pos
    }

    private fun subSpaceByPoint(v: Array<Int>, value: T, valueComparator: (T, T) -> Boolean): KDTree<T> {
        val pos = subSpacePos(v)
        val oldSpace = subSpaceArray[pos]
        if (oldSpace != null) {
            if (oldSpace is KDLeaf && valueComparator(value, oldSpace.value)) {
                oldSpace.value = value
            }
            return oldSpace
        }

        val mid = Array(k) { i -> middle(start[i], end[i]) }
        val newStart = Array(k) { i -> if (v[i] < mid[i]) start[i] else mid[i] }
        val newEnd   = Array(k) { i -> if (v[i] < mid[i])   mid[i] else end[i] }

        var isNewLeaf = true
        for (i in 0..k-1) {
            if (newEnd[i] - newStart[i] > 1) { isNewLeaf = false }
        }
        val newSpace = if (isNewLeaf) KDLeaf(k, newStart, newEnd, value)
                       else           KDTree<T>(k, newStart, newEnd)
        subSpaceArray[pos] = newSpace
        return newSpace
    }

    public fun insert(v: Array<Int>, newValue: T, valueComparator: (T, T) -> Boolean) {
        if (!isInSpace(v)) { return }

        if (this is KDLeaf) {
            if (valueComparator(newValue, this.value)) this.value = newValue
            return
        }
        val space = subSpaceByPoint(v, newValue, valueComparator)
        if (!space.isLeaf()) { space.insert(v, newValue, valueComparator) }
    }

    public fun remove(v: Array<Int>): KDTree<T>? {
        if (!isInSpace(v))  { return this }
        if (this is KDLeaf) { return null }
        val pos = subSpacePos(v)
        subSpaceArray[pos] = subSpaceArray[pos]?.remove(v)
        if (subSpaceArray.all { s -> s == null }) { return null }
        return this
    }

    public fun getValues(): List<T> = subSpaceArray.flatMap { s ->
        if (s is KDLeaf) listOf(s.value)
        else             s?.getValues() ?: listOf()
    }

    public fun factorizeInsert(v: Array<Int>, newValue: T, valueComparator: (T, T) -> Boolean): KDTree<T>? {
        val curTree = this
        if (curTree is KDLeaf) {
            if (valueComparator(newValue, curTree.value)) {
                if (isInSpace(v)) {
                    curTree.value = newValue
                } else { return null }
            }
            return this
        }

        insert(v, newValue, valueComparator)

        var posList = if (v[0] < middle(start[0], end[0]))
                            listOf(0, 1)
                       else listOf(1)
        for (i in 1..k-1) {
            val mid = middle(start[i], end[i])
            if (v[i] < mid) posList = posList.flatMap { x -> listOf(2 * x, 2 * x + 1) }
            else            posList = posList.flatMap { x -> listOf(2 * x + 1) }
        }
        for (pos in posList) {
            subSpaceArray[pos] = subSpaceArray[pos]?.factorizeInsert(v, newValue, valueComparator)
        }

        if (subSpaceArray.all { s -> s == null }) { return null }
        return this
    }

    public fun isInSpace(v: Array<Int>): Boolean {
        for (i in 0..k-1) {
            if (v[i] < start[i] || v[i] >= end[i]) { return false }
        }
        return true
    }
    public fun isLeaf(): Boolean = this is KDLeaf
}

public class KDLeaf<T>(
          k: Int
,     start: Array<Int>
,       end: Array<Int>
, var value: T
): KDTree<T>(k, start, end)