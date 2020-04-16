import yfiles.styles.IArrow

@JsExport
@ExperimentalJsExport
abstract class AbstractArrow : IArrow {
    override val cropLength = 13.0
    override val length = 42.0
}

@JsExport
@ExperimentalJsExport
abstract class AbstractArrow2 : AbstractArrow() {
    override val cropLength = 14.0
    override val length = 43.0
}

abstract class ZArrow {
    val cropLength = -13.0
    val length = -42.0
}
