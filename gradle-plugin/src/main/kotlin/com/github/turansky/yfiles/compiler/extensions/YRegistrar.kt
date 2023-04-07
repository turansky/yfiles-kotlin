package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.diagnostic.YDiagnosticSuppressor
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.compiler.plugin.CompilerPluginRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor

class YRegistrar : CompilerPluginRegistrar() {
    override val supportsK2: Boolean = false

    override fun ExtensionStorage.registerExtensions(
        configuration: CompilerConfiguration,
    ) {
        @Suppress("DEPRECATION")
        Extensions.getRootArea()
            .getExtensionPoint(DiagnosticSuppressor.EP_NAME)
            .registerExtension(YDiagnosticSuppressor()) { /* do nothing */ }

    }
}
