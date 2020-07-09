package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.symbols.IrValueSymbol
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName

private val CALL_SUPER_CONSTRUCTOR = FqName("yfiles.lang.callSuperConstructor")

internal class SuperTypeTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoid() {
    private val baseClasses = mutableListOf<IrClass>()

    private val IrClass.transformRequired
        get() = when {
            isExternal -> false
            isInline -> false
            !isClass -> false
            else -> implementsYFilesInterface
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

        val baseClass = baseClass(declaration.superTypes)
        baseClasses.add(baseClass)

        declaration.superTypes += baseClass.typeWith(emptyList())

        return super.visitClass(declaration)
    }

    override fun visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall
    ): IrExpression {
        if (expression.valueArgumentsCount == 0)
            return super.visitDelegatingConstructorCall(expression)

        val callSuperConstructor = context.referenceFunctions(CALL_SUPER_CONSTRUCTOR).single()
        val call = IrCallImpl(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            type = context.irBuiltIns.unitType,
            symbol = callSuperConstructor
        )

        val thisValue = IrGetValueImpl(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            // TODO: fix
            symbol = expression.symbol as IrValueSymbol
        )
        call.putValueArgument(0, thisValue)
        return call
    }
}
