package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.backend.ir.IrLoweringExtension
import com.github.turansky.yfiles.compiler.backend.js.JsExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.resolve.extensions.SyntheticResolveExtension

class YRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        SyntheticResolveExtension.registerExtension(project, ResolveExtension())

        IrGenerationExtension.registerExtension(project, IrLoweringExtension())
        JsSyntheticTranslateExtension.registerExtension(project, JsExtension())
    }
}
