package com.github.turansky.yfiles.compiler.backend.common

import com.github.turansky.yfiles.compiler.diagnostic.BaseClassErrors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext

object YDeclarationChecker : DeclarationChecker {
    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext
    ) {
        if (declaration !is KtClassOrObject) return
        if (descriptor !is ClassDescriptor) return
        if (descriptor.isExternal) return

        when (descriptor.kind) {
            ClassKind.CLASS
            -> context.checkClass(declaration, descriptor)

            ClassKind.OBJECT,
            ClassKind.INTERFACE,
            ClassKind.ENUM_CLASS
            -> context.checkInterfaces(declaration, descriptor)

            else -> {
                // do nothing
            }
        }
    }

    private fun DeclarationCheckerContext.checkClass(
        declaration: KtClassOrObject,
        descriptor: ClassDescriptor
    ) {
        // implement
    }

    private fun DeclarationCheckerContext.checkInterfaces(
        declaration: KtClassOrObject,
        descriptor: ClassDescriptor
    ) {
        if (descriptor.implementsYFilesInterface) {
            reportError(declaration, BaseClassErrors.INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
        }
    }

    private fun DeclarationCheckerContext.reportError(
        declaration: KtClassOrObject,
        diagnosticFactory: DiagnosticFactory0<KtClassOrObject>
    ) {
        trace.report(diagnosticFactory.on(declaration))
    }
}
