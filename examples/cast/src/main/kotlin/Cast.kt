import yfiles.graph.INode
import yfiles.input.IHitTestable
import yfiles.view.IVisualCreator

@JsExport
@ExperimentalJsExport
class CastPolygon {
    private val c_i = js("({})")

    private val c_t1 = c_i as IHitTestable
    private val c_t2 = c_i as IVisualCreator
    private val c_t3 = c_i as INode

    private val c_is1 = c_i is IHitTestable
    private val c_is2 = c_i is IVisualCreator
    private val c_is3 = c_i is INode

    fun castTest() {
        val i = js("({})")

        val l_t1 = i as IHitTestable
        val l_t2 = i as IVisualCreator
        val l_t3 = i as INode

        val l_is1 = i is IHitTestable
        val l_is2 = i is IVisualCreator
        val l_is3 = i is INode

        println(l_t1)
        println(l_t2)
    }
}

private val f_i = js("({})")

private val f_t1 = f_i as IHitTestable
private val f_t2 = f_i as IVisualCreator
private val f_t3 = f_i as INode

private val f_is1 = f_i is IHitTestable
private val f_is2 = f_i is IVisualCreator
private val f_is3 = f_i is INode
