package com.github.turansky.yfiles.compiler.backend.common

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
        // implement
    }

    private fun DeclarationCheckerContext.reportError(
        classOrObject: KtClassOrObject,
        diagnosticFactory: DiagnosticFactory0<KtClassOrObject>
    ) {
        trace.report(diagnosticFactory.on(classOrObject))
    }
}
