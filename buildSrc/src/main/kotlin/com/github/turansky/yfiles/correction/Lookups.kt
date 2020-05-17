package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.EXTENSIONS
import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.ILOOKUP
import com.github.turansky.yfiles.TYPE_METADATA
import com.github.turansky.yfiles.YOBJECT

internal fun generateLookupExtensions(context: GeneratorContext) {
    context[ILOOKUP, EXTENSIONS] = """
        import yfiles.lang.yclass
        
        inline infix fun <T : $YOBJECT> $ILOOKUP.lookup(type: $TYPE_METADATA<T>):T? = 
            lookup(type.yclass)
    """.trimIndent()
}
