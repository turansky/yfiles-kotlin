import kotlin.test.Test
import kotlin.test.assertEquals

class BaseClassTest {
    @Test
    fun comboClass() {
        assertEquals(
            "BaseClass[IVisibilityTestable-IBoundsProvider]",
            ComboClass::class.js.asDynamic().prototype.className
        )
    }
}