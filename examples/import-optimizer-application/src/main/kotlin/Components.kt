import yfiles.algorithms.nodeDpKey
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

@JsExport
@ExperimentalJsExport
val STRING_DATA_KEY by nodeDpKey<String>()

@JsExport
@ExperimentalJsExport
val INT_DATA_KEY by nodeDpKey<Int>()

abstract class AbstractArrow : IArrow, IVisualCreator, IHitTestable
