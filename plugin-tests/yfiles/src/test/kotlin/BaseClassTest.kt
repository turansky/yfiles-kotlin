import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseClassTest {
    @Test
    @Ignore
    fun comboClass() {
        assertEquals(
            "IVisibilityTestable-IBoundsProvider",
            ComboClass::class.js.asDynamic().className
        )
    }
}