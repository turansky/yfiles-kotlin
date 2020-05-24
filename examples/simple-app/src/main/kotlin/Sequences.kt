@file:Suppress("unused")

import yfiles.algorithms.asSequence
import yfiles.collections.asSequence
import yfiles.graph.DefaultGraph
import yfiles.graph.IGraph
import yfiles.graph.applyLayout
import yfiles.hierarchic.HierarchicLayout
import yfiles.layout.DefaultLayoutGraph
import yfiles.layout.LayoutGraph

fun enumerableSequence() {
    val graph: IGraph = DefaultGraph {
        createNode()
        createNode()
        createNode()

        applyLayout(HierarchicLayout())
    }

    graph.nodes.asSequence()
        .forEach { println("Node layout: ${it.layout}") }
}

fun cursorSequence() {
    val graph: LayoutGraph = DefaultLayoutGraph().apply {
        createNode()
        createNode()
        createNode()
    }

    HierarchicLayout()
        .applyLayout(graph)

    graph.getNodeCursor()
        .asSequence()
        .filter { it.index % 2 == 0 }
        .forEach { println("Node index: ${it.index}") }
}
