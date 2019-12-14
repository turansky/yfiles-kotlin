package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.diagnostic.YMessagesExtension
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.diagnostics.SimpleDiagnostic
import org.jetbrains.kotlin.diagnostics.reportFromPlugin
import org.jetbrains.kotlin.js.backend.ast.JsBlock
import org.jetbrains.kotlin.js.backend.ast.JsExpression
import org.jetbrains.kotlin.js.backend.ast.JsFunction
import org.jetbrains.kotlin.js.backend.ast.JsStatement
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.name.Name
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPureClassOrObject
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

internal fun <T : KtElement> TranslationContext.reportError(
    element: T,
    diagnosticFactory: DiagnosticFactory0<T>
) {
    val diagnostic = SimpleDiagnostic(
        element,
        diagnosticFactory,
        Severity.ERROR
    )

    bindingTrace()
        .reportFromPlugin(diagnostic, YMessagesExtension)
}

internal fun TranslationContext.jsFunction(
    description: String,
    vararg statements: JsStatement
): JsFunction =
    JsFunction(
        scope(),
        JsBlock(*statements),
        description
    )

internal fun TranslationContext.declareConstantValue(
    suggestedName: String,
    value: JsExpression
): JsExpression =
    declareConstantValue(
        suggestedName,
        suggestedName,
        value,
        null
    )

internal fun TranslationContext.reportError(
    declaration: KtPureClassOrObject,
    diagnosticFactory: DiagnosticFactory0<KtClassOrObject>
) {
    reportError(
        element = declaration.psiOrParent as KtClassOrObject,
        diagnosticFactory = diagnosticFactory
    )
}
