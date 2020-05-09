package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.backend.ir.YClassLowering
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment

internal class YLoweringExtension : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val classLowering = YClassLowering(pluginContext)
        for (file in moduleFragment.files) {
            classLowering.runOnFilePostfix(file)
        }
    }
}
