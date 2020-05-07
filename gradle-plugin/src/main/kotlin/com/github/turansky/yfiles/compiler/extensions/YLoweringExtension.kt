package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.backend.common.LANG_PACKAGE
import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name

private val YOBJECT_ID = ClassId(
    LANG_PACKAGE,
    Name.identifier("YObject")
)

internal class YLoweringExtension : IrGenerationExtension {
    override fun generate(
        moduleFragment: IrModuleFragment,
        pluginContext: IrPluginContext
    ) {
        val classLowering = YClassLowering(pluginContext)
        for (file in moduleFragment.files) {
            classLowering.runOnFilePostfix(file)
        }
    }
}

private class YClassLowering(
    private val context: IrPluginContext
) : IrElementTransformerVoid(), ClassLoweringPass {
    override fun lower(irClass: IrClass) {
        if (irClass.name.identifier != "AbstractArrow3") {
            return
        }

        val yobject = context.moduleDescriptor.findClassAcrossModuleDependencies(YOBJECT_ID)!!
        val classReference = context.symbolTable.referenceClass(yobject)

        irClass.superTypes = listOf(
            IrSimpleTypeImpl(
                kotlinType = yobject.defaultType,
                classifier = classReference,
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

