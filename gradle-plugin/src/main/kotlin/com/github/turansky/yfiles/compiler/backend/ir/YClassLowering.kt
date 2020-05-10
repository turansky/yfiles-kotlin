package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
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

internal class YClassLowering(
    private val context: IrPluginContext
) : IrElementTransformerVoid(), ClassLoweringPass {
    private fun findClassSymbol(id: ClassId): IrClassSymbol {
        val descriptor = context.moduleDescriptor.findClassAcrossModuleDependencies(id)!!
        return context.symbolTable.referenceClass(descriptor)
    }

    override fun lower(irClass: IrClass) {
        if (irClass.isExternal) return
        if (irClass.isInline) return
        if (!irClass.isClass) return
        // if (!irClass.implementsYFilesInterface) return
        if (irClass.name.identifier != "AbstractArrow2") return

        val arrow = findClassSymbol(ARROW_ID)

        irClass.superTypes = listOf(
            IrSimpleTypeImpl(
                classifier = arrow,
                hasQuestionMark = false,
                arguments = emptyList(),
                annotations = emptyList()
            )
        )
        irClass.transformChildrenVoid()
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
