package com.github.turansky.yfiles.compiler.backend.common

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

internal val YFILES_PACKAGE = FqName("yfiles")
internal val LANG_PACKAGE = YFILES_PACKAGE.child(identifier("lang"))
internal val YOBJECT = LANG_PACKAGE.child(identifier("YObject"))

internal val YCLASS_NAME = identifier("Class")
internal val BASE_CLASS_NAME = identifier("BaseClass")

private val YFILES_INTERFACE = LANG_PACKAGE.child(identifier("Interface"))

internal fun ClassDescriptor.isYFilesInterface(): Boolean =
    isExternal && annotations.hasAnnotation(YFILES_INTERFACE)

internal val ClassDescriptor.extendsYObject: Boolean
    get() {
        val superClass = getSuperClassNotAny()
            ?: return false

        return superClass.fqNameSafe == YOBJECT
    }

internal val ClassDescriptor.implementsYFilesInterface: Boolean
    get() = getSuperInterfaces().any { it.isYFilesInterface() }
