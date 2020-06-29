package com.github.turansky.yfiles.compiler.diagnostic

import com.github.turansky.yfiles.compiler.backend.common.isYFilesInterface
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.diagnostics.Errors.USELESS_IS_CHECK
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.*
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor
import org.jetbrains.kotlin.types.KotlinType

private val IS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    CANNOT_CHECK_FOR_EXTERNAL_INTERFACE,
    USELESS_IS_CHECK
)

private val AS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
)

private val REIFIED_TYPE_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    EXTERNAL_INTERFACE_AS_REIFIED_TYPE_ARGUMENT
)

class YDiagnosticSuppressor : DiagnosticSuppressor {
    override fun isSuppressed(diagnostic: Diagnostic): Boolean = false

    override fun isSuppressed(diagnostic: Diagnostic, bindingContext: BindingContext?): Boolean {
        bindingContext ?: return false

        val psiElement = diagnostic.psiElement
        val factory = diagnostic.factory

        return when {
            psiElement is KtIsExpression && factory in IS_FACTORIES
            -> psiElement.typeReference.isYFilesInterface(bindingContext)

            psiElement is KtBinaryExpressionWithTypeRHS && factory in AS_FACTORIES
            -> psiElement.right.isYFilesInterface(bindingContext)

            // TODO: specify psi elements
            factory in REIFIED_TYPE_FACTORIES
            -> diagnostic.reifiedType.isYFilesInterface()

            else -> false
        }
    }
}

private val Diagnostic.reifiedType: KotlinType
    get() {
        this as DiagnosticWithParameters1<*, *>
        return a as KotlinType
    }

private fun KtTypeReference?.isYFilesInterface(
    context: BindingContext
): Boolean =
    context[BindingContext.TYPE, this]
        ?.isYFilesInterface()
        ?: false

private fun KotlinType.isYFilesInterface(): Boolean {
    val descriptor = constructor.declarationDescriptor as? ClassDescriptor
        ?: return false

    return descriptor.isYFilesInterface()
}
