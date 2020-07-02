package com.github.turansky.yfiles.ide.inspections

import com.github.turansky.yfiles.ide.js.isYFilesInterface
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.EXTERNAL_INTERFACE_AS_REIFIED_TYPE_ARGUMENT
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtCallExpression
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.psi.KtTypeReference
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor
import org.jetbrains.kotlin.types.KotlinType

private val IS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    // TODO: use in IR
    /*
    CANNOT_CHECK_FOR_EXTERNAL_INTERFACE,
    USELESS_IS_CHECK
    */
)

private val AS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    // TODO: use in IR
    /*
    UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
    */
)

private val REIFIED_TYPE_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    EXTERNAL_INTERFACE_AS_REIFIED_TYPE_ARGUMENT
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
                    && bindingContext.isYFilesInterface(psiElement.typeReference)

            is KtBinaryExpressionWithTypeRHS
            -> factory in AS_FACTORIES
                    && bindingContext.isYFilesInterface(psiElement.right)

            is KtCallExpression,
            is KtTypeReference
            -> factory in REIFIED_TYPE_FACTORIES
                    && diagnostic.reifiedType.isYFilesInterface()

            else -> false
        }
    }
}

// HACK: for verification
@Suppress("UsePropertyAccessSyntax")
private val Diagnostic.reifiedType: KotlinType?
    get() = when (this) {
        is DiagnosticWithParameters1<*, *> -> getA() as? KotlinType
        else -> null
    }

private fun BindingContext.isYFilesInterface(
    typeReference: KtTypeReference?
): Boolean =
    get(BindingContext.TYPE, typeReference)
        .isYFilesInterface()

private fun KotlinType?.isYFilesInterface(): Boolean {
    this ?: return false

    val descriptor = constructor.declarationDescriptor as? ClassDescriptor
        ?: return false

    return descriptor.isYFilesInterface()
}
