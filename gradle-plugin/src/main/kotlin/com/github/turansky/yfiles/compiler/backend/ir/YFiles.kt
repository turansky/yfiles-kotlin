package com.github.turansky.yfiles.compiler.backend.ir

import com.github.turansky.yfiles.compiler.backend.common.ENUM_METADATA
import com.github.turansky.yfiles.compiler.backend.common.YENUM
import com.github.turansky.yfiles.compiler.backend.common.YOBJECT
import org.jetbrains.kotlin.ir.declarations.IrClass
import org.jetbrains.kotlin.ir.util.fqNameWhenAvailable
import org.jetbrains.kotlin.ir.util.isInterface

private val IrClass.isYObject: Boolean
    get() = isExternal && fqNameWhenAvailable == YOBJECT

internal val IrClass.isYEnum: Boolean
    get() = isExternal && fqNameWhenAvailable == YENUM

internal val IrClass.isYEnumMetadata: Boolean
    get() = isExternal && fqNameWhenAvailable == ENUM_METADATA

internal fun IrClass.isYFilesInterface(): Boolean =
    isExternal && isInterface && (isYObject || implementsYObject)

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
