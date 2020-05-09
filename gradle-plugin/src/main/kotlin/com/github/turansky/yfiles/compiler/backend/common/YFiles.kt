package com.github.turansky.yfiles.compiler.backend.common

import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier

internal val YFILES_PACKAGE = FqName("yfiles")
internal val LANG_PACKAGE = YFILES_PACKAGE.child(identifier("lang"))
internal val YOBJECT = LANG_PACKAGE.child(identifier("YObject"))

internal val YCLASS_NAME = identifier("YClass")
internal val BASE_CLASS_NAME = identifier("BaseClass")

internal val CLASS_METADATA = LANG_PACKAGE.child(identifier("ClassMetadata"))
