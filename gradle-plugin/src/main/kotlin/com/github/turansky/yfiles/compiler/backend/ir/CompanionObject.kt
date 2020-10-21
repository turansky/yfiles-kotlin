package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.name.SpecialNames.DEFAULT_NAME_FOR_COMPANION_OBJECT

internal fun IrFactory.createCompanionObject(): IrClass =
    buildClass {
        origin = IrDeclarationOrigin.FILE_CLASS

        name = DEFAULT_NAME_FOR_COMPANION_OBJECT
        kind = ClassKind.OBJECT
        isCompanion = true
        visibility = DescriptorVisibilities.PUBLIC
    }.apply {
        createParameterDeclarations()
    }
