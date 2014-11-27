package org.jetbrains.format.research


import java.util.HashMap
import java.util.ArrayList
import java.util.Random
import com.intellij.util.containers.HashSet
import java.util.TreeMap
import org.jetbrains.format.Format

/**
 * User: anlun
 */

abstract class Doc {
    public fun div  (d: Doc): Doc = Beside(this, d)
    public fun minus(d: Doc): Doc = Above (this, d)

    override public fun hashCode(): Int = hashCode
    abstract protected val hashCode: Int

//    abstract public fun equals(obj: Any?): Boolean

    protected val hashCodePrime: Int = 31
}

data class Text(
    val text: String
): Doc() {
    override protected val hashCode: Int
    {
        hashCode = text.hashCode()
    }

    override public fun equals(obj: Any?): Boolean {
        if (!(obj is Text)) { return false }
        return text.equals(obj.text)
    }
}

data class Indent(
      val   i: Int
    , val doc: Doc
): Doc() {
    override protected val hashCode: Int
    {
        hashCode = i * hashCodePrime + doc.hashCode()
    }

    override public fun equals(obj: Any?): Boolean {
        if (!(obj is Indent)) { return false }
        if (i != obj.i) { return false }

        return doc.equals(obj.doc)
    }
}

abstract data class DoubleDoc(
      val  left: Doc
    , val right: Doc
    , val     n: Int
): Doc() {
    override protected val hashCode: Int
    {
        hashCode = ((left.hashCode() * hashCodePrime) + right.hashCode) * hashCodePrime + n
    }

    override public fun equals(obj: Any?): Boolean {
        if (!(obj is DoubleDoc)) { return false }

        if (n != obj.n) { return false }
        if (!left .equals(obj.left )) { return false }
        if (!right.equals(obj.right)) { return false }

        return true
    }
}

data class Beside(
       left: Doc
    , right: Doc
): DoubleDoc(left, right, 0)

data class Above(
       left: Doc
    , right: Doc
): DoubleDoc(left, right, 1)

data class Choice(
       left: Doc
    , right: Doc
): DoubleDoc(left, right, 2)

data class NonTerminal(
      val         width: Int
    , val lastLineWidth: Int
//    , val firstLineWidth: Int
) {
    class object {
        fun fromFormat(fmt: Format) = NonTerminal(fmt.totalWidth, fmt.lastLineWidth)
    }

    public fun isSuitable(widthToSuit: Int): Boolean {
        if (width         > widthToSuit || width         < 0) { return false }
        if (lastLineWidth > widthToSuit || lastLineWidth < 0) { return false }
        return true
    }
    public fun shiftLeft(shiftSize: Int): NonTerminal? {
        if (width - shiftSize < 1 || lastLineWidth - shiftSize < 1) { return null }
        return NonTerminal(width - shiftSize, lastLineWidth - shiftSize)
    }
}

abstract class DocVariants {
    abstract public fun get(nt: NonTerminal): Format?
    abstract public fun getFormats(): List<Format>
    abstract public fun getVariants(): Map<NonTerminal, Format>

    abstract protected fun put(nonTerminal: NonTerminal, fmt: Format)

    public fun update(nonTerminal: NonTerminal, fmt: Format): Boolean {
        val oldFmt = get(nonTerminal)
        if (oldFmt == null || oldFmt.height > fmt.height) {
            put(nonTerminal, fmt)
            return true
        }
        return false
    }
}

class DocVariantsBasedOn2DimArray(
    val maxWidth: Int
): DocVariants() {
    private val myVariants: Array<Array<Format?>> =
            Array(
                  maxWidth + 1
                , { _ -> Array<Format?>(maxWidth + 1, { _ -> null }) }
            )

    override public fun get(nt: NonTerminal): Format? {
        try {
            if (nt.width <= maxWidth && nt.lastLineWidth <= maxWidth) {
                return myVariants[nt.width][nt.lastLineWidth]
            }
        } catch (e: ArrayIndexOutOfBoundsException) {
            println("nt.width        : ${nt.width}")
            println("nt.lastLineWidth: ${nt.lastLineWidth}")
            println("maxWidth        : ${maxWidth}")
            throw e
        }
        return null
    }
    override public fun getFormats(): List<Format> {
        val nonNullNumber = nonNullFmtCount()
        val result = ArrayList<Format>(nonNullNumber)
        myVariants map { arr ->
            arr map { (fmt: Format?) ->
                if (fmt != null) {
                    result.add(fmt)
                }
            }
        }
        return result
    }
    override public fun getVariants(): Map<NonTerminal, Format> {
        val result = HashMap<NonTerminal, Format>()
        myVariants map { arr ->
            arr map { (fmt: Format?) ->
                if (fmt != null) {
                    result.put(NonTerminal.fromFormat(fmt), fmt)
                }
            }
        }
        return result
    }

//    override protected fun put(nonTerminal: NonTerminal, fmt: Format) {
    override public fun put(nonTerminal: NonTerminal, fmt: Format) {
        val width = nonTerminal.width
        val lastLineWidth = nonTerminal.lastLineWidth
        if (width <= maxWidth && lastLineWidth <= maxWidth) {
            myVariants[width][lastLineWidth] = fmt
        }
    }

    public fun update(docVariants: DocVariantsBasedOn2DimArray) {
        for (i in 1..maxWidth) {
            for (j in 1..maxWidth) {
                val oldFmt = myVariants[i][j]
                val    fmt = docVariants.myVariants[i][j]
                if (fmt != null && (oldFmt == null || oldFmt.height >= fmt.height)) {
                    myVariants[i][j] = fmt
                }
            }
        }
    }

    private fun nonNullFmtCount(): Int {
        return myVariants.fold(0) { (res: Int, arr: Array<Format?>) ->
            res + arr.fold(0) { (res: Int, fmt: Format?) ->
                if (fmt != null) {
                    res + 1
                } else {
                    res
                }
            }
        }
    }
}

var count: Long = 0
var    allComputedVariants: Long = 0
var usefulComputedVariants: Long = 0

abstract class Processor(
        val width: Int
) {
    protected fun update(doc: Doc, nt: NonTerminal, fmt: Format) {
        val oldVariants = docVariants.get(doc)
        if (oldVariants == null) {
            val newVariants = DocVariantsBasedOn2DimArray(width)
            newVariants.update(nt, fmt)
            docVariants.put(doc, newVariants)
            return
        }
        oldVariants.update(nt, fmt)
    }

    protected fun update(doc: Doc, docVariantsToUpdate: DocVariantsBasedOn2DimArray) {
        val oldVariants = docVariants.get(doc)
        if (oldVariants == null) {
            docVariants.put(doc, docVariantsToUpdate)
            return
        }
        oldVariants.update(docVariantsToUpdate)
    }

    protected fun getTableVariant(doc: Doc, nt: NonTerminal): Format? {
        val docVariantTable = docVariants.get(doc)
        return docVariantTable?.get(nt)
    }

    protected val docVariants: MutableMap<Doc, DocVariantsBasedOn2DimArray> = HashMap()

    protected val alreadyObservedVariants: MutableSet<Pair<Doc, NonTerminal>> = HashSet()
}

class LazyProcessor(
        width: Int
): Processor(width) {
    public fun getVariant(doc: Doc): Format? {
        var bestFmt: Format? = null

        for (resultWidth in width downTo 1) {
            for (resultLastLineWidth in 1..resultWidth) {
                val nt = NonTerminal(resultWidth, resultLastLineWidth)
                val curResult = process(doc, nt)

                bestFmt = betterFmt(curResult, bestFmt)
            }
//            if (bestFmt != null) { return bestFmt }
        }
        return bestFmt
    }

    private fun process(doc: Doc, nt: NonTerminal): Format? {
        if (!nt.isSuitable(width)) { return null }

        val alreadyCalculatedVariant = getTableVariant(doc, nt)
        if (alreadyCalculatedVariant != null) { return alreadyCalculatedVariant }

        if (alreadyObservedVariants.contains(Pair(doc, nt))) { return null }
        alreadyObservedVariants.add(Pair(doc, nt))

        when (doc) {
            is Text -> {
                val textLen = doc.text.length
                if (textLen > width || textLen != nt.width || textLen != nt.lastLineWidth) { return null }

                val fmt = Format.line(doc.text)
                update(doc, nt, fmt)
                return fmt
            }

            is Indent -> {
                val shiftedNT = nt.shiftLeft(doc.i)
                if (shiftedNT == null) { return null }
                val subDocFmt = process(doc.doc, shiftedNT)
                if (subDocFmt == null) { return null }

                val docFmt    = subDocFmt.getIndented(doc.i)
                update(doc, nt, docFmt)
                return docFmt
            }

            is DoubleDoc -> {
                return processDD(doc, nt)
            }
        }
        return null
    }

    private fun processDD(doc: Doc, nt: NonTerminal): Format? {
        when (doc) {
            is Choice -> {
                val  leftFmt = process(doc.left , nt)
                val rightFmt = process(doc.right, nt)
                val  bestFmt = betterFmt(leftFmt, rightFmt)
                if (bestFmt == null) { return null }
                update(doc, nt, bestFmt)
                return  bestFmt
            }

            is Above  -> return processAbove (doc, nt)
            is Beside -> return processBeside(doc, nt)
        }
        return null
    }

    private fun betterFmt(left: Format?, right: Format?): Format? {
        if (left  == null) { return right }
        if (right == null) { return left  }

        if (left.height < right.height) { return left }
        return right
    }

    private fun processBeside(doc: Beside, nt: NonTerminal): Format? {
        var bestFmt: Format? = null

        // 1 - leftWidth == nt.width
        val leftWidth = nt.width
        for (leftLastLineWidth in 1..leftWidth-1) {
            val leftFmt = process(doc.left, NonTerminal(leftWidth, leftLastLineWidth))
            if (leftFmt == null) { continue }

            val rightLastLineWidth = nt.lastLineWidth - leftLastLineWidth
            for (rightWidth in rightLastLineWidth..(leftWidth - leftLastLineWidth)) {
                val rightFmt = process(doc.right, NonTerminal(rightWidth, rightLastLineWidth))
                if (rightFmt == null) { continue }

                bestFmt = betterFmt(leftFmt.addBeside(rightFmt), bestFmt)
            }
        }

        // 2 - leftWidth <  nt.width
        for (leftWidth in 1..nt.width-1) {
            for (leftLastLineWidth in 1..leftWidth) {
                val leftFmt = process(doc.left, NonTerminal(leftWidth, leftLastLineWidth))
                if (leftFmt == null) { continue }

                val rightWidth         = nt.width         - leftLastLineWidth
                val rightLastLineWidth = nt.lastLineWidth - leftLastLineWidth
                val rightFmt = process(doc.right, NonTerminal(rightWidth, rightLastLineWidth))
                if (rightFmt == null) { continue }

                bestFmt = betterFmt(leftFmt.addBeside(rightFmt), bestFmt)
            }
        }

        val bestFmtVal = bestFmt
        if (bestFmtVal == null) { return null }
        update(doc, nt, bestFmtVal)
        return bestFmtVal
    }

    private fun processAbove(doc: Above, nt: NonTerminal): Format? {
        var bestFmt: Format? = null

        // 1 - постороение при rightNT = (nt.width, nt.lastLineWidth)
        val rightFmt = process(doc.right, nt)
        if (rightFmt != null) {
            for (leftWidth in 1..nt.width) {
                for (leftLastLineWidth in 1..leftWidth) {
                    val leftFmt = process(doc.left, NonTerminal(leftWidth, leftLastLineWidth))
                    bestFmt = betterFmt(leftFmt?.addAbove(rightFmt), bestFmt)
                }
            }
        }

        // 2 - если nt.lastLineWidth < nt.width, то построение для перебора от lastLineWidth до width
        if (nt.lastLineWidth < nt.width) {
            val rightLastLineWidth = nt.lastLineWidth
            val leftWidth          = nt.width
            for (rightWidth in rightLastLineWidth..nt.width-1) {
                val rightFmt = process(doc.right, NonTerminal(rightWidth, rightLastLineWidth))
                if (rightFmt == null) { continue }
                for (leftLastLineWidth in 1..leftWidth) {
                    val leftFmt = process(doc.left, NonTerminal(leftWidth, leftLastLineWidth))
                    bestFmt = betterFmt(leftFmt?.addAbove(rightFmt), bestFmt)
                }
            }
        }

        val bestFmtVal = bestFmt
        if (bestFmtVal == null) { return null }
        update(doc, nt, bestFmtVal)
        return bestFmtVal
    }
}

class StrictProcessor(
        width: Int
): Processor(width) {
    public fun getVariant(doc: Doc): Format? {
        process(doc)
        val variants = docVariants.get(doc)
        if (variants == null) { return null }
        val sortedFmts = variants.getFormats() sortBy { fmt -> fmt.height }
        if (sortedFmts.size() <= 0) { return null }
        return sortedFmts.get(0)
    }

    private fun process(doc: Doc) {
        val variants = docVariants.get(doc)
        if (variants != null) { return }
        count++
        when (doc) {
            is Text -> {
                val fmt = Format.line(doc.text)
                val nt  = NonTerminal.fromFormat(fmt)
                if (!nt.isSuitable(width)) { return }
                update(doc, nt, fmt) //TODO: можно ускорить
            }

            is Indent -> {
                val subDoc = doc.doc
                process(subDoc)
                val subDocVariants = docVariants.get(subDoc)
                if (subDocVariants == null) { return }
                for (fmt in subDocVariants.getFormats()) {
                    val newFmt = fmt.getIndented(doc.i)
                    val newNT  = NonTerminal.fromFormat(newFmt)
                    if (!newNT.isSuitable(width)) { continue }
                    update(doc, newNT, newFmt) //TODO: можно ускорить
                }
            }

            is DoubleDoc -> {
                processDD(doc)
            }
        }

        allComputedVariants += docVariants.get(doc)?.getFormats()?.size() ?: 0
    }

    private fun processDD(doc: DoubleDoc) {
        //        count++
        val  leftDoc = doc.left
        process(leftDoc)
        val rightDoc = doc.right
        process(rightDoc)

        val  leftDocVariants = docVariants.get(leftDoc)
        val rightDocVariants = docVariants.get(rightDoc)

        fun processContact(f: (Format, Format) -> Format) {
            if (leftDocVariants == null || rightDocVariants == null) { return }
            val  leftFmts =  leftDocVariants.getFormats()
            val rightFmts = rightDocVariants.getFormats()

            val newVariants = DocVariantsBasedOn2DimArray(width)
            docVariants.put(doc, newVariants)

            for (leftFmt in leftFmts) {
                for (rightFmt in rightFmts) {
                    val newFmt = f(leftFmt, rightFmt)
                    val newNT  = NonTerminal.fromFormat(newFmt)
                    if (!newNT.isSuitable(width)) { continue }

                    val oldFmt = newVariants.get(newNT)
                    if (oldFmt == null || oldFmt.height > newFmt.height) {
                        newVariants.put(newNT, newFmt)
                    }
                }
            }
        }

        when (doc) {
            is Beside -> processContact { (f1, f2) -> f1 / f2 }
            is Above  -> processContact { (f1, f2) -> f1 - f2 }

            is Choice -> {
                if (leftDocVariants != null) {
                    update(doc, leftDocVariants)
                }
                if (rightDocVariants != null) {
                    update(doc, rightDocVariants)
                }
            }
        }
    }
}

abstract class STree
class SNode(
      val  left: STree
    , val right: STree
): STree()
class Leaf: STree()

fun genTree(n: Int): STree {
    if (n == 0) { return Leaf() }
    val subTree = genTree(n - 1)
    return SNode(subTree, subTree)
}

val randomGen = Random()
fun treeToDoc(tree: STree): Doc {
    when (tree) {
        is Leaf  -> {
            val alpha = "abcdefghijklmnopqrstuvwxyz"
            val alphaSize = alpha.length
            val randomPos = randomGen.nextInt(alphaSize)

            return Text("${alpha[randomPos]}")
        }
        is SNode -> {
            val s = Text("-")
            val lD = treeToDoc(tree.left)
            val rD = treeToDoc(tree.right)
            return Choice(s - lD - rD, s / lD / rD)
        }

        else -> throw UnknownError()
    }
}

fun main(args: Array<String>) {
//    val width = 10
    val width = 20
//    val i = 5

    for (i in 5..8) {
        val startTime = System.nanoTime()

        count = 0
        allComputedVariants = 0
        println(i)
        println("-----------------------------------------------")
        val tree = genTree(i)
        val processor = StrictProcessor(width)
//        val processor = LazyProcessor(width)
        val doc = treeToDoc(tree)
        val format = processor.getVariant(doc)
        if (format == null) { println("Error!"); return }
        println(format.toText(0, ""))

        val endTime = System.nanoTime()

        val duration = endTime - startTime
        println("Duration: ${duration.toDouble() / Math.pow(10.0, 9.0)}")
//        println("Count: $count")
        println("All computed variants: $allComputedVariants")
        println()
    }
}