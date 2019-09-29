package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity.ERROR
import org.jetbrains.kotlin.psi.KtElement

internal val BASE_CLASS__INTERFACE_IMPLEMENTING_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val BASE_CLASS__INTERFACE_MIXING_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val BASE_CLASS__CONSTRUCTOR_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val BASE_CLASS__BODY_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val BASE_CLASS__INLINE_CLASS_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val BASE_CLASS__DATA_CLASS_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val BASE_CLASS__COMPANION_OBJECT_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val YOBJECT__INTERFACE_IMPLEMENTING_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)

internal val YOBJECT__COMPANION_OBJECT_NOT_SUPPORTED: DiagnosticFactory0<KtElement> =
    DiagnosticFactory0.create(ERROR)
