package com.github.turansky.yfiles.compiler.diagnostic

import com.github.turansky.yfiles.compiler.backend.common.isYEnum
import com.github.turansky.yfiles.compiler.backend.common.isYFilesInterface
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.diagnostics.Errors.USELESS_IS_CHECK
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.*
import org.jetbrains.kotlin.psi.*
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

class YDiagnosticSuppressor : DiagnosticSuppressor {
    override fun isSuppressed(
        diagnostic: Diagnostic
    ): Boolean =
        false

    override fun isSuppressed(
        diagnostic: Diagnostic,
        bindingContext: BindingContext?
    ): Boolean {
        bindingContext ?: return false

        val psiElement = diagnostic.psiElement
        val factory = diagnostic.factory

        return when (psiElement) {
            is KtIsExpression
            -> factory in IS_FACTORIES
                    && psiElement.typeReference.isYFilesInterface(bindingContext)

            is KtBinaryExpressionWithTypeRHS
            -> factory in AS_FACTORIES
                    && psiElement.right.isYFilesInterface(bindingContext)

            is KtCallExpression,
            is KtTypeReference
            -> factory === EXTERNAL_INTERFACE_AS_REIFIED_TYPE_ARGUMENT
                    && diagnostic.reifiedType.isYFilesInterface()

            is KtObjectDeclaration
            -> factory === NESTED_CLASS_IN_EXTERNAL_INTERFACE
                    && psiElement.isYFilesInterfaceCompanion(bindingContext)

            is KtClass
            -> factory === WRONG_EXTERNAL_DECLARATION
                    && diagnostic.funInterfaceCheck
                    && psiElement.isYFilesInterface(bindingContext)

            else -> false
        }
    }
}

private val Diagnostic.reifiedType: KotlinType?
    get() = when (this) {
        is DiagnosticWithParameters1<*, *> -> a as? KotlinType
        else -> null
    }

private val Diagnostic.funInterfaceCheck: Boolean
    get() = this is DiagnosticWithParameters1<*, *>
            && a == "fun interface"

private fun KtTypeReference?.isYFilesInterface(
    context: BindingContext
): Boolean =
    context[BindingContext.TYPE, this]
        .isYFilesInterface()

private fun KtClass.isYFilesInterface(
    context: BindingContext
): Boolean {
    val descriptor = context[BindingContext.CLASS, this] ?: return false
    return descriptor.isYFilesInterface()
}

private fun KotlinType?.isYFilesInterface(): Boolean {
    this ?: return false

    val descriptor = constructor.declarationDescriptor as? ClassDescriptor
        ?: return false

    return descriptor.isYFilesInterface()
}

private fun KtObjectDeclaration.isYFilesInterfaceCompanion(
    context: BindingContext
): Boolean {
    if (!isCompanion()) return false
    val descriptor = context[BindingContext.CLASS, parent?.parent] ?: return false
    return descriptor.isYFilesInterface() || descriptor.isYEnum
}
