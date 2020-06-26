package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Errors.USELESS_IS_CHECK
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.CANNOT_CHECK_FOR_EXTERNAL_INTERFACE
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor
import org.jetbrains.kotlin.types.KotlinType
import org.jetbrains.kotlin.types.typeUtil.isInterface

private val IS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    CANNOT_CHECK_FOR_EXTERNAL_INTERFACE,
    USELESS_IS_CHECK
)

private val AS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
)

class YDiagnosticSuppressor : DiagnosticSuppressor {
    override fun isSuppressed(diagnostic: Diagnostic): Boolean = false

    override fun isSuppressed(diagnostic: Diagnostic, bindingContext: BindingContext?): Boolean {
        bindingContext ?: return false

        val psiElement = diagnostic.psiElement
        val factory = diagnostic.factory

        val typeReference = when {
            psiElement is KtIsExpression && factory in IS_FACTORIES
            -> psiElement.typeReference

            psiElement is KtBinaryExpressionWithTypeRHS && factory in AS_FACTORIES
            -> psiElement.right

            else -> return false
        }

        val type = bindingContext[BindingContext.TYPE, typeReference]
            ?: return false

        return type.isInterface() && type.isExternal()
    }
}

private fun KotlinType.isExternal(): Boolean =
    (constructor.declarationDescriptor as? ClassDescriptor)?.isExternal == true
