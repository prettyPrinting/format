package org.jetbrains.format

import java.util.ArrayList
import java.util.NoSuchElementException
import java.util.HashMap
import org.jetbrains.format.FormatSet.FormatSetType
import java.util.TreeMap
import java.util.HashSet
import java.util.LinkedList
import org.jetbrains.format.toFormat
import org.jetbrains.format.Format

/**
 * User: anlun
 */
abstract class FormatSet (
    val width: Int
): Iterable<Format> {
    public enum class FormatSetType {
        D1; D2; D3; D3AF; SteppedD3AF; List
    }

    class object {
        public var usingNewInsertToText: Boolean = true
        public var defaultFormatSetType: FormatSetType = FormatSetType.D3AF
        public var stepInMap: Int = 1

        public fun setDefaultSettings() {
            usingNewInsertToText = true
            defaultFormatSetType = FormatSetType.D3AF
            stepInMap = 0
        }

        public fun empty(width: Int): FormatSet =
            when(defaultFormatSetType) {
                FormatSetType.D1   -> FormatMap1D   (width)
                FormatSetType.D2   -> FormatMap2D_LL(width)
                FormatSetType.D3   -> FormatMap3D   (width)
                FormatSetType.D3AF -> FormatMap3D_AF(width)
                FormatSetType.List -> FormatList    (width)
                FormatSetType.SteppedD3AF -> SteppedFormatMap(stepInMap, width)
                else -> FormatMap3D_AF(width)
            }

        public fun initial(width: Int, f: Format = Format.empty): FormatSet {
            val s = empty(width)
            s.add(f)
            return s
        }
        public fun initial(width: Int, s: String): FormatSet = initial(width, s.toFormat())
    }

    public fun add(f: Format) { if (defaultFilter(f)) { isFactorized = false; uncondAdd(f) } }
    abstract protected fun uncondAdd(f: Format)
    public fun addAll(fl: Iterable<Format>) { fl.forEach { f -> add(f) } }

    protected var isFactorized: Boolean = false

    open protected fun factorize() { isFactorized = true }

    override public fun iterator(): Iterator<Format> {
        if (!isFactorized) { factorize() }
        return getIterator()
    }
    abstract protected fun getIterator(): Iterator<Format>

    public fun transform(func: (Format) -> Format): FormatSet {
        val s = sameEmptySet()
        forEach { v -> s.add(func(v)) }
        return s
    }

    abstract protected fun sameEmptySet(): FormatSet
    public fun crossTransform(secondSet: FormatSet, func: (Format, Format) -> Format): FormatSet {
        val s = sameEmptySet()
        forEach { vl ->
            secondSet.forEach { vr ->
                s.add(func(vl, vr))
            }
        }
        if (size() == 1 || secondSet.size() == 1) { s.isFactorized = true }
        return s
    }

    public fun addAbove    (f: Format): FormatSet = addAbove    (initial(width, f))
    public fun addBeside   (f: Format): FormatSet = addBeside   (initial(width, f))
    public fun addFillStyle(f: Format): FormatSet = addFillStyle(initial(width, f))
    public fun addFillStyle(f: Format, shiftConstant: Int): FormatSet =
            addFillStyle(initial(width, f), shiftConstant)
    public fun addAboveWithEmptyLine(f: Format): FormatSet = addAboveWithEmptyLine(initial(width, f))

    open public fun addAbove (f: FormatSet): FormatSet = crossTransform(f) { (vl, vr) -> vl - vr }
    open public fun addBeside(f: FormatSet): FormatSet = crossTransform(f) { (vl, vr) -> vl / vr }
    public fun addFillStyle(f: FormatSet): FormatSet = crossTransform(f) { (vl, vr) -> vl + vr }
    public fun addFillStyle(f: FormatSet, shiftConstant: Int): FormatSet =
            crossTransform(f) { (vl, vr) -> vl.addFillStyle(vr, shiftConstant) }
    public fun addAboveWithEmptyLine(f: FormatSet): FormatSet = crossTransform(f) { (vl, vr) -> vl % vr }

    /* Shortcuts */
    public fun minus(f: FormatSet): FormatSet = addAbove(f)
    public fun minus(f: Format): FormatSet = addAbove(f)

    public fun mod(f: FormatSet): FormatSet = addAboveWithEmptyLine(f)
    public fun mod(f: Format): FormatSet = addAboveWithEmptyLine(f)

    public fun div(f: FormatSet): FormatSet = addBeside(f)
    public fun div(f: Format): FormatSet = addBeside(f)

    public fun plus(f: FormatSet): FormatSet = addFillStyle(f)
    public fun plus(f: Format): FormatSet = addFillStyle(f)

    abstract public fun isEmpty   (): Boolean
    abstract public fun isNotEmpty(): Boolean

    // Chooses the best one from set
    abstract public fun head(resultWidth: Int = width): Format?

    public fun headSingleton(): FormatSet {
        val h = head()
        if (h == null) { return empty(width)
        }
        return FormatSet.initial(width, h)
    }

    abstract public fun filter(predicate: (Format) -> Boolean): FormatSet
    abstract public fun map   (func     : (Format) -> Format): FormatSet

    abstract public fun size(): Int

    protected val defaultFilter: (Format) -> Boolean = { f -> f.totalWidth <= width }
    protected fun betterFormat(l: Format?, r: Format?): Format? {
        if (r == null) { return l }
        if (l == null || l.height > r.height) { return r }
        return l
    }
}

fun Format.minus(f: FormatSet): FormatSet = FormatSet.initial(f.width, this) - f
fun Format.mod  (f: FormatSet): FormatSet = FormatSet.initial(f.width, this) % f
fun Format.div  (f: FormatSet): FormatSet = FormatSet.initial(f.width, this) / f
fun Format.plus (f: FormatSet): FormatSet = FormatSet.initial(f.width, this) + f

class FormatList(
  width: Int
,  list: List<Format> = ArrayList<Format>()
): FormatSet(width) {
    private val myList: MutableList<Format> = ArrayList(list.filter(defaultFilter))

    override protected fun getIterator() = myList.iterator()
    override protected fun uncondAdd(f: Format) { myList.add(f) }

    override public fun isEmpty   (): Boolean = myList.isEmpty()
    override public fun isNotEmpty(): Boolean = myList.isNotEmpty()

    override public fun head(resultWidth: Int): Format? =
            myList filter { f -> f.totalWidth <= resultWidth } minBy { f -> f.height }

    override public fun filter(predicate: (Format) -> Boolean): FormatList =
            FormatList(width, myList.filter(predicate))
    override public fun map   (func     : (Format) -> Format): FormatList =
            FormatList(width, myList.map(func).filter(defaultFilter))

    override public fun size(): Int = myList.size()

    override protected fun sameEmptySet(): FormatSet = FormatList(width)
}

class FormatLine(
  width: Int
, private val array: Array<Format?> = Array<Format?>(width + 1, {_ -> null})
): FormatSet(width) {
    private var count: Int

    {
        count = 0
        for (i in array.indices) {
            val f = array[i]; if (f == null) { continue }
            if (f.totalWidth > width) array[i] = null
            else                      count++
        }
    }

    private inner class MyIterator: Iterator<Format> {
        private var nextExists: Boolean = true
        private var currentPlace: Int = -1

        {
            updateCurrentPlace()
        }

        private fun updateCurrentPlace() {
            do {
                currentPlace++
                if (currentPlace > array.lastIndex) {
                    nextExists = false
                    return
                }
            } while (array[currentPlace] == null)
        }

        override public fun next(): Format {
            if (currentPlace > array.lastIndex) {
                throw NoSuchElementException()
            }
            val f = array[currentPlace]
            if (f == null) {
                throw NoSuchElementException()
            }
            updateCurrentPlace()
            return f
        }
        override public fun hasNext(): Boolean = nextExists
    }

    override protected fun getIterator(): Iterator<Format> = MyIterator()

    override protected fun uncondAdd(f: Format) {
        val place    = f.totalWidth
        val oldValue = array[place]
        if (oldValue == null) { count++ }
        array[place] = betterFormat(oldValue, f)
    }

    override public fun isEmpty   (): Boolean = count == 0
    override public fun isNotEmpty(): Boolean = count != 0

    override public fun head(resultWidth: Int): Format? = array.take(resultWidth + 1).fold(null) { (best: Format?, f) ->
        betterFormat(best, f)
    }

    override public fun filter(predicate: (Format) -> Boolean): FormatLine {
        val newArray = Array<Format?>(width) { i ->
            val f = array[i]
            if (f == null || !predicate(f)) {
                null
            } else {
                f
            }
        }
        return FormatLine(width, newArray)
    }

    override public fun map(func: (Format) -> Format): FormatLine {
        val newArray = Array<Format?>(width) { i ->
            val f = array[i]
            val nF = if (f != null) {
                        func(f)
                     } else {
                        null
                     }
            if (nF != null && defaultFilter(nF)) {
                nF
            } else {
                null
            }
        }
        return FormatLine(width, newArray)
    }

    override public fun size(): Int = count

    override protected fun sameEmptySet(): FormatSet = FormatLine(width)
}

abstract class FormatMap<in Key>(
  width: Int
, map: Map<Key, Format> = HashMap<Key, Format>()
): FormatSet(width) {
    protected val myMap: HashMap<Key, Format> = HashMap()

    private inner class MyIterator(
            val it: Iterator<Map.Entry<Key, Format>>
    ): Iterator<Format> {
        override public fun next(): Format = it.next().value
        override public fun hasNext(): Boolean = it.hasNext()
    }

    {
        for (e in map) {
            if (defaultFilter(e.value)) { myMap.put(e.key, e.value) }
        }
    }

    override protected fun getIterator(): Iterator<Format> = MyIterator(myMap.iterator())

    abstract public fun keyFromFormat(f: Format): Key
    abstract public fun createFormatMap(width: Int, map: Map<Key, Format>): FormatMap<Key>

    override protected fun uncondAdd(f: Format) {
        val key = keyFromFormat(f)
        val oldValue = myMap.get(key)
        myMap.put(key, betterFormat(oldValue, f) ?: f)
    }

    override public fun isEmpty   (): Boolean =  myMap.isEmpty()
    override public fun isNotEmpty(): Boolean = !myMap.isEmpty()

    override public fun head(resultWidth: Int): Format? =
            myMap.values() filter { f -> f.totalWidth <= resultWidth } minBy { f -> f.height }

    override public fun filter(predicate: (Format) -> Boolean): FormatMap<Key> {
        val result = HashMap<Key, Format>()
        for (e in myMap) {
            if (predicate(e.value)) {
                result.put(e.key, e.value)
            }
        }
        return createFormatMap(width, result)
    }

    override public fun map(func: (Format) -> Format): FormatMap<Key> {
        val result = HashMap<Key, Format>()
        for (e in myMap) {
            val nF = func(e.value)
            if (defaultFilter(nF)) {
                result.put(keyFromFormat(nF), nF)
            }
        }
        return createFormatMap(width, result)
    }

    override public fun size(): Int = myMap.size()
}

abstract class Frame<F>() {
    abstract public fun isLessThan(fr: F): Boolean
}

data class Frame1D(
  val width: Int
): Frame<Frame1D>() {
    override public fun isLessThan(fr: Frame1D): Boolean = width <= fr.width
}

class FormatMap1D(
  width: Int
,   map: Map<Frame1D, Format> = HashMap<Frame1D, Format>()
): FormatMap<Frame1D>(width, map)/*, AdditionFactorization<Frame1D>*/ {
    override public fun keyFromFormat(f: Format): Frame1D = Frame1D(f.totalWidth)
    override public fun createFormatMap(width: Int, map: Map<Frame1D, Format>): FormatMap1D = FormatMap1D(width, map)

    override protected fun sameEmptySet(): FormatSet = FormatMap1D(width)
}

data class Frame2D_LL(
  val width: Int
, val lastLineWidth: Int
): Frame<Frame2D_LL>() {
    override public fun isLessThan(fr: Frame2D_LL): Boolean = width <= fr.width && lastLineWidth <= fr.lastLineWidth
}

class FormatMap2D_LL(
  width: Int
,   map: Map<Frame2D_LL, Format> = HashMap<Frame2D_LL, Format>()
): FormatMap<Frame2D_LL>(width, map), AdditionFactorization<Frame2D_LL> {
    override public fun keyFromFormat(f: Format): Frame2D_LL = Frame2D_LL(f.totalWidth, f.lastLineWidth)
    override public fun createFormatMap(width: Int, map: Map<Frame2D_LL, Format>): FormatMap2D_LL = FormatMap2D_LL(width, map)

    override protected fun sameEmptySet(): FormatSet = FormatMap2D_LL(width)
}

data class Frame2D_FL(
  val width: Int
, val firstLineWidth: Int
): Frame<Frame2D_FL>() {
    override public fun isLessThan(fr: Frame2D_FL): Boolean = width <= fr.width && firstLineWidth <= fr.firstLineWidth
}

class FormatMap2D_FL(
  width: Int
,   map: Map<Frame2D_FL, Format> = HashMap<Frame2D_FL, Format>()
): FormatMap<Frame2D_FL>(width, map)/*, AdditionFactorization<Frame2D_FL>*/ {
    override public fun keyFromFormat(f: Format): Frame2D_FL = Frame2D_FL(f.totalWidth, f.firstLineWidth)
    override public fun createFormatMap(width: Int, map: Map<Frame2D_FL, Format>): FormatMap2D_FL = FormatMap2D_FL(width, map)

    override protected fun sameEmptySet(): FormatSet = FormatMap2D_FL(width)
}

data class Frame3D(
  val firstLineWidth: Int
, val    middleWidth: Int
, val  lastLineWidth: Int
): Frame<Frame3D>() {
    override public fun isLessThan(fr: Frame3D): Boolean =
          firstLineWidth <= fr.firstLineWidth
        &&   middleWidth <= fr.   middleWidth
        && lastLineWidth <= fr. lastLineWidth
}

open class FormatMap3D(
  width: Int
,   map: Map<Frame3D, Format> = HashMap<Frame3D, Format>()
): FormatMap<Frame3D>(width, map) {
    class object {
        public fun factorize_1(s: FormatSet, firstLineFact: Boolean): FormatSet {
            //if (s.size() < s.width * s.width) { return s }
            val res = if (firstLineFact) { FormatMap2D_FL(s.width) } else { FormatMap2D_LL(s.width) }
            res.addAll(s)
            return res
        }
    }

    override public fun keyFromFormat(f: Format): Frame3D = Frame3D(f.firstLineWidth, f.middleWidth, f.lastLineWidth)
    override public fun createFormatMap(width: Int, map: Map<Frame3D, Format>): FormatMap3D = FormatMap3D(width, map)

    override protected fun sameEmptySet(): FormatSet = FormatMap3D(width)
    /*
    override public fun addAbove(f: FormatSet): FormatSet {
        val left  = factorize_1(this, true)
        val right = factorize_1(f   , false)

        val result = FormatMap3D(width)
        left forEach { l ->
            right forEach { r ->
                result.add(l - r)
            }
        }
        return result
    }

    override public fun addBeside(f: FormatSet): FormatSet {
        val right = factorize_1(f, false)

        val result = FormatMap3D(width)
        this forEach { l ->
            right forEach { r ->
                result.add(l / r)
            }
        }
        return result
    }
    */
}

var mapSizeMax: Int = 0

trait AdditionFactorization<T: Frame<T>>: FormatMap<T> {
    /*
    override protected fun uncondAdd(f: Format) {
        val fKey = keyFromFormat(f)
        val keysToDelete = ArrayList<T>()

//        mapSizeMax = Math.max(mapSizeMax, myMap.size)
//        println(mapSizeMax)

        val oldValue = myMap.get(fKey)
        val betterFormatValue = betterFormat(oldValue, f) ?: f
        var isThereBetterValue = false

        for (key in myMap.keySet()) {
            val mapValue = myMap.get(key)
            if (mapValue == null /*|| key.equals(fKey)*/) { continue }
            if (betterFormatValue.height <= mapValue.height && fKey.isLessThan(key)) {
                keysToDelete.add(key)
            }

            if (betterFormatValue.height > mapValue.height && key.isLessThan(fKey)) {
                isThereBetterValue = true
            }
        }

        keysToDelete forEach {k -> myMap.remove(k)}
        if (!isThereBetterValue) { myMap.put(fKey, betterFormatValue) }
    }
    */
    override protected fun factorize() {
        val keysToObserve = HashSet(myMap.keySet())
        val keysToRemove = HashSet<T>()
        for (key in myMap.keySet()) {
            if (!keysToObserve.contains(key)) { continue }
            var isThereBetter = false
            val curFormat = myMap.get(key); if (curFormat == null) { continue }

            val keysToRemoveFromObserveSet = LinkedList<T>()
            for (anotherKey in keysToObserve) {
                val anotherFormat = myMap.get(anotherKey)
                if (anotherFormat == null || anotherKey.equals(key)) { continue }

                if (anotherKey.isLessThan(key) &&
                    curFormat.height >= anotherFormat.height) { isThereBetter = true; break }

                if (key.isLessThan(anotherKey) &&
                    curFormat.height <= anotherFormat.height) { keysToRemoveFromObserveSet.add(anotherKey) }
            }
            keysToObserve.removeAll(keysToRemoveFromObserveSet)
            keysToRemove.addAll(keysToRemoveFromObserveSet)

            if (isThereBetter) { keysToObserve.remove(key); keysToRemove.add(key) }
        }
        keysToRemove forEach { k -> myMap.remove(k) }

//        mapSizeMax = Math.max(mapSizeMax, myMap.size)
//        println(mapSizeMax)

        isFactorized = true
    }
}

class FormatMap3D_AF(
  width: Int
, map: Map<Frame3D, Format> = HashMap<Frame3D, Format>()
): FormatMap3D(width, map)
 , AdditionFactorization<Frame3D> {
    override public fun createFormatMap(width: Int, map: Map<Frame3D, Format>): FormatMap3D_AF = FormatMap3D_AF(width, map)
    override protected fun sameEmptySet(): FormatSet = FormatMap3D_AF(width)
}

data class SteppedFrame(
  val              step: Int
, val firstLineWidthDiv: Int
, val    middleWidthDiv: Int
, val  lastLineWidthDiv: Int
): Frame<SteppedFrame>() {
    override public fun isLessThan(fr: SteppedFrame): Boolean =
          firstLineWidthDiv <= fr.firstLineWidthDiv
        &&   middleWidthDiv <= fr.   middleWidthDiv
        && lastLineWidthDiv <= fr. lastLineWidthDiv
}

class SteppedFormatMap(
  val  step: Int
,     width: Int
,       map: Map<SteppedFrame, Format> = HashMap<SteppedFrame, Format>()
): FormatMap<SteppedFrame>(width, map)
 , AdditionFactorization<SteppedFrame> {
    override public fun keyFromFormat(f: Format): SteppedFrame =
            SteppedFrame(step, f.firstLineWidth / step, f.middleWidth / step, f.lastLineWidth / step)
    override public fun createFormatMap(width: Int, map: Map<SteppedFrame, Format>): SteppedFormatMap =
            SteppedFormatMap(step, width, map)

    override protected fun sameEmptySet(): FormatSet = SteppedFormatMap(step, width)
}