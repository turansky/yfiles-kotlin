package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.ContentMode.EXTENSIONS

internal fun generateLookupExtensions(context: GeneratorContext) {
    context[ILOOKUP, EXTENSIONS] = """
        import yfiles.lang.yclass
        
        inline infix fun <T : $YOBJECT> $ILOOKUP.lookup(type: $ICLASS_METADATA<T>):T? = 
            lookup(type.yclass)
            
        inline fun <reified T : $YOBJECT> $ILOOKUP.lookup(): T? =
            lookup(T::class.js.yclass)     
            
        inline infix fun <T : $YOBJECT> $ILOOKUP.lookupValue(type: $YCLASS<T>):T = 
            requireNotNull(lookup(type)) {
                "Unable to lookup type ${'$'}type"
            }
            
        inline infix fun <T : $YOBJECT> $ILOOKUP.lookupValue(type: $ICLASS_METADATA<T>):T = 
            lookupValue(type.yclass)
            
        inline fun <reified T : $YOBJECT> $ILOOKUP.lookupValue(): T =
            lookupValue(T::class.js.yclass)      
    """.trimIndent()
}
