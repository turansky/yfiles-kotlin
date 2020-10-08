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
    val color_11_1 = Color("#F00")
    val color_11_2 = Color("#0F0")
    val color_11_3 = Color("#00F")

    val color_12_1 = Color("#FF0000")
    val color_12_2 = Color("#00FF00")
    val color_12_3 = Color("#0000FF")

    val color_21_1 = Color("rgb(255, 0, 0)")
    val color_21_2 = Color("rgb(0, 255, 0)")
    val color_21_3 = Color("rgb(0, 0, 255)")
}

// language=CSS
val CSS = """
    #red {
        background-color: red    
    }
    
    #green {
        background-color: lime    
    }
    
    #blue {
        background-color: blue    
    }
""".trimIndent()
