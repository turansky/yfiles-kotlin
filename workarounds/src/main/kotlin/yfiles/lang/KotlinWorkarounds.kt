@file:Suppress("NOTHING_TO_INLINE")

package yfiles.lang

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

        val localObject: LikeObject = js("{}")

        Object.getOwnPropertyNames(Object)
            .filter { jsTypeOf(Object[it]) == "function" }
            .forEach { localObject[it] = Object[it] }

        val globalDefineProperty = Object::defineProperty
        localObject.defineProperty = { obj, prop, descriptor ->
            descriptor.configurable = true

            globalDefineProperty(obj, prop, descriptor)
        }

        localScope.Object = localObject
    }
}

private external interface Scope {
    var Object: LikeObject?
}

private external interface LikeObject {
    var defineProperty: (obj: Any, prop: String, descriptor: ObjectPropertyDescriptor) -> Unit
}

private external object Object {
    fun getOwnPropertyNames(o: Any): Array<String>

    fun defineProperty(obj: Any, prop: String, descriptor: ObjectPropertyDescriptor)
}

private external interface ObjectPropertyDescriptor {
    var configurable: Boolean?
}

private inline operator fun Object.get(propName: String): Any? {
    return asDynamic()[propName]
}

private inline operator fun LikeObject.set(propName: String, value: Any?) {
    asDynamic()[propName] = value
}
