package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

internal class EnumTransformer() : IrElementTransformerVoid() {
    private val IrClass.transformRequired
        get() = isExternal && isEnumClass
                && superTypes.singleOrNull()?.getClass()?.isYEnum ?: false

    override fun visitClass(declaration: IrClass): IrStatement {
        if (!declaration.transformRequired)
            return declaration

        return super.visitClass(declaration)
    }
}
