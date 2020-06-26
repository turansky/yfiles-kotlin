package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Errors.USELESS_IS_CHECK
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.CANNOT_CHECK_FOR_EXTERNAL_INTERFACE
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
import org.jetbrains.kotlin.psi.KtBinaryExpressionWithTypeRHS
import org.jetbrains.kotlin.psi.KtIsExpression
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor

private val IS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    CANNOT_CHECK_FOR_EXTERNAL_INTERFACE,
    USELESS_IS_CHECK
)

private val AS_FACTORIES: Set<DiagnosticFactory<*>> = setOf(
    UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
)

class YDiagnosticSuppressor : DiagnosticSuppressor {
    override fun isSuppressed(diagnostic: Diagnostic): Boolean {
        val factory = diagnostic.factory
        val psiElement = diagnostic.psiElement

        return when (psiElement) {
            is KtIsExpression
            -> factory in IS_FACTORIES

            is KtBinaryExpressionWithTypeRHS
            -> factory in AS_FACTORIES

            else -> false
        }
    }
}
