import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO: remove after plugin fix
@Ignore
class CustomObjectTest {
    @Test
    fun testClassName() {
        val o = CustomObject()
        assertEquals("CustomObject", o.asDynamic().className)
    }
}
