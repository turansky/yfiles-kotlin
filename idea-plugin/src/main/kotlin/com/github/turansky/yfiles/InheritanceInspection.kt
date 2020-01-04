package com.github.turansky.yfiles

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.lexer.KtTokens.EXTERNAL_KEYWORD
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

private const val YOBJECT = "yfiles.lang.YObject"

class InheritanceInspection : AbstractKotlinInspection() {
    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        if (!manager.project.platform.isJs()) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        val problems = file.getChildrenOfType<KtClass>()
            .mapNotNull { checkClass(it) }

        if (problems.isEmpty()) {
            return ProblemDescriptor.EMPTY_ARRAY
        }

        return problems.toTypedArray()
    }

    private fun checkClass(declaration: KtClass): ProblemDescriptor? {
        if (declaration.isExternal()) {
            return null
        }

        val superTypes = declaration.getSuperTypeList()
            ?.takeIf { it.implementsExternalInterfaceDirectly() }
            ?: return null

        if (superTypes.implementsYObject()) {
            return null
        }

        return null
    }
}

private fun KtModifierListOwner.isExternal(): Boolean =
    hasModifier(EXTERNAL_KEYWORD)

private fun KtSuperTypeList.implementsExternalInterfaceDirectly(): Boolean =
    getChildrenOfType<KtClass>()
        .any { it.isExternal() && it.isInterface() }

private fun KtSuperTypeList.implementsYObject(): Boolean =
    anyDescendantOfType<KtClass> { it.name == YOBJECT }
