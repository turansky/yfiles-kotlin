package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.DescriptorUtils.getFunctionByName

internal fun TranslationContext.findFunction(
    packageName: String,
    functionName: String
): JsExpression {
    val descriptor = getFunctionByName(
        currentModule.getPackage(FqNameUnsafe(packageName).toSafe()).memberScope,
        identifier(functionName)
    )
    return translateAsValueReference(descriptor, this)
}