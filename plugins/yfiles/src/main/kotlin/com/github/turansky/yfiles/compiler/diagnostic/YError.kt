package com.github.turansky.yfiles.compiler.diagnostic

import org.jetbrains.kotlin.diagnostics.DiagnosticFactory0
import org.jetbrains.kotlin.diagnostics.Severity.ERROR
import org.jetbrains.kotlin.psi.KtClassOrObject
import org.jetbrains.kotlin.psi.KtElement

private fun <T : KtElement> errorDiagnosticFactory(): DiagnosticFactory0<T> =
    DiagnosticFactory0.create(ERROR)

internal val BASE_CLASS__INTERFACE_IMPLEMENTING_NOT_SUPPORTED: DiagnosticFactory0<KtClassOrObject> =
    errorDiagnosticFactory()

internal val BASE_CLASS__INTERFACE_MIXING_NOT_SUPPORTED: DiagnosticFactory0<KtClassOrObject> =
    errorDiagnosticFactory()

internal val BASE_CLASS__INLINE_CLASS_NOT_SUPPORTED: DiagnosticFactory0<KtClassOrObject> =
    errorDiagnosticFactory()

internal val BASE_CLASS__DATA_CLASS_NOT_SUPPORTED: DiagnosticFactory0<KtClassOrObject> =
    errorDiagnosticFactory()

internal val BASE_CLASS__COMPANION_OBJECT_NOT_SUPPORTED: DiagnosticFactory0<KtClassOrObject> =
    errorDiagnosticFactory()


internal val YOBJECT__INTERFACE_IMPLEMENTING_NOT_SUPPORTED: DiagnosticFactory0<KtClassOrObject> =
    errorDiagnosticFactory()

internal val YOBJECT__COMPANION_OBJECT_NOT_SUPPORTED: DiagnosticFactory0<KtClassOrObject> =
    errorDiagnosticFactory()
