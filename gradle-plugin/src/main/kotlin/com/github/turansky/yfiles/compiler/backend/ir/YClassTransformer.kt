package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.ir.IrStatement
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.symbols.IrClassSymbol
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.util.isClass
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val ARROW_ID = ClassId(
    FqName("yfiles.styles"),
    Name.identifier("Arrow")
)

internal class YClassTransformer(
    private val context: IrPluginContext
) : IrElementTransformerVoid() {
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

    override fun visitClass(declaration: IrClass): IrStatement {
        if (declaration.transformRequired) {
            val arrow = findClassSymbol(ARROW_ID)

            declaration.superTypes = listOf(
                IrSimpleTypeImpl(
                    classifier = arrow,
                    hasQuestionMark = false,
                    arguments = emptyList(),
                    annotations = emptyList()
                )
            )
        }

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
