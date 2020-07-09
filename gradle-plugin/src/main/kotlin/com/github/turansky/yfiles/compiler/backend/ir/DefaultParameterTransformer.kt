package com.github.turansky.yfiles.compiler.backend.ir

import com.github.turansky.yfiles.compiler.backend.common.YOBJECT
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrElement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrCall
import org.jetbrains.kotlin.ir.expressions.IrConst
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.isSubtypeOfClass
import org.jetbrains.kotlin.ir.util.hasDefaultValue
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

internal class DefaultParameterTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoid() {
    private fun constNull(offsetSource: IrElement): IrConst<Nothing?> =
        IrConstImpl.constNull(
            startOffset = offsetSource.startOffset,
            endOffset = offsetSource.endOffset,
            type = context.irBuiltIns.anyNType
        )

    private val IrCall.checkRequired: Boolean
        get() {
            val function = symbol.owner
            val klass = function.parent as? IrClass ?: return false
            val yobject = context.referenceClass(YOBJECT) ?: return false

            return klass.isExternal
                    && klass.symbol.isSubtypeOfClass(yobject)
                    && function.valueParameters.any { it.hasDefaultValue() }
        }

    private fun IrCall.fixDefaultValues() {
        var index = valueArgumentsCount - 1
        while (index >= 0 && getValueArgument(index) == null) {
            index--
        }

        for (i in 0 until index) {
            if (getValueArgument(i) == null) {
                putValueArgument(i, constNull(this))
            }
        }
    }

    override fun visitCall(expression: IrCall): IrExpression {
        if (expression.checkRequired) {
            expression.fixDefaultValues()
        }

        return super.visitCall(expression)
    }
}
