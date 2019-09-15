package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity
import org.jetbrains.kotlin.psi.KtElement

val YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(Severity.ERROR)