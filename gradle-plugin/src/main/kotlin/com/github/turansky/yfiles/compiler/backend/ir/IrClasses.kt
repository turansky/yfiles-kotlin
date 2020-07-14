package com.github.turansky.yfiles.compiler.backend.ir

import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.types.getClass
import org.jetbrains.kotlin.ir.util.companionObject

internal val IrClass.companionObjectClass: IrClass
    get() = companionObject() as IrClass

internal val IrClass.superInterfaces: List<IrClass>
    get() = superTypes
        .mapNotNull { it.getClass() }
        .filter { it.kind == ClassKind.INTERFACE }
