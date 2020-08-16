package com.github.turansky.yfiles.ide.documentation

import com.intellij.psi.PsiFile

private const val SVG_EXTENSION = "svg"

internal val PsiFile.isSvgFile: Boolean
    get() = fileType.defaultExtension.toLowerCase() == SVG_EXTENSION
