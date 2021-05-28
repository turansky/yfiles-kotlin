package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.backend.js.JsExtension
import com.github.turansky.yfiles.compiler.diagnostic.YDiagnosticSuppressor
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.com.intellij.openapi.extensions.Extensions
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.extensions.StorageComponentContainerContributor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor

class YRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration,
    ) {
        JsSyntheticTranslateExtension.registerExtension(project, JsExtension())

        IrGenerationExtension.registerExtension(project, YTransformExtension())
        StorageComponentContainerContributor.registerExtension(project, YStorageComponentContainerContributor())

        @Suppress("DEPRECATION")
        Extensions.getRootArea()
            .getExtensionPoint(DiagnosticSuppressor.EP_NAME)
            .registerExtension(YDiagnosticSuppressor()) { /* do nothing */ }
    }
}
