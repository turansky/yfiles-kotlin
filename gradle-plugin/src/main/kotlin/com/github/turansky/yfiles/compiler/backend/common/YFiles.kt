package com.github.turansky.yfiles.compiler.backend.common

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

internal val YFILES_PACKAGE = FqName("yfiles")
internal val LANG_PACKAGE = YFILES_PACKAGE.child(identifier("lang"))
internal val YOBJECT = LANG_PACKAGE.child(identifier("YObject"))

internal val YCLASS_NAME = identifier("Class")
internal val BASE_CLASS_NAME = identifier("BaseClass")

private val CLASS_METADATA = LANG_PACKAGE.child(identifier("ClassMetadata"))

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

internal fun ClassDescriptor.asClassMetadata(): ClassDescriptor? =
    getSuperInterfaces().firstOrNull { it.fqNameSafe == CLASS_METADATA }
