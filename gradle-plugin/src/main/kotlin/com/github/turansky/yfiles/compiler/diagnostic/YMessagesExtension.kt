package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap

private val DIAGNOSTIC_FACTORY_TO_RENDERER by lazy {
    DiagnosticFactoryToRendererMap("yfiles").apply {
        put(
            BaseClassErrors.INTERFACE_IMPLEMENTING_NOT_SUPPORTED,
            "yFiles interface implementing supported only for ordinary classes"
        )
        put(
            BaseClassErrors.INTERFACE_MIXING_NOT_SUPPORTED,
            "yFiles interfaces could't be mixed with non-yFiles interfaces"
        )
        put(
            BaseClassErrors.INLINE_CLASS_NOT_SUPPORTED,
            "yFiles interface implementing not supported for inline classes"
        )

        put(
            YObjectErrors.INTERFACE_IMPLEMENTING_NOT_SUPPORTED,
            "Interface implementing not supported for direct `yfiles.lang.YObject` inheritors"
        )
    }
}

internal object YMessagesExtension : DefaultErrorMessages.Extension {
    override fun getMap(): DiagnosticFactoryToRendererMap =
        DIAGNOSTIC_FACTORY_TO_RENDERER
}
