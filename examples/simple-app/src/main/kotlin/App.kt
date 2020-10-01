import org.w3c.dom.HTMLDivElement
import yfiles.graph.DefaultGraph
import yfiles.graph.invoke
import yfiles.hierarchic.HierarchicLayout
import yfiles.input.GraphViewerInputMode
import yfiles.layout.LayoutOrientation
import yfiles.view.GraphComponent

fun create(): HTMLDivElement =
    GraphComponent().run {
        inputMode = GraphViewerInputMode()

        div.style.apply {
            width = "100%"
            height = "100%"
            backgroundColor = "#CCCCCC"
        }

        val layout = HierarchicLayout {
            layoutOrientation = LayoutOrientation.LEFT_TO_RIGHT
            automaticEdgeGrouping = true
            gridSpacing = 20.0
        }

        graph = DefaultGraph {
            val node1 = createNode()
            val node2 = createNode()
            createEdge(node1, node2)

            applyLayout(layout)
        }

        graph.decorator {
            nodeDecorator {
                selectionDecorator.hideImplementation()
                focusIndicatorDecorator.hideImplementation()
                highlightDecorator.hideImplementation()
            }
        }

        fitGraphBounds()

        div
    }
