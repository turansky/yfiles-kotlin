@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.geometry.Size
import yfiles.graph.DefaultGraph
import yfiles.graph.Tag
import yfiles.lang.YObject
import yfiles.styles.ShinyPlateNodeStyle
import yfiles.styles.StringTemplatePortStyle

@JsExport
@ExperimentalJsExport
fun yndefined1() {
    val graph = DefaultGraph()
    val node = graph.createNode(tag = Tag("node-tag"))
    val port = graph.addPort(owner = node, tag = Tag("port-tag"))
    val label = graph.addLabel(
        owner = node,
        text = "label-text",
        tag = Tag("label-tag")
    )
}

@JsExport
@ExperimentalJsExport
fun yndefined2() {
    val graph = DefaultGraph()
    val node = graph.createNode(style = ShinyPlateNodeStyle())
    val port = graph.addPort(owner = node, style = StringTemplatePortStyle())
    val label = graph.addLabel(
        owner = node,
        text = "label-text",
        preferredSize = Size.ZERO
    )
}

@JsExport
@ExperimentalJsExport
fun yndefined3() {
    val sd = SData()
    sd.create(d = "sd")

    val yd = YData()
    yd.create(d = "yd")
}

private external class SData {
    fun create(
        a: String = definedExternally,
        b: String = definedExternally,
        c: String = definedExternally,
        d: String = definedExternally,
    )
}

private external class YData : YObject {
    fun create(
        a: String = definedExternally,
        b: String = definedExternally,
        c: String = definedExternally,
        d: String = definedExternally,
    )
}
