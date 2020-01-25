package com.github.turansky.yfiles.ide

import com.intellij.codeInsight.daemon.GutterIconDescriptor

internal object LineMarkerOptions {
    val baseClassOption = GutterIconDescriptor.Option(
        "yfiles.base.class",
        "BaseClass inside",
        Icons.Gutter.BASE_CLASS
    )

    val classFixTypeOption = GutterIconDescriptor.Option(
        "yfiles.class.fix.type",
        "Type fix activated",
        Icons.Gutter.CLASS_FIX_TYPE
    )

    val allOptions = arrayOf(
        baseClassOption,
        classFixTypeOption
    )
}
