import web.html.HTMLDivElement
import yfiles.graph.Graph
import yfiles.graph.invoke
import yfiles.hierarchic.HierarchicalLayout
import yfiles.input.GraphViewerInputMode
import yfiles.layout.LayoutOrientation
import yfiles.view.GraphComponent

fun create(): HTMLDivElement =
    GraphComponent().run {
        inputMode = GraphViewerInputMode()

        htmlElement.style.apply {
            width = "100%"
            height = "100%"
            backgroundColor = "#CCCCCC"
        }

        val layout = HierarchicalLayout {
            layoutOrientation = LayoutOrientation.LEFT_TO_RIGHT
            automaticEdgeGrouping = true
            gridSpacing = 20.0
        }

        graph = Graph {
            val node1 = createNode()
            val node2 = createNode()
            createEdge(node1, node2)

            applyLayout(layout)
        }

        graph.decorator {
            nodes {
                selectionRenderer.hide { true }
                focusRenderer.hide { true }
                highlightRenderer.hide { true }
            }
        }

        fitGraphBounds()

        htmlElement.unsafeCast<HTMLDivElement>()
    }
