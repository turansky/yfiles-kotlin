package com.github.turansky.yfiles

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.lexer.KtTokens.EXTERNAL_KEYWORD
import org.jetbrains.kotlin.name.FqName
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.psi.psiUtil.anyDescendantOfType
import org.jetbrains.kotlin.psi.psiUtil.getChildrenOfType

private val YOBJECT = FqName("yfiles.lang.YObject")

class InheritanceInspection : AbstractKotlinInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return object : KtVisitorVoid() {
            override fun visitKtFile(file: KtFile) {
                if (file.platform.isJs()) {
                    super.visitKtFile(file)
                }
            }

            override fun visitClass(klass: KtClass) {
                if (klass.isExternal()) {
                    return
                }

                val superTypes = klass.getSuperTypeList()
                    ?.takeIf { it.implementsExternalInterfaceDirectly() }
                    ?.takeIf { it.implementsYObject() }
                    ?: return

                holder.registerProblem(
                    superTypes,
                    "YObject inheritor detected!",
                    ProblemHighlightType.GENERIC_ERROR
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
    anyDescendantOfType<KtClass> { it.fqName == YOBJECT }
