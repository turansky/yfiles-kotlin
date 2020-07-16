package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrGetObjectValueImpl
import org.jetbrains.kotlin.ir.util.defaultType

internal fun IrClass.companionObjectExpression(
    offsetSource: IrExpression
): IrExpression {
    val companionClass = companionObjectClass

    return IrGetObjectValueImpl(
        startOffset = offsetSource.startOffset,
        endOffset = offsetSource.endOffset,
        type = companionClass.defaultType,
        symbol = companionClass.symbol
    )
}
