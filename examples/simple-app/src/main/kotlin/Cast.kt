@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.graph.DefaultGraph
import yfiles.graph.IGraph
import yfiles.graph.IModelItem
import yfiles.graph.INode
import yfiles.lang.yAs
import yfiles.lang.yIs
import yfiles.lang.yOpt

fun cast() {
    val g: IGraph = DefaultGraph()
    val n1: IModelItem = g.createNode()
    val n2: INode = g.createNode()

    val isNode: Boolean = n1 yIs INode
    val optNode: INode? = n1 yOpt INode
    val asNode: INode = n1 yAs INode
}