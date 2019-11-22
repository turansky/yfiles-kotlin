package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.BackendContext
import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.resolve.BindingContext

internal class IrLoweringExtension : IrGenerationExtension {
    override fun generate(
        file: IrFile,
        backendContext: BackendContext,
        bindingContext: BindingContext
    ) {
        YClassLowering(backendContext, bindingContext)
            .runOnFilePostfix(file)
    }
}

private class YClassLowering(
    val context: BackendContext,
    val bindingContext: BindingContext
) : IrElementTransformerVoid(), ClassLoweringPass {
    override fun lower(irClass: IrClass) {
        when {
            !irClass.isClass -> return
            irClass.isExternal -> return
            irClass.isData -> return
            irClass.isInline -> return
        }
    }
}
