package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.STRING

internal val IPROPERTY_OBSERVER = "yfiles.lang.IPropertyObserver"

internal fun generatePropertyObserver(context: GeneratorContext) {
    // language=kotlin
    context[IPROPERTY_OBSERVER] =
        """
            external interface IPropertyObserver {
                fun firePropertyChanged(propertyName: $STRING)
            }
        """.trimIndent()
}
