import yfiles.algorithms.nodeDpKey
import yfiles.view.GraphComponent

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
