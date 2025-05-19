package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.timeSpanExtensions

internal fun Class.getExtensions(): String? =
    when (classId) {
        "yfiles.lang.TimeSpan",
        -> timeSpanExtensions(this)

        else
        -> null
    }

internal fun Interface.getExtensions(): String? =
    when (classId) {
        // language=kotlin
        ILOOKUP,
        -> """
            inline fun <reified T: Any> $ILOOKUP.lookup(): T? =
                lookup(T::class.js.unsafeCast<IClassMetadata<T>>())
            
            inline fun <T : Any> $ILOOKUP.lookupValue(type: $ICLASS_METADATA<T>):T = 
                requireNotNull(lookup(type)) {
                    "Unable to lookup type ${'$'}type"
                }
            
            inline fun <reified T : Any> $ILOOKUP.lookupValue(): T =
                lookupValue(T::class.js.unsafeCast<IClassMetadata<T>>())      
        """.trimIndent()
        else -> null
    }

