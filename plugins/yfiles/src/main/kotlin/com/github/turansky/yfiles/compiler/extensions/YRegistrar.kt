package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.backend.ir.IrExtension
import com.github.turansky.yfiles.compiler.backend.js.JsExtension
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.com.intellij.mock.MockProject
import org.jetbrains.kotlin.compiler.plugin.ComponentRegistrar
import org.jetbrains.kotlin.config.CompilerConfiguration
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension

class YRegistrar : ComponentRegistrar {
    override fun registerProjectComponents(
        project: MockProject,
        configuration: CompilerConfiguration
    ) {
        IrGenerationExtension.registerExtension(project, IrExtension())
        JsSyntheticTranslateExtension.registerExtension(project, JsExtension())
    }
}
