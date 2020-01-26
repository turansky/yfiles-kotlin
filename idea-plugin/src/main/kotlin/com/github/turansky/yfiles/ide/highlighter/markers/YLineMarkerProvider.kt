package com.github.turansky.yfiles.ide.highlighter.markers

import com.github.turansky.yfiles.ide.js.baseClassUsed
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.progress.ProgressManager
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.idea.util.ProjectRootsUtil
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.psi.KtClass

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

        for (element in elements) {
            ProgressManager.checkCanceled()

            val klass = element as? KtClass ?: continue
            if (!klass.platform.isJs()) return

            collectClassMarkers(klass, result)
        }
    }
}

private fun collectClassMarkers(
    klass: KtClass,
    result: MutableCollection<LineMarkerInfo<*>>
) {
    if (!LineMarkerOptions.baseClassOption.isEnabled) return

    val descriptor = klass.descriptor as? ClassDescriptor
        ?: return

    if (!descriptor.baseClassUsed) {
        return
    }

    val anchor = klass.nameIdentifier ?: klass

    val markerInfo = LineMarkerInfo(
        anchor,
        anchor.textRange,
        LineMarkerOptions.baseClassOption.icon,
        null,
        null,
        GutterIconRenderer.Alignment.RIGHT
    )

    result.add(markerInfo)
}
