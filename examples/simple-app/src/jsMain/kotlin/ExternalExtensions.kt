@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.analysis.LayoutGraphAlgorithms.isAcyclic
import yfiles.analysis.LayoutGraphAlgorithms.isConnected
import yfiles.analysis.LayoutGraphAlgorithms.isForest
import yfiles.layout.LayoutGraph

fun externalExtensions() {
    val graph = LayoutGraph()
    // JS: LayoutGraphAlgorithms.isConnected(graph)
    graph.isConnected()
    // JS: LayoutGraphAlgorithms.isAcyclic(graph)
    graph.isAcyclic()
    // JS: LayoutGraphAlgorithms.isForest(graph, boolean)
    graph.isForest(true)
}
