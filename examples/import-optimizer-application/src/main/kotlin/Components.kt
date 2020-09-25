import yfiles.view.GraphComponent

@JsExport
@ExperimentalJsExport
fun createComponent(): GraphComponent =
    GraphComponent {
        graph = createGraph()
    }
