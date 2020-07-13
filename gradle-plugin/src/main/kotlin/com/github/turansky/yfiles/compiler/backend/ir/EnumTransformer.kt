package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrFunction
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrCallImpl
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.types.defaultType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.util.isEnumClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val GET_ENUM_NAME = FqName("yfiles.lang.getEnumName")
private val GET_ENUM_ORDINAL = FqName("yfiles.lang.getEnumOrdinal")
private val GET_ENUM_VALUES = FqName("yfiles.lang.getEnumValues")

private val GET_NAME = Name.special("<get-name>")
private val GET_ORDINAL = Name.special("<get-ordinal>")

private val VALUES = Name.identifier("values")

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
        get() {
            val parent = parent as? IrClass
                ?: return false

            return when (name) {
                GET_NAME,
                GET_ORDINAL
                -> parent.isYFilesEnum

                VALUES
                -> parent.isYFilesEnum

                else -> false
            }
        }

    override fun visitCall(expression: IrCall): IrExpression {
        val function = expression.symbol.owner
        if (!function.transformRequired)
            return super.visitCall(expression)

        val transformedExpression = when (function.name) {
            GET_NAME -> createCall(expression, GET_ENUM_NAME)
            GET_ORDINAL -> createCall(expression, GET_ENUM_ORDINAL)

            VALUES -> createStaticCall(expression, GET_ENUM_VALUES)

            else -> null
        }

        return transformedExpression
            ?: super.visitCall(expression)
    }

    private fun createCall(
        sourceCall: IrCall,
        functionName: FqName
    ): IrCall? {
        val parameter = sourceCall.dispatchReceiver
            ?: return null

        val type = parameter.type
        val companionClass = type.getClass()?.companionObject() as? IrClass
            ?: return null

        val function = context.referenceFunctions(functionName).single()
        val call = IrCallImpl(
            startOffset = sourceCall.startOffset,
            endOffset = sourceCall.endOffset,
            type = type,
            symbol = function
        )

        val typeParameter = IrGetObjectValueImpl(
            startOffset = sourceCall.startOffset,
            endOffset = sourceCall.endOffset,
            type = companionClass.defaultType,
            symbol = companionClass.symbol
        )

        call.putTypeArgument(0, type)
        call.putValueArgument(0, parameter)
        call.putValueArgument(1, typeParameter)

        return call
    }

    private fun createStaticCall(
        sourceCall: IrCall,
        functionName: FqName
    ): IrCall {
        val function = context.referenceFunctions(functionName).single()
        val enumClass = sourceCall.symbol.owner.parent as IrClass
        val companionClass = enumClass.companionObject() as IrClass
        val call = IrCallImpl(
            startOffset = sourceCall.startOffset,
            endOffset = sourceCall.endOffset,
            type = context.symbols.array.defaultType,
            symbol = function
        )

        val typeParameter = IrGetObjectValueImpl(
            startOffset = sourceCall.startOffset,
            endOffset = sourceCall.endOffset,
            type = companionClass.defaultType,
            symbol = companionClass.symbol
        )

        call.putTypeArgument(0, enumClass.defaultType)
        call.putValueArgument(0, typeParameter)

        return call
    }
}

