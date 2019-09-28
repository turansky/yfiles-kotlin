package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.diagnostic.YMessagesExtension
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SimpleDiagnostic
import org.jetbrains.kotlin.diagnostics.reportFromPlugin
import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
import org.jetbrains.kotlin.name.FqNameUnsafe
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPureClassOrObject
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

internal fun TranslationContext.reportError(
    declaration: KtPureClassOrObject,
    diagnosticFactory: DiagnosticFactory0<KtElement>
) {
    val diagnostic = SimpleDiagnostic(
        declaration.psiOrParent,
        diagnosticFactory,
        Severity.ERROR
    )

    bindingTrace()
        .reportFromPlugin(diagnostic, YMessagesExtension)
}