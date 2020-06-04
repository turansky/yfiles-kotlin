import yfiles.lang.TypeMetadata
import yfiles.lang.YClass
import yfiles.lang.YObject
import kotlin.browser.window

class CustomObject : YObject {
    fun hallo() {
        window.alert("Hallo from CustomObject!")
    }

    companion object : TypeMetadata<CustomObject> {
        override val yclass: YClass<CustomObject>
            get() = TODO("not implemented")
    }
}

class OtherCustomObject : YObject {
    fun hallo() {
        window.alert("Hallo from OtherCustomObject!")
    }
}
