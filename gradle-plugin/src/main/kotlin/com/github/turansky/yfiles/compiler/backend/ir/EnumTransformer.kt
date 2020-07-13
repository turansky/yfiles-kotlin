package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val GET_ENUM_NAME = FqName("yfiles.lang.getEnumName")
private val GET_ENUM_ORDINAL = FqName("yfiles.lang.getEnumOrdinal")

private val GET_NAME = Name.special("<get-name>")
private val GET_ORDINAL = Name.special("<get-ordinal>")

private val NAMES = setOf(
    GET_NAME,
    GET_ORDINAL
)

private val IrClass.isYFilesEnum
    get() = isExternal && isEnumClass
            && superTypes.any { it.getClass()?.isYEnum ?: false }

private val IrClass.isYEnumMetadataCompanion
    get() = isExternal && isCompanion
            && superTypes.any { it.getClass()?.isYEnumMetadata ?: false }

internal class EnumTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoid() {
    private val IrFunction.transformRequired: Boolean
        get() = name in NAMES && parent.let { it is IrClass && it.isYFilesEnum }

    override fun visitCall(expression: IrCall): IrExpression {
        val dispatchReceiver = expression.dispatchReceiver
            ?: return super.visitCall(expression)

        val function = expression.symbol.owner
        if (!function.transformRequired)
            return super.visitCall(expression)

        return when (function.name) {
            GET_NAME -> createCall(expression, GET_ENUM_NAME, dispatchReceiver)
            GET_ORDINAL -> createCall(expression, GET_ENUM_ORDINAL, dispatchReceiver)
            else -> expression
        }
    }

    private fun createCall(
        offsetSource: IrExpression,
        functionName: FqName,
        parameter: IrExpression
    ): IrCall {
        val type = parameter.type

        val function = context.referenceFunctions(functionName).single()
        val call = IrCallImpl(
            startOffset = offsetSource.startOffset,
            endOffset = offsetSource.endOffset,
            type = type,
            symbol = function
        )

        val companionClass = parameter.type.getClass()!!.companionObject()!! as IrClass
        val typeParameter = IrGetObjectValueImpl(
            startOffset = offsetSource.startOffset,
            endOffset = offsetSource.endOffset,
            type = companionClass.defaultType,
            symbol = companionClass.symbol
        )

        call.putTypeArgument(0, type)
        call.putValueArgument(0, parameter)
        call.putValueArgument(1, typeParameter)

        return call
    }
}

