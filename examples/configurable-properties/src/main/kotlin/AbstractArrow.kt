import yfiles.graph.INode
import yfiles.input.IHitTestable
import yfiles.styles.IArrow
import yfiles.view.IVisualCreator

@JsExport
@ExperimentalJsExport
fun arrow() {
    println(AbstractArrow::class.js)
    println(AbstractArrow2::class.js)
    println(AbstractArrow3::class.js)
}

abstract class AbstractArrow : IArrow {
    override val cropLength = 13.0
    override val length = 42.0
}

abstract class AbstractArrow2 {
    val cropLength = 14.0
    val length = 43.0

    fun castTest() {
        val i = js("({})")

        val t1 = i as IHitTestable
        val t2 = i as IVisualCreator
        val t3 = i as INode

        val is1 = i is IHitTestable
        val is2 = i is IVisualCreator
        val is3 = i is INode

        println(t1)
        println(t2)
    }
}

abstract class AbstractArrow3 : AbstractArrow() {
    override val cropLength = 15.0
    override val length = 44.0
}

abstract class ZArrow {
    val cropLength = -13.0
    val length = -42.0
}
