package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.BackendContext
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.resolve.BindingContext

internal class IrExtension : IrGenerationExtension {
    override fun generate(
        file: org.jetbrains.kotlin.ir.declarations.IrFile,
        backendContext: BackendContext,
        bindingContext: BindingContext
    ) {
        // implement
    }
}
