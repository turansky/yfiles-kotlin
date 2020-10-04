package com.github.turansky.yfiles.ide.js

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private val YFILES_PACKAGE_ID = Name.identifier("yfiles")
private val YOBJECT = FqName("yfiles.lang.YObject")
private val YENUM = FqName("yfiles.lang.YEnum")

internal val FqName.isYFiles: Boolean
    get() = startsWith(YFILES_PACKAGE_ID)

internal val ClassDescriptor.locatedInYFilesPackage: Boolean
    get() = fqNameSafe.isYFiles

private val ClassDescriptor.isYObject: Boolean
    get() = isExternal && fqNameSafe == YOBJECT

internal val ClassDescriptor.isYEnum: Boolean
    get() = isExternal && fqNameSafe == YENUM

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

internal val ClassDescriptor.baseClassUsed: Boolean
    get() = checkClass {
        it.all { it.isYFilesInterface() && !it.isYObject }
    }

internal val ClassDescriptor.classFixTypeUsed: Boolean
    get() = checkClass {
        it.singleOrNull()?.isYObject ?: false
    }

private fun ClassDescriptor.checkClass(
    check: (List<ClassDescriptor>) -> Boolean
): Boolean =
    when {
        isExternal -> false
        kind != ClassKind.CLASS -> false
        getSuperClassNotAny() != null -> false

        else -> getSuperInterfaces()
            .let { it.isNotEmpty() && check(it) }
    }
