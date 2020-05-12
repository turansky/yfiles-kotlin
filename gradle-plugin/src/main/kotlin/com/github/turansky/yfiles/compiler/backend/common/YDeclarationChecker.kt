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

        descriptor.check {
            context.trace.report(it.on(declaration))
        }
    }

    private fun ClassDescriptor.check(
        reportError: (DiagnosticFactory0<KtClassOrObject>) -> Unit
    ) {
        when {
            kind != ClassKind.CLASS
            -> reportError(BaseClassErrors.INTERFACE_IMPLEMENTING_NOT_SUPPORTED)

            isInline
            -> reportError(BaseClassErrors.INLINE_CLASS_NOT_SUPPORTED)

            getSuperInterfaces().size == 1
            -> return

            implementsYObjectDirectly
            -> reportError(YObjectErrors.INTERFACE_IMPLEMENTING_NOT_SUPPORTED)

            getSuperInterfaces().any { !it.isYFilesInterface() }
            -> reportError(BaseClassErrors.INTERFACE_MIXING_NOT_SUPPORTED)
        }
    }
}
