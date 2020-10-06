package com.github.turansky.yfiles

internal fun Interface.getExtensions(): String? =
    when (classId) {
        // language=kotlin
        YOBJECT
        -> """
            inline fun <T: $YOBJECT> T.getClass(): $YCLASS<out T> =
                $AS_DYNAMIC.getClass()
        """.trimIndent()

        // language=kotlin
        ILOOKUP
        -> """
            inline fun <reified T : $YOBJECT> $ILOOKUP.lookup(): T? =
                lookup(T::class.js.yclass)     
                
            inline fun <T : $YOBJECT> $ILOOKUP.lookupValue(type: $YCLASS<T>):T = 
                requireNotNull(lookup(type)) {
                    "Unable to lookup type ${'$'}type"
                }
                
            inline fun <reified T : $YOBJECT> $ILOOKUP.lookupValue(): T =
                lookupValue(T::class.js.yclass)      
        """.trimIndent()

        else -> null
    }
