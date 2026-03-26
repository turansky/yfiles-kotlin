@file:Suppress("USELESS_IS_CHECK")

import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

@Suppress("CANNOT_CHECK_FOR_EXTERNAL_INTERFACE")
@Ignore
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
    fun superComboClassName() {
        val jsClass = SuperComboClass::class.js

        assertEquals(
            "BaseClass[IVisibilityTestable-IBoundsProvider]",
            jsClass.asDynamic().prototype.className
        )
    }
}
