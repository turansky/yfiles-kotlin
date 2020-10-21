package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.ir.createParameterDeclarations
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DescriptorVisibilities
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.ir.backend.js.utils.getJsName
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.declarations.IrDeclarationOrigin
import org.jetbrains.kotlin.ir.declarations.IrFactory
import org.jetbrains.kotlin.ir.types.IrType
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.name.Name

private const val Y = "\$module\$yfiles"

private val IrType.jsName: String
    get() = getJsName() ?: getClass()!!.name.identifier

private fun baseClassCall(superTypes: List<IrType>): String {
    val types = superTypes.joinToString(",") {
        "$Y.${it.jsName}"
    }

    return "$Y.BaseClass($types)"
}

internal fun IrFactory.baseClass(superTypes: List<IrType>): IrClass =
    buildClass {
        origin = IrDeclarationOrigin.FILE_CLASS

        name = Name.identifier(baseClassCall(superTypes))
        kind = ClassKind.CLASS
        visibility = DescriptorVisibilities.PRIVATE
        modality = Modality.ABSTRACT
        isExternal = true
    }.apply {
        createParameterDeclarations()
    }
