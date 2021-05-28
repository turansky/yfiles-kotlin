package com.github.turansky.yfiles.ide.highlighter.markers

import com.github.turansky.yfiles.ide.js.baseClassUsed
import com.github.turansky.yfiles.ide.js.classFixTypeUsed
import com.github.turansky.yfiles.ide.psi.descriptor
import com.intellij.codeInsight.daemon.GutterIconDescriptor
import com.intellij.codeInsight.daemon.LineMarkerInfo
import com.intellij.codeInsight.daemon.LineMarkerProviderDescriptor
import com.intellij.openapi.editor.markup.GutterIconRenderer
import com.intellij.openapi.project.DumbService
import com.intellij.psi.PsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.project.platform
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
        result: MutableCollection<in LineMarkerInfo<*>>,
    ) {
        if (elements.isEmpty()) return
        if (LineMarkerOptions.allOptions.none { option -> option.isEnabled }) return

        val first = elements.first()
        if (DumbService.getInstance(first.project).isDumb || !ProjectRootsUtil.isInProjectOrLibSource(first)) return

        for (element in elements) {
            val klass = element as? KtClass ?: continue
            if (!klass.platform.isJs()) return

            createClassMarker(
                klass = klass,
                option = LineMarkerOptions.baseClassOption,
                check = ClassDescriptor::baseClassUsed,
                tooltipProvider = { "yFiles base class inside" }
            )?.also { result.add(it) }

            createClassMarker(
                klass = klass,
                option = LineMarkerOptions.classFixTypeOption,
                check = ClassDescriptor::classFixTypeUsed,
                tooltipProvider = { "yFiles type fix for YObject inheritor" }
            )?.also { result.add(it) }
        }
    }
}

private fun createClassMarker(
    klass: KtClass,
    option: GutterIconDescriptor.Option,
    check: (ClassDescriptor) -> Boolean,
    tooltipProvider: () -> String,
): LineMarkerInfo<*>? {
    if (!option.isEnabled) return null

    val descriptor = klass.descriptor
        ?: return null

    if (!check(descriptor)) {
        return null
    }

    val anchor = klass.nameIdentifier ?: klass

    @Suppress("DEPRECATION")
    return LineMarkerInfo(
        anchor,
        anchor.textRange,
        LineMarkerOptions.baseClassOption.icon,
        { tooltipProvider() },
        null,
        GutterIconRenderer.Alignment.RIGHT
    )
}
