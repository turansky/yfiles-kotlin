import yfiles.input.IHitTestable
import yfiles.styles.IArrow
import yfiles.view.GraphComponent
import yfiles.view.IVisualCreator

@JsExport
@ExperimentalJsExport
fun createComponent(): GraphComponent =
    GraphComponent {
        graph = createGraph()
    }

// TODO: create Kotlin issue
/*
@JsExport
@ExperimentalJsExport
object Keys {
    val STRING_DATA_KEY by nodeDpKey<String>()

    val INT_DATA_KEY by nodeDpKey<Int>()
}
*/

abstract class AbstractArrow : IArrow, IVisualCreator, IHitTestable
