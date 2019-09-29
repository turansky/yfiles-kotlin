package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.backend.ast.JsStatement
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.DescriptorUtils.getFunctionByName

internal fun TranslationContext.fixType(
    descriptor: ClassDescriptor
): JsStatement {
    val classCompanion = currentModule.findClassAcrossModuleDependencies(
        ClassId(FqName("yfiles.lang"), identifier("Class"))
    )!!.companionObjectDescriptor!!

    val fixType = getFunctionByName(
        classCompanion.unsubstitutedMemberScope,
        identifier("fixType")
    )

    return JsInvocation(
        translateAsValueReference(fixType, this),
        translateAsValueReference(descriptor, this)
    ).makeStmt()
}