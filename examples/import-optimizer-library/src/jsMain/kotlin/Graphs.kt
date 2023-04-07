import yfiles.graph.DefaultGraph
import yfiles.graph.IGraph

fun createGraph(): IGraph =
    DefaultGraph {
        val n1 = createNode()
        val n2 = createNode()

        createEdge(n1, n2)
    }
