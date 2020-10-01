import Object.Companion.getOwnPropertyDescriptor
import kotlin.test.Test
import kotlin.test.assertTrue

class ConfigurablePropertyTest {
    @Test
    fun arrowProperties() {
        val jsClassPrototype: Any = CustomArrow::class.js.asDynamic().prototype

        assertTrue {
            getOwnPropertyDescriptor(jsClassPrototype, "cropLength").configurable
        }

        assertTrue {
            getOwnPropertyDescriptor(jsClassPrototype, "length").configurable
        }
    }

    @Test
    fun arrowDelegateProperties() {
        val jsClassPrototype: Any = ArrowDelegate::class.js.asDynamic().prototype

        assertTrue {
            getOwnPropertyDescriptor(jsClassPrototype, "cropLength").configurable
        }

        assertTrue {
            getOwnPropertyDescriptor(jsClassPrototype, "length").configurable
        }
    }
}
