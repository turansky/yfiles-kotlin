package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId

internal class YClassTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoid() {
    private val baseClasses = mutableListOf<IrClass>()

    private fun findClassSymbol(id: ClassId): IrClassSymbol =
        context.referenceClass(id.asSingleFqName())!!

    private val IrClass.transformRequired
        get() = when {
            isExternal -> false
            isInline -> false
            !isClass -> false
            // else -> implementsYFilesInterface
            else -> name.identifier == "AbstractArrow2"
        }

    override fun visitFile(declaration: IrFile): IrFile {
        val file = super.visitFile(declaration)

        for (baseClass in baseClasses) {
            baseClass.parent = file
            file.declarations.add(baseClass)
        }

        baseClasses.clear()

        return file
    }

    override fun visitClass(declaration: IrClass): IrStatement {
        if (!declaration.transformRequired)
            return declaration

        val baseClass = baseClass()
        baseClasses.add(baseClass)

        /*
        declaration.superTypes = listOf(
            baseClass.typeWith(emptyList())
        )
        */

        return super.visitClass(declaration)
    }

    override fun visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall
    ): IrExpression {
        return IrConstImpl.constNull(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            type = context.irBuiltIns.anyNType
        )
    }
}
