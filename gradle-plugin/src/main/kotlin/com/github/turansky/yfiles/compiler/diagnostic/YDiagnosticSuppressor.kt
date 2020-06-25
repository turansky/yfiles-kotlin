package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticFactory
import org.jetbrains.kotlin.diagnostics.Errors.USELESS_IS_CHECK
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.CANNOT_CHECK_FOR_EXTERNAL_INTERFACE
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor

class YDiagnosticSuppressor : DiagnosticSuppressor {
    private val suppresssedFactories: Set<DiagnosticFactory<*>> = setOf(
        CANNOT_CHECK_FOR_EXTERNAL_INTERFACE,
        UNCHECKED_CAST_TO_EXTERNAL_INTERFACE,
        USELESS_IS_CHECK
    )

    override fun isSuppressed(diagnostic: Diagnostic): Boolean {
        return diagnostic.factory in suppresssedFactories
    }
}
