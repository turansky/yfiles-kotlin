import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

class CustomObjectTest {
    @Test
    @Ignore
    fun itWorks() {
        val o = CustomObject()
        assertEquals("CustomObject", o.asDynamic().fixedClassName)
    }
}