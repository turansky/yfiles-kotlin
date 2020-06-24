import yfiles.graph.INode
import yfiles.input.IHitTestable
import yfiles.styles.IArrow
import yfiles.view.IVisualCreator

@JsExport
@ExperimentalJsExport
abstract class AbstractArrow : IArrow {
    override val cropLength = 13.0
    override val length = 42.0
}

@JsExport
@ExperimentalJsExport
abstract class AbstractArrow2 : AbstractArrow() {
    override val cropLength = 14.0
    override val length = 43.0

    fun castTest() {
        val i = js("({})")
        val t1 = i as IHitTestable
        val t2 = i as IVisualCreator
        val t3 = i as INode
        println(t1)
        println(t2)
    }
}

abstract class ZArrow {
    val cropLength = -13.0
    val length = -42.0
}
