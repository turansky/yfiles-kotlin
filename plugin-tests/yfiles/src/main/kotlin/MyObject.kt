import yfiles.lang.YObject
import kotlin.browser.window

class MyObject : YObject() {
    fun hallo() {
        window.alert("Hallo from MyObject!")
    }
}