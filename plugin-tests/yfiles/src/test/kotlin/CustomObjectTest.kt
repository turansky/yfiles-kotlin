import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomObjectTest {
    @Test
    @Ignore
    fun testClassName() {
        val o = CustomObject()
        assertEquals("CustomObject", o.asDynamic().fixedClassName)
    }
}