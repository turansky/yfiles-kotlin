package com.github.turansky.yfiles.compiler.backend.common

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private val YFILES_PACKAGE_ID = identifier("yfiles")
private val YFILES_PACKAGE = FqName(YFILES_PACKAGE_ID.identifier)
internal val LANG_PACKAGE = YFILES_PACKAGE.child(identifier("lang"))
internal val YOBJECT = LANG_PACKAGE.child(identifier("YObject"))
internal val YENUM = LANG_PACKAGE.child(identifier("YEnum"))

internal val YCLASS_NAME = identifier("YClass")
internal val BASE_CLASS_NAME = identifier("BaseClass")

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
