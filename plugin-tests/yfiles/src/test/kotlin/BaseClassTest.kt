import kotlin.test.Test
import kotlin.test.assertEquals

class BaseClassTest {
    @Test
    fun comboClass() {
        val jsClass = ComboClass::class.js

        assertEquals(
            "BaseClass[IVisibilityTestable-IBoundsProvider]",
            jsClass.asDynamic().prototype.className
        )

        assertEquals(
            jsClass.asDynamic().prototype.constructor,
            jsClass
        )
    }
}