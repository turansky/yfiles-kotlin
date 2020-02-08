import yfiles.styles.IArrow

abstract class CustomArrow : IArrow {
    override val cropLength = 13.0
    override val length = 42.0
}

abstract class ZArrow {
    val cropLength: Double
        get() = -13.0

    val length: Double
        get() = -42.0
}
