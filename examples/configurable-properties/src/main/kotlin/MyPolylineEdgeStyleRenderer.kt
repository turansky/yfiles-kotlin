import yfiles.lang.ConfigurableProperties
import yfiles.styles.PolylineEdgeStyleRenderer

@ConfigurableProperties
abstract class MyPolylineEdgeStyleRenderer : PolylineEdgeStyleRenderer() {
    override val addBridges: Boolean
        get() = false
}
