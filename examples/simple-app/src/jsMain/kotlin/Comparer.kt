@file:Suppress("unused")

import yfiles.layout.LayoutGraph

fun comparer() {
    val g = LayoutGraph()
    g.sortNodes { x, y -> x.index.compareTo(y.index) }
}