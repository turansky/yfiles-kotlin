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
            inline fun <reified T: $ANY> $ILOOKUP.lookup(): T? =
                lookup(T::class.js.unsafeCast<$ICLASS_METADATA<T>>())
            
            inline fun <T : Any> $ILOOKUP.lookupValue(type: $ICLASS_METADATA<T>):T = 
                requireNotNull(lookup(type)) {
                    "Unable to lookup type ${'$'}type"
                }
            
            inline fun <reified T : $ANY> $ILOOKUP.lookupValue(): T =
                lookupValue(T::class.js.unsafeCast<$ICLASS_METADATA<T>>())      
        """.trimIndent()
        else -> null
    }

