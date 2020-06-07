@file:Suppress("unused")

import yfiles.algorithms.iterator
import yfiles.collections.iterator
import yfiles.graph.DefaultGraph
import yfiles.graph.IGraph
import yfiles.graph.applyLayout
import yfiles.hierarchic.HierarchicLayout
import yfiles.layout.DefaultLayoutGraph
import yfiles.layout.LayoutGraph

fun enumerableIterator() {
    val graph: IGraph = DefaultGraph {
        createNode()
        createNode()
        createNode()

        applyLayout(HierarchicLayout())
    }

    for (node in graph.nodes) {
        println("Node layout: ${node.layout}")
    }
}

fun cursorIterator() {
    val graph: LayoutGraph = DefaultLayoutGraph {
        createNode()
        createNode()
        createNode()
    }

    HierarchicLayout()
        .applyLayout(graph)

    for (node in graph.getNodeCursor()) {
        println("Node index: ${node.index}")
    }
}
