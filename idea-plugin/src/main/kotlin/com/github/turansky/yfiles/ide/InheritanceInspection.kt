package com.github.turansky.yfiles.ide

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtVisitorVoid

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
                val descriptor = klass.descriptor as? ClassDescriptor
                    ?: return

                if (descriptor.isExternal) {
                    return
                }

                val superTypeList = klass.getSuperTypeList()
                    ?: return

                holder.registerProblem(
                    superTypeList,
                    "YObject inheritor detected!",
                    ProblemHighlightType.GENERIC_ERROR
                )
            }
        }
    }
}
