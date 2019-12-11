@file:Suppress("NOTHING_TO_INLINE")

package yfiles.lang

import kotlin.browser.window

@Target(AnnotationTarget.CLASS)
@Retention(AnnotationRetention.BINARY)
annotation class ConfigurableProperties

// TODO: remove after fix - https://youtrack.jetbrains.com/issue/KT-34770
@Deprecated(message = "For code generation only", level = DeprecationLevel.HIDDEN)
object KotlinWorkarounds {
    fun apply(scope: dynamic) {
        val localScope = scope.unsafeCast<Scope>()
        if (localScope.Object != null) {
            return
        }

        val globalObject = window.unsafeCast<Scope>().Object!!

        val localObject: Object = js("{}")
        localObject.defineProperty = { obj, prop, descriptor ->
            descriptor.configurable = true

            globalObject.defineProperty(obj, prop, descriptor)
        }

        localScope.Object = localObject
    }
}

private external interface Scope {
    var Object: Object?
}

private external interface Object {
    var defineProperty: (obj: Any, prop: String, descriptor: ObjectPropertyDescriptor) -> Unit
}

private external interface ObjectPropertyDescriptor {
    var configurable: Boolean?
}
