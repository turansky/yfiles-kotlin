import yfiles.view.Color.Companion.BLUE
import yfiles.view.Color.Companion.LIME
import yfiles.view.Color.Companion.RED
import yfiles.view.Fill

class Fill2(val source: String)

fun fillTest() {
    val fill_01_1 = Fill("red")
    val fill_01_2 = Fill("lime")
    val fill_01_3 = Fill("blue")

    val fill_11_1 = Fill("#F00")
    val fill_11_2 = Fill("#0F0")
    val fill_11_3 = Fill("#00F")

    val fill_12_1 = Fill("#FF0000")
    val fill_12_2 = Fill("#00FF00")
    val fill_12_3 = Fill("#0000FF")

    val fill_21_1 = Fill("rgb(255, 0, 0)")
    val fill_21_2 = Fill("rgb(0, 255, 0)")
    val fill_21_3 = Fill("rgb(0, 0, 255)")

    val fill_31_1 = Fill("hsl(0, 100%, 50%)")
    val fill_31_2 = Fill("hsl(120, 100%, 50%)")
    val fill_31_3 = Fill("hsl(240, 100%, 50%)")

    val fill_71_1 = Fill.RED
    val fill_71_2 = Fill.LIME
    val fill_71_3 = Fill.BLUE

    val fill_72_1 = RED
    val fill_72_2 = LIME
    val fill_72_3 = BLUE
}

// language=CSS
val FILL_CSS = """
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
