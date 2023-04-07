import yfiles.styles.IArrow

abstract class AbstractArrow : IArrow {
    override val cropLength = 13.0
    override val length = 42.0
}

abstract class ZArrow {
    val cropLength = -13.0
    val length = -42.0
}
