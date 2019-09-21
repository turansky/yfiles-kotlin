package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.diagnostic.YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED
import com.github.turansky.yfiles.compiler.diagnostic.YMessagesExtension
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity.ERROR
import org.jetbrains.kotlin.diagnostics.SimpleDiagnostic
import org.jetbrains.kotlin.diagnostics.reportFromPlugin
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.psi.KtElement
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private val YFILES_PACKAGE = identifier("yfiles")

private val AFFECTED_CLASS_KINDS = setOf(
    ClassKind.CLASS,
    ClassKind.OBJECT
)

class YExtension : JsSyntheticTranslateExtension {
    override fun generateClassSyntheticParts(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        translator: DeclarationBodyVisitor,
        context: TranslationContext
    ) {
        if (descriptor.kind !in AFFECTED_CLASS_KINDS) {
            return
        }

        if (descriptor.isExternal) {
            return
        }

        val superInterfaces = descriptor.getSuperInterfaces()
        if (superInterfaces.isEmpty()) {
            return
        }

        val externalSuperInterfaces = superInterfaces
            .filter { it.isExternal }

        if (externalSuperInterfaces.isEmpty()) {
            return
        }

        val yfilesInterfaces = externalSuperInterfaces
            .filter { it.companionObjectDescriptor != null }
            .mapNotNull { it.fqNameOrNull() }
            .filterNot { it.isRoot }
            .filter { it.pathSegments().first() == YFILES_PACKAGE }
            .toList()

        if (yfilesInterfaces.isEmpty()) {
            return
        }

        reportError(declaration, context, YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
    }
}

private fun reportError(
    declaration: KtPureClassOrObject,
    context: TranslationContext,
    diagnosticFactory: DiagnosticFactory0<KtElement>
) {
    val diagnostic = SimpleDiagnostic(
        declaration.psiOrParent,
        diagnosticFactory,
        ERROR
    )

    context.bindingTrace()
        .reportFromPlugin(diagnostic, YMessagesExtension)
}