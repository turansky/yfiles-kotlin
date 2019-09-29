package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private val YFILES_PACKAGE = identifier("yfiles")
private val YOBJECT = FqName("yfiles.lang.YObject")

internal fun ClassDescriptor.isYFiles(): Boolean {
    if (!isExternal) {
        return false
    }

    if (companionObjectDescriptor == null) {
        return false
    }

    val fqName = fqNameOrNull()
        ?: return false

    if (fqName.isRoot) {
        return false
    }

    return fqName.pathSegments().first() == YFILES_PACKAGE
}

internal val ClassDescriptor.extendsYObject: Boolean
    get() {
        val superClass = getSuperClassNotAny()
            ?: return false

        return superClass.fqNameSafe == YOBJECT
    }

internal val ClassDescriptor.implementsYFilesInterface: Boolean
    get() = getSuperInterfaces().any { it.isYFiles() }