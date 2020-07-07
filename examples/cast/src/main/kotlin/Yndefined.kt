@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.graph.DefaultGraph
import yfiles.graph.Tag

@JsExport
@ExperimentalJsExport
fun yndefined() {
    val graph = DefaultGraph()
    val node = graph.createNode(tag = Tag("node-tag"))
    val port = graph.addPort(owner = node, tag = Tag("port-tag"))
    val label = graph.addLabel(
        owner = node,
        text = "label-text",
        tag = Tag("label-tag")
    )
}
