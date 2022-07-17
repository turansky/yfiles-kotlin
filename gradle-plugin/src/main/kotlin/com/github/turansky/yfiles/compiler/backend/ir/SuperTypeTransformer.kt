package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFile
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetValueImpl
import org.jetbrains.kotlin.ir.types.typeWith
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName

private val CALL_SUPER_CONSTRUCTOR = FqName("yfiles.lang.callSuperConstructor")

internal class SuperTypeTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoid() {
    private val baseClasses = mutableListOf<IrClass>()
    private var latestClass: IrClass? = null

    private val IrClass.transformRequired
        get() = when {
            isExternal -> false
            isValue -> false
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

        val baseClass = context.irFactory.baseClass(declaration.superTypes)
        baseClasses.add(baseClass)

        declaration.superTypes += baseClass.typeWith(emptyList())
        if (declaration.companionObject() == null) {
            val companionObject = context.irFactory.createCompanionObject()
            declaration.declarations += companionObject
            companionObject.parent = declaration
        }

        latestClass = declaration

        return super.visitClass(declaration)
    }

    override fun visitDelegatingConstructorCall(
        expression: IrDelegatingConstructorCall
    ): IrExpression {
        val thisReceiver = latestClass
            ?.takeIf { it != expression.symbol.owner.parent }
            ?.thisReceiver
            ?: return super.visitDelegatingConstructorCall(expression)

        latestClass = null

        val callSuperConstructor = context.referenceFunctions(CALL_SUPER_CONSTRUCTOR).single()
        val call = IrCallImpl(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            type = context.irBuiltIns.unitType,
            symbol = callSuperConstructor,
            typeArgumentsCount = 0,
            valueArgumentsCount = 1
        )

        val thisValue = IrGetValueImpl(
            startOffset = expression.startOffset,
            endOffset = expression.endOffset,
            symbol = thisReceiver.symbol
        )
        call.putValueArgument(0, thisValue)
        return call
    }
}
