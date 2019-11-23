import yfiles.lang.YObject
import kotlin.browser.window

class CustomObject : YObject() {
    fun hallo() {
        window.alert("Hallo from CustomObject!")
    }
}