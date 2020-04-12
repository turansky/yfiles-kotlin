package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

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

private class YClassLowering(
    val context: IrPluginContext
) : IrElementTransformerVoid(), ClassLoweringPass {
    override fun lower(irClass: IrClass) {
        // implement
    }
}
