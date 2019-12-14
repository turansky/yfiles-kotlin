import yfiles.lang.ClassMetadata
import yfiles.lang.YObject
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
