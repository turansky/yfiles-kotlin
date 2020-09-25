import yfiles.view.GraphComponent

fun createComponent(): GraphComponent =
    GraphComponent {
        graph = createGraph()
    }
