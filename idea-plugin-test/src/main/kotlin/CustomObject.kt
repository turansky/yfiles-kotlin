import yfiles.lang.ClassMetadata
import yfiles.lang.YObject
import yfiles.styles.IArrow
import kotlinx.browser.window
import yfiles.lang.IClassMetadata
import yfiles.lang.classMetadata

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
