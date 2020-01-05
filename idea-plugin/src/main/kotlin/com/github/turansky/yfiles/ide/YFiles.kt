package com.github.turansky.yfiles.ide

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private val YOBJECT = FqName("yfiles.lang.YObject")

private fun ClassDescriptor.isYObject(): Boolean =
    isExternal && fqNameSafe == YOBJECT

internal fun ClassDescriptor.isYFilesInterface(): Boolean =
    isExternal && implementsYObject

internal val ClassDescriptor.implementsYObjectDirectly: Boolean
    get() = getSuperInterfaces()
        .any { it.isYObject() }

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
