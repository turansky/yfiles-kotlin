import yfiles.geometry.Rect
import yfiles.view.IBoundsProvider
import yfiles.view.ICanvasContext
import yfiles.view.IVisibilityTestable

abstract class ComboClass : IVisibilityTestable, IBoundsProvider

abstract class SuperComboClass : ComboClass() {
    val puper = true
}

object SimpleComboObject : IVisibilityTestable, IBoundsProvider {
    override fun isVisible(context: ICanvasContext, rectangle: Rect): Boolean = false

    override fun getBounds(context: ICanvasContext): Rect = TODO()
}
