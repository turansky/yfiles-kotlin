package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.backend.common.CLASS_METADATA
import com.github.turansky.yfiles.compiler.backend.common.YOBJECT
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private val ClassDescriptor.isYObject: Boolean
    get() = isExternal && fqNameSafe == YOBJECT

internal fun ClassDescriptor.isYFilesInterface(): Boolean =
    isExternal and (isYObject or implementsYObject)

internal val ClassDescriptor.implementsYObjectDirectly: Boolean
    get() = getSuperInterfaces()
        .any { it.isYObject }

private val ClassDescriptor.implementsYObject: Boolean
    get() {
        if (implementsYObjectDirectly) {
            return true
        }

        return getSuperInterfaces()
            .any { it.implementsYObject }
    }

internal val ClassDescriptor.implementsYFilesInterface: Boolean
    get() = getSuperInterfaces()
        .any { it.isYFilesInterface() }

internal fun ClassDescriptor.asClassMetadata(): ClassDescriptor? =
    getSuperInterfaces().firstOrNull { it.fqNameSafe == CLASS_METADATA }
