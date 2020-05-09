package com.github.turansky.yfiles.compiler.backend.ir

import com.github.turansky.yfiles.compiler.backend.common.YOBJECT
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable

private val IrClass.isYObject: Boolean
    get() = isExternal && fqNameWhenAvailable == YOBJECT

internal fun IrClass.isYFilesInterface(): Boolean =
    isExternal and (isYObject or implementsYObject)

internal val IrClass.implementsYObjectDirectly: Boolean
    get() = superInterfaces
        .any { it.isYObject }

private val IrClass.implementsYObject: Boolean
    get() {
        if (implementsYObjectDirectly) {
            return true
        }

        return superInterfaces
            .any { it.implementsYObject }
    }

internal val IrClass.implementsYFilesInterface: Boolean
    get() = superInterfaces
        .any { it.isYFilesInterface() }
