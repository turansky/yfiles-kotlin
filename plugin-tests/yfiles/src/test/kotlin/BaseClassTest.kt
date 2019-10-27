import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class BaseClassTest {
    @Test
    fun comboClassName() {
        val jsClass = ComboClass::class.js

        assertEquals(
            "BaseClass[IVisibilityTestable-IBoundsProvider]",
            jsClass.asDynamic().prototype.className
        )
    }

    @Test
    fun comboClassConstructor() {
        val jsClass = ComboClass::class.js

        assertEquals(
            jsClass.asDynamic().prototype.constructor,
            jsClass
        )
    }

    @Ignore
    @Test
    fun superComboClassName() {
        val jsClass = SuperComboClass::class.js

        assertEquals(
            "BaseClass[IVisibilityTestable-IBoundsProvider]",
            jsClass.asDynamic().prototype.className
        )
    }
}