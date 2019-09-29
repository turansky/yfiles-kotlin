package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity.ERROR
import org.jetbrains.kotlin.psi.KtElement

internal val BASE_CLASS__INTERFACE_IMPLEMENTING_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val YOBJECT__INTERFACE_IMPLEMENTING_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)