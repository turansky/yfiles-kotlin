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
    val color_01_1 = Color("red")
    val color_01_2 = Color("lime")
    val color_01_3 = Color("blue")

    val color_11_1 = Color("#F00")
    val color_11_2 = Color("#0F0")
    val color_11_3 = Color("#00F")

    val color_12_1 = Color("#FF0000")
    val color_12_2 = Color("#00FF00")
    val color_12_3 = Color("#0000FF")

    val color_21_1 = Color("rgb(255, 0, 0)")
    val color_21_2 = Color("rgb(0, 255, 0)")
    val color_21_3 = Color("rgb(0, 0, 255)")

    val color_31_1 = Color("hsl(0, 100%, 50%)")
    val color_31_2 = Color("hsl(120, 100%, 50%)")
    val color_31_3 = Color("hsl(240, 100%, 50%)")

    val color_71_1 = Color.RED
    val color_71_2 = Color.LIME
    val color_71_3 = Color.BLUE
}

// language=CSS
val CSS = """
    #red {
        background-color: red   
        border-color: hsl(0, 100%, 50%)
    }
    
    #green {
        background-color: lime    
        border-color: hsl(120, 100%, 50%)
    }
    
    #blue {
        background-color: blue    
        border-color: hsl(240, 100%, 50%)
    }
""".trimIndent()
