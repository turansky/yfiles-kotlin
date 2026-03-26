@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.graph.Graph
import yfiles.graph.IGraph
import yfiles.graph.IModelItem
import yfiles.graph.INode

@Suppress("CANNOT_CHECK_FOR_EXTERNAL_INTERFACE", "UNCHECKED_CAST_TO_EXTERNAL_INTERFACE")
fun cast() {
    val g: IGraph = Graph()
    val n1: IModelItem = g.createNode()
    val n2: INode = g.createNode()

    val isNode: Boolean = n1 is INode
    val optNode: INode? = n1 as? INode
    val asNode: INode = n1 as INode
}