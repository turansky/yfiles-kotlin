package com.github.turansky.yfiles.compiler.backend.common

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameSafe

private val YFILES_PACKAGE_ID = identifier("yfiles")
private val YFILES_PACKAGE = FqName(YFILES_PACKAGE_ID.identifier)
internal val LANG_PACKAGE = YFILES_PACKAGE.child(identifier("lang"))
internal val YENUM = LANG_PACKAGE.child(identifier("YEnum"))


internal val FqName.isYFiles: Boolean
    get() = startsWith(YFILES_PACKAGE_ID)

internal val ClassDescriptor.locatedInYFilesPackage: Boolean
    get() = fqNameSafe.isYFiles

internal fun ClassDescriptor.isYFilesInterface(): Boolean {
    return locatedInYFilesPackage && kind == ClassKind.INTERFACE
}

internal val ClassDescriptor.isYEnum: Boolean
    get() = locatedInYFilesPackage && kind == ClassKind.ENUM_CLASS
