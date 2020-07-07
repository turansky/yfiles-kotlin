package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.backend.common.ir.createImplicitParameterDeclarationWithWrappedDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.Modality
import org.jetbrains.kotlin.descriptors.Visibilities
import org.jetbrains.kotlin.ir.backend.js.ir.JsIrBuilder
import org.jetbrains.kotlin.ir.builders.declarations.buildClass
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.name.Name

internal fun baseClass(): IrClass = buildClass {
    origin = JsIrBuilder.SYNTHESIZED_DECLARATION

    name = Name.identifier("BaseClass_YYYY")
    kind = ClassKind.CLASS
    visibility = Visibilities.INTERNAL
    modality = Modality.ABSTRACT
    isExternal = true
}.apply {
    createImplicitParameterDeclarationWithWrappedDescriptor()
}
