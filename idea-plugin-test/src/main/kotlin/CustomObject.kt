import yfiles.lang.ClassMetadata
import yfiles.lang.YObject
import yfiles.styles.IArrow
import kotlin.browser.window

class CustomObject : YObject {
    fun hallo() {
        window.alert("Hallo from CustomObject!")
    }

    companion object : ClassMetadata<CustomObject>
}

class OtherCustomObject : YObject {
    fun hallo() {
        window.alert("Hallo from OtherCustomObject!")
    }
}

abstract class AbstractArrow : IArrow {}
