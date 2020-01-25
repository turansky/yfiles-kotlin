package com.github.turansky.yfiles.ide.highlighter.markers

import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil

class YLineMarkerProvider : LineMarkerProviderDescriptor() {
    override fun getName() = "yFiles line markers"

    override fun getOptions(): Array<Option> =
        LineMarkerOptions.allOptions

    override fun getLineMarkerInfo(element: PsiElement): LineMarkerInfo<PsiElement>? = null

    override fun collectSlowLineMarkers(
        elements: List<PsiElement>,
        result: MutableCollection<LineMarkerInfo<*>>
    ) {
        if (elements.isEmpty()) return
        if (LineMarkerOptions.allOptions.none { option -> option.isEnabled }) return

        val first = elements.first()
        if (DumbService.getInstance(first.project).isDumb || !ProjectRootsUtil.isInProjectOrLibSource(first)) return
    }
}
