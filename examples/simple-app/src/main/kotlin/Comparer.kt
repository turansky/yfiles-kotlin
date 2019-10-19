@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.algorithms.Comparers.createComparableComparer
import yfiles.algorithms.Graph
import yfiles.layout.DefaultLayoutGraph
import yfiles.tree.NodeOrderComparer

fun comparer() {
    val g: Graph = DefaultLayoutGraph()
    g.sortNodes(NodeOrderComparer())
    g.sortNodes(createComparableComparer())
}