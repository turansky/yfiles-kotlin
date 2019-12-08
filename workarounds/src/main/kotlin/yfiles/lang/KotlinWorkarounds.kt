@file:Suppress("NOTHING_TO_INLINE")

package yfiles.lang

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ConfigurableProperties

// TODO: remove after fix - https://youtrack.jetbrains.com/issue/KT-34770
@Deprecated(message = "For code generation only", level = DeprecationLevel.HIDDEN)
object KotlinWorkarounds {
    private const val KT_34770 = "KT-34770"

    fun apply(
        jsClass: JsClass<*>,
        workaroundName: String
    ) {
        if (workaroundName != KT_34770) {
            return
        }

        jsClass.kt34770 = true
        if (Object.defineProperty.kt34770 == true) {
            return
        }

        val originalDefineProperty = Object.defineProperty
        Object.defineProperty = { obj, prop, descriptor ->
            if (obj.constructor?.kt34770 == true) {
                descriptor.configurable = true
            }

            originalDefineProperty(obj, prop, descriptor)
        }
    }
}

private external object Object {
    var defineProperty: (obj: Any, prop: String, descriptor: ObjectPropertyDescriptor) -> Unit
}

private external interface ObjectPropertyDescriptor {
    var configurable: Boolean?
}

private inline val Any.constructor: Any?
    get() = asDynamic().constructor

private inline var Any.kt34770: Boolean?
    get() = asDynamic().kt34770
    set(value) {
        asDynamic().kt34770 = value
    }
