@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.algorithms.Graph
import yfiles.algorithms.GraphChecker.isAcyclic
import yfiles.algorithms.GraphChecker.isCyclic
import yfiles.algorithms.Trees.isForest
import yfiles.layout.DefaultLayoutGraph

fun externalExtensions() {
    val graph: Graph = DefaultLayoutGraph()
    // JS: GraphChecker.isCyclic(graph)
    graph.isCyclic()
    // JS: GraphChecker.isAcyclic(graph)
    graph.isAcyclic()
    // JS: Trees.isForest(graph)
    graph.isForest()
}
