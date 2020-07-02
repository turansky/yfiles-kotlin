package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.EXTENSIONS
import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.ILOOKUP
import com.github.turansky.yfiles.YCLASS
import com.github.turansky.yfiles.YOBJECT

internal fun generateLookupExtensions(context: GeneratorContext) {
    context[ILOOKUP, EXTENSIONS] = """
        import yfiles.lang.yclass
         
        inline fun <reified T : $YOBJECT> $ILOOKUP.lookup(): T? =
            lookup(T::class.js.yclass)     
            
        inline fun <T : $YOBJECT> $ILOOKUP.lookupValue(type: $YCLASS<T>):T = 
            requireNotNull(lookup(type)) {
                "Unable to lookup type ${'$'}type"
            }
            
        inline fun <reified T : $YOBJECT> $ILOOKUP.lookupValue(): T =
            lookupValue(T::class.js.yclass)      
    """.trimIndent()
}
