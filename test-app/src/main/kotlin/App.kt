import org.w3c.dom.HTMLDivElement
import yfiles.graph.DefaultGraph
import yfiles.input.GraphViewerInputMode
import yfiles.view.GraphComponent

fun create(): HTMLDivElement {
    val component = GraphComponent()
    component.inputMode = GraphViewerInputMode()

    val container = component.div
    container.style.width = "100%"
    container.style.height = "100%"
    container.style.backgroundColor = "#CCCCCC"

    val graph = DefaultGraph()
    val node1 = graph.createNode()
    val node2 = graph.createNode()
    graph.createEdge(node1, node2)

    // TODO: support extensions
    // graph.applyLayout(HierarchicLayout())

    component.graph = graph
    component.fitGraphBounds()

    return container
}