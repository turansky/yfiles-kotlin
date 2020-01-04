package com.github.turansky.yfiles

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.lexer.KtTokens.EXTERNAL_KEYWORD
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtModifierListOwner
import org.jetbrains.kotlin.psi.KtSuperTypeList
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

private const val YOBJECT = "yfiles.lang.YObject"

class InheritanceInspection : AbstractKotlinInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        if (!holder.project.platform.isJs()) {
            return PsiElementVisitor.EMPTY_VISITOR
        }

        return object : KtVisitorVoid() {
            override fun visitClass(klass: KtClass) {
                if (klass.isExternal()) {
                    return
                }

                klass.getSuperTypeList()
                    ?.takeIf { it.implementsExternalInterfaceDirectly() }
                    ?.takeIf { it.implementsYObject() }
                    ?: return

                holder.registerProblem(
                    klass,
                    "YObject inheritor detected!",
                    ProblemHighlightType.ERROR
                )
            }
        }
    }
}

private fun KtModifierListOwner.isExternal(): Boolean =
    hasModifier(EXTERNAL_KEYWORD)

private fun KtSuperTypeList.implementsExternalInterfaceDirectly(): Boolean =
    getChildrenOfType<KtClass>()
        .any { it.isExternal() && it.isInterface() }

private fun KtSuperTypeList.implementsYObject(): Boolean =
    anyDescendantOfType<KtClass> { it.name == YOBJECT }
