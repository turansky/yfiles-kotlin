import kotlin.test.Ignore
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@Ignore
@Suppress("USELESS_IS_CHECK")
class CustomObjectTest {
    @Test
    fun testClassName() {
        val o = CustomObject()
        assertTrue {
            o is CustomObject
        }
        assertEquals("CustomObject", o.asDynamic().className)
    }
}
