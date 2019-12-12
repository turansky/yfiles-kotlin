import yfiles.styles.PolylineEdgeStyleRenderer

abstract class MyPolylineEdgeStyleRenderer : PolylineEdgeStyleRenderer() {
    override val addBridges: Boolean
        get() = false
}
