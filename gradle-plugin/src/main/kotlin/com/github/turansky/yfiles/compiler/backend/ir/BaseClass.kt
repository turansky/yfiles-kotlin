package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.name.Name

private const val Y = "\$module\$yfiles"

internal fun baseClass(): IrClass = buildClass {
    origin = IrDeclarationOrigin.FILE_CLASS

    name = Name.identifier("$Y.BaseClass($Y.IArrow)")
    kind = ClassKind.CLASS
    visibility = Visibilities.PRIVATE
    modality = Modality.ABSTRACT
    isExternal = true
}.apply {
    createParameterDeclarations()
}
