import yfiles.lang.ConfigurableProperties
import yfiles.styles.IArrow

@ConfigurableProperties
abstract class AbstractArrow : IArrow {
    override val cropLength = 13.0
    override val length = 42.0
}
