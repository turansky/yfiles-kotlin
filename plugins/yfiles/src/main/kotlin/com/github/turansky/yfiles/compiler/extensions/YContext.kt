package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.diagnostic.YMessagesExtension
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SimpleDiagnostic
import org.jetbrains.kotlin.diagnostics.reportFromPlugin
import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPureElement
import org.jetbrains.kotlin.resolve.DescriptorUtils.getFunctionByName

internal fun TranslationContext.toValueReference(descriptor: DeclarationDescriptor): JsExpression =
    translateAsValueReference(descriptor, this)

internal fun TranslationContext.findFunction(
    packageName: FqName,
    functionName: Name
): JsExpression {
    val descriptor = getFunctionByName(
        currentModule.getPackage(packageName).memberScope,
        functionName
    )
    return toValueReference(descriptor)
}

internal fun TranslationContext.reportError(
    element: KtElement,
    diagnosticFactory: DiagnosticFactory0<KtElement>
) {
    val diagnostic = SimpleDiagnostic(
        element,
        diagnosticFactory,
        Severity.ERROR
    )

    bindingTrace()
        .reportFromPlugin(diagnostic, YMessagesExtension)
}

internal fun TranslationContext.reportError(
    declaration: KtPureElement,
    diagnosticFactory: DiagnosticFactory0<KtElement>
) = reportError(declaration.psiOrParent, diagnosticFactory)