package com.github.turansky.yfiles.ide.color

import com.intellij.psi.PsiElement

interface ColorProvider {
    fun get(element: PsiElement): ColorData?
}
