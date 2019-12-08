@file:Suppress("NOTHING_TO_INLINE")

package yfiles.lang

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ConfigurableProperties

// TODO: remove after fix - https://youtrack.jetbrains.com/issue/KT-34770
internal object KotlinWorkarounds {
    const val KT_34770 = "KT-34770"

    inline fun apply(
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

internal external object Object {
    var defineProperty: (obj: Any, prop: String, descriptor: ObjectPropertyDescriptor) -> Unit
}

internal external interface ObjectPropertyDescriptor {
    var configurable: Boolean?
}

internal inline val Any.constructor: Any?
    get() = asDynamic().constructor

internal inline var Any.kt34770: Boolean?
    get() = asDynamic().kt34770
    set(value) {
        asDynamic().kt34770 = value
    }
