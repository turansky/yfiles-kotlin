package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.ClassLoweringPass
import org.jetbrains.kotlin.backend.common.extensions.IrGenerationExtension
import org.jetbrains.kotlin.backend.common.extensions.IrPluginContext
import org.jetbrains.kotlin.backend.common.runOnFilePostfix
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrModuleFragment
import org.jetbrains.kotlin.ir.expressions.IrDelegatingConstructorCall
import org.jetbrains.kotlin.ir.expressions.IrExpression
import org.jetbrains.kotlin.ir.expressions.impl.IrConstImpl
import org.jetbrains.kotlin.ir.types.impl.IrSimpleTypeImpl
import org.jetbrains.kotlin.ir.visitors.IrElementTransformerVoid
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name

private val ARROW_ID = ClassId(
    FqName("yfiles.styles"),
    Name.identifier("Arrow")
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
        if (irClass.isExternal) {
            return
        }

        when (irClass.kind) {
            ClassKind.CLASS
            -> generateClass(irClass)

            ClassKind.OBJECT,
            ClassKind.INTERFACE,
            ClassKind.ENUM_CLASS,
            -> checkInterfaces(irClass)

            else -> {
                /* do nothing */
            }
        }

        if (irClass.isCompanion) {
            enrichCompanion(irClass)
        }
    }

    private fun checkInterfaces(irClass: IrClass) {
        // implement
    }

    private fun generateClass(irClass: IrClass) {
        if (irClass.name.identifier != "AbstractArrow2") {
            return
        }

        val arrow = context.moduleDescriptor.findClassAcrossModuleDependencies(ARROW_ID)!!
        val classReference = context.symbolTable.referenceClass(arrow)

        irClass.superTypes = listOf(
            IrSimpleTypeImpl(
                classifier = classReference,
                hasQuestionMark = false,
                arguments = emptyList(),
                annotations = emptyList()
            )
        )
        irClass.transformChildrenVoid()
    }

    private fun enrichCompanion(irClass: IrClass) {
        // implement
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

