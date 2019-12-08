import yfiles.lang.ConfigurableProperties
import yfiles.styles.IArrow

@ConfigurableProperties
abstract class AbstractArrow : IArrow {
    override val cropLength: Double
        get() = 13.0

    override val length: Double
        get() = 42.0
}
