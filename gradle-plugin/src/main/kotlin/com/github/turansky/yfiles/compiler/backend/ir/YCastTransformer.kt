package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.*
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.companionObject
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.ir.visitors.transformChildrenVoid

private val SUPPORTED_OPERATORS: Set<IrTypeOperator> = setOf(
    INSTANCEOF,
    NOT_INSTANCEOF,

    CAST,
    SAFE_CAST
)

internal class YCastTransformer : IrElementTransformerVoid() {
    override fun visitTypeOperator(
        expression: IrTypeOperatorCall
    ): IrExpression {
        expression.transformChildrenVoid(this)

        if (expression.operator !in SUPPORTED_OPERATORS)
            return expression

        val type = expression.typeOperand.getClass()
            ?.takeIf { it.isYFilesInterface() }
            ?.companionObject() as? IrClass
            ?: return expression

        return IrTypeOperatorCallDelegate(expression, type.defaultType)
    }
}

private class IrTypeOperatorCallDelegate(
    source: IrTypeOperatorCall,
    override val typeOperand: IrType
) : IrTypeOperatorCall by source
