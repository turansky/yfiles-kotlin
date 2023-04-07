@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.algorithms.Comparers.createComparableComparer
import yfiles.algorithms.Graph
import yfiles.layout.DefaultLayoutGraph
import yfiles.layout.TabularLayout
import yfiles.tree.NodeOrderComparer

fun comparer() {
    val g: Graph = DefaultLayoutGraph()
    g.sortNodes(NodeOrderComparer())
    g.sortNodes(createComparableComparer())

    val l = TabularLayout()
    l.nodeComparer = NodeOrderComparer()
    l.nodeComparer = createComparableComparer()
}