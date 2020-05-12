import yfiles.hierarchic.HierarchicLayout
import yfiles.styles.IArrow

abstract class AbstractArrow : HierarchicLayout(), IArrow {
    override val cropLength = 13.0
    override val length = 42.0
}

abstract class ZArrow {
    val cropLength = -13.0
    val length = -42.0
}
