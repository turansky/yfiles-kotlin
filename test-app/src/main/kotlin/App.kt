import org.w3c.dom.HTMLDivElement
import yfiles.graph.DefaultGraph
import yfiles.graph.applyLayout
import yfiles.graph.decorator
import yfiles.graph.invoke
import yfiles.hierarchic.HierarchicLayout
import yfiles.input.GraphViewerInputMode
import yfiles.view.GraphComponent

fun create(): HTMLDivElement =
    GraphComponent().run {
        inputMode = GraphViewerInputMode()

        div.style.apply {
            width = "100%"
            height = "100%"
            backgroundColor = "#CCCCCC"
        }

        graph = DefaultGraph {
            val node1 = createNode()
            val node2 = createNode()
            createEdge(node1, node2)

            applyLayout(HierarchicLayout())
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