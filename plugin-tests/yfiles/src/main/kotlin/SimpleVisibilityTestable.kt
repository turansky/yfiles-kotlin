import yfiles.geometry.Rect
import yfiles.view.ICanvasContext
import yfiles.view.IVisibilityTestable

abstract class IVisibilityTestableBase : IVisibilityTestable

class SimpleVisibilityTestable : IVisibilityTestableBase() {
    override fun isVisible(
        context: ICanvasContext,
        rectangle: Rect
    ): Boolean {
        return true
    }
}