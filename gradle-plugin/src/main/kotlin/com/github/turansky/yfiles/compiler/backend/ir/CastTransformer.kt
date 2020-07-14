package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator
import org.jetbrains.kotlin.ir.expressions.IrTypeOperator.*
import org.jetbrains.kotlin.ir.expressions.IrTypeOperatorCall
import org.jetbrains.kotlin.ir.expressions.impl.IrTypeOperatorCallImpl
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.defaultType
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid

private val SUPPORTED_OPERATORS: Set<IrTypeOperator> = setOf(
    INSTANCEOF,
    NOT_INSTANCEOF,

    CAST,
    SAFE_CAST
)

internal class CastTransformer : IrElementTransformerVoid() {
    override fun visitTypeOperator(
        expression: IrTypeOperatorCall
    ): IrExpression =
        super.visitTypeOperator(expression.correct())

    private fun IrTypeOperatorCall.correct(): IrTypeOperatorCall {
        if (operator !in SUPPORTED_OPERATORS)
            return this

        val newTypeOperand = typeOperand.getClass()
            ?.takeIf { it.isYFilesInterface() }
            ?.companionObjectClass
            ?.defaultType
            ?: return this

        // TODO: Use delegate after bug fix
        //  https://youtrack.jetbrains.com/issue/KT-40130
        return IrTypeOperatorCallImpl(
            startOffset = startOffset,
            endOffset = endOffset,
            type = type,
            operator = operator,
            typeOperand = newTypeOperand,
            argument = argument
        )
    }
}
