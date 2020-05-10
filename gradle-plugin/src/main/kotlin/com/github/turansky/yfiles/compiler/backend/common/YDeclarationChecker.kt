package com.github.turansky.yfiles.compiler.backend.common

import com.github.turansky.yfiles.compiler.diagnostic.BaseClassErrors
import com.github.turansky.yfiles.compiler.diagnostic.YObjectErrors
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.descriptors.DeclarationDescriptor
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtDeclaration
import org.jetbrains.kotlin.resolve.checkers.DeclarationChecker
import org.jetbrains.kotlin.resolve.checkers.DeclarationCheckerContext
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

internal object YDeclarationChecker : DeclarationChecker {
    private val TARGET_KINDS = setOf(
        ClassKind.CLASS,
        ClassKind.OBJECT,
        ClassKind.INTERFACE,
        ClassKind.ENUM_CLASS
    )

    override fun check(
        declaration: KtDeclaration,
        descriptor: DeclarationDescriptor,
        context: DeclarationCheckerContext
    ) {
        if (declaration !is KtClassOrObject) return
        if (descriptor !is ClassDescriptor) return
        if (descriptor.isExternal) return
        if (descriptor.kind !in TARGET_KINDS) return
        if (!descriptor.implementsYFilesInterface) return

        when {
            descriptor.kind != ClassKind.CLASS
            -> context.reportError(declaration, BaseClassErrors.INTERFACE_IMPLEMENTING_NOT_SUPPORTED)

            descriptor.implementsYObjectDirectly
            -> context.checkCustomYObject(declaration, descriptor)

            else -> context.checkBaseClass(declaration, descriptor)
        }
    }

    private fun DeclarationCheckerContext.checkBaseClass(
        declaration: KtClassOrObject,
        descriptor: ClassDescriptor
    ) {
        when {
            descriptor.isInline
            -> reportError(declaration, BaseClassErrors.INLINE_CLASS_NOT_SUPPORTED)

            descriptor.getSuperInterfaces().any { !it.isYFilesInterface() }
            -> reportError(declaration, BaseClassErrors.INTERFACE_MIXING_NOT_SUPPORTED)
        }
    }

    private fun DeclarationCheckerContext.checkCustomYObject(
        declaration: KtClassOrObject,
        descriptor: ClassDescriptor
    ) {
        if (descriptor.getSuperInterfaces().size != 1) {
            reportError(declaration, YObjectErrors.INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
        }
    }

    private fun DeclarationCheckerContext.reportError(
        declaration: KtClassOrObject,
        diagnosticFactory: DiagnosticFactory0<KtClassOrObject>
    ) {
        trace.report(diagnosticFactory.on(declaration))
    }
}
