import yfiles.geometry.Rect
import yfiles.view.ICanvasContext
import yfiles.view.IVisibilityTestable

class MyVisibilityTestable : IVisibilityTestable {
    override fun isVisible(
        context: ICanvasContext,
        rectangle: Rect
    ): Boolean {
        return true
    }
}