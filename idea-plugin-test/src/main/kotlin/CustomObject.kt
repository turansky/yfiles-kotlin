import yfiles.lang.ClassMetadata
import yfiles.lang.YObject
import yfiles.styles.IArrow
import kotlinx.browser.window
import yfiles.lang.IClassMetadata
import yfiles.lang.classMetadata
import yfiles.view.Color

/**
 * [yfiles.styles.ITemplateStyleBindingContext.zoom]
 */

class CustomObject : YObject {
    fun hallo() {
        window.alert("Hallo from CustomObject!")
    }

    companion object : IClassMetadata<CustomObject> by classMetadata()
}

class OtherCustomObject : YObject {
    fun hallo() {
        window.alert("Hallo from OtherCustomObject!")
    }
}

abstract class AbstractArrow : IArrow {}

fun colorTest() {
    val color1 = Color("#FF0000")
    val color2 = Color("#00FF00")
    val color3 = Color("#0000FF")
}
