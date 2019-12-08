import yfiles.styles.IArrow

abstract class AbstractArrow : IArrow {
    override val cropLength: Double
        get() = 13.0

    override val length: Double
        get() = 42.0
}
