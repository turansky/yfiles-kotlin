package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.rendering.DefaultErrorMessages
import org.jetbrains.kotlin.diagnostics.rendering.DiagnosticFactoryToRendererMap

private val DIAGNOSTIC_FACTORY_TO_RENDERER by lazy {
    DiagnosticFactoryToRendererMap("yfiles").apply {
        put(
            BASE_CLASS__INTERFACE_IMPLEMENTING_NOT_SUPPORTED,
            "yFiles interface implementing supported only for ordinal classes"
        )
        put(
            BASE_CLASS__INTERFACE_MIXING_NOT_SUPPORTED,
            "yFiles interfaces could't be mixed with non-yFiles interfaces"
        )
        put(
            BASE_CLASS__INLINE_CLASS_NOT_SUPPORTED,
            "yFiles interface implementing not supported for inline classes"
        )
        put(
            BASE_CLASS__DATA_CLASS_NOT_SUPPORTED,
            "yFiles interface implementing not supported for data classes"
        )
        put(
            BASE_CLASS__COMPANION_OBJECT_NOT_SUPPORTED,
            "yFiles interface implementing not supported for companion objects"
        )

        put(
            YOBJECT__INTERFACE_IMPLEMENTING_NOT_SUPPORTED,
            "Interface implementing not supported for direct `yfiles.lang.YObject` inheritors"
        )
        put(
            YOBJECT__COMPANION_OBJECT_NOT_SUPPORTED,
            "Parent type `yfiles.lang.YObject` not supported for companion objects"
        )

        put(
            CLASS_METADATA__INVALID_TYPE_PARAMETER,
            "Invalid type parameter for ClassMetadata"
        )
    }
}

internal object YMessagesExtension : DefaultErrorMessages.Extension {
    override fun getMap(): DiagnosticFactoryToRendererMap =
        DIAGNOSTIC_FACTORY_TO_RENDERER
}
