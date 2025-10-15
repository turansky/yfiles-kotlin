@file:Suppress("unused")

import yfiles.collections.iterator
import yfiles.graph.Graph
import yfiles.graph.IGraph
import yfiles.hierarchic.HierarchicalLayout
import yfiles.layout.LayoutGraph

fun enumerableIterator() {
    val graph: IGraph = Graph {
        createNode()
        createNode()
        createNode()

        applyLayout(HierarchicalLayout())
    }

    for (node in graph.nodes) {
        println("Node layout: ${node.layout}")
    }
}

fun cursorIterator() {
    val graph = LayoutGraph().apply {
        createNode()
        createNode()
        createNode()
    }

    HierarchicalLayout()
        .applyLayout(graph)

    for (node in graph.nodes.getCursor()) {
        println("Node index: ${node.index}")
    }
}
