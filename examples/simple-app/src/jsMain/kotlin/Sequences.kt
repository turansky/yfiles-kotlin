@file:Suppress("unused")

import yfiles.collections.asSequence
import yfiles.graph.Graph
import yfiles.graph.IGraph
import yfiles.hierarchic.HierarchicalLayout
import yfiles.layout.LayoutGraph

fun enumerableSequence() {
    val graph: IGraph = Graph {
        createNode()
        createNode()
        createNode()

        applyLayout(HierarchicalLayout())
    }

    graph.nodes.asSequence()
        .forEach { println("Node layout: ${it.layout}") }
}

fun cursorSequence() {
    val graph: LayoutGraph = LayoutGraph().apply {
        createNode()
        createNode()
        createNode()
    }

    HierarchicalLayout()
        .applyLayout(graph)

    graph.nodes.getCursor()
        .asSequence()
        .filter { it.index % 2 == 0 }
        .forEach { println("Node index: ${it.index}") }
}
