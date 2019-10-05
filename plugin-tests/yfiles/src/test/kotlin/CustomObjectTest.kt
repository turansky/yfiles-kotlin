import kotlin.test.Test
import kotlin.test.assertEquals

class CustomObjectTest {
    @Test
    fun testClassName() {
        val o = CustomObject()
        assertEquals("CustomObject", o.asDynamic().fixedClassName)
    }
}