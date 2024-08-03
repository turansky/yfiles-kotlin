package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration

class YRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration,
    ) {
        /*
        @Suppress("DEPRECATION")
        Extensions.getRootArea()
            .getExtensionPoint(DiagnosticSuppressor.EP_NAME)
            .registerExtension(YDiagnosticSuppressor()) { /* do nothing */ }
        */
    }
}
