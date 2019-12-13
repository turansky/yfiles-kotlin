package com.github.turansky.yfiles

import com.intellij.codeInspection.InspectionManager
import com.intellij.codeInspection.ProblemDescriptor
import com.intellij.psi.PsiFile
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection

class InheritanceInspection : AbstractKotlinInspection() {
    override fun checkFile(
        file: PsiFile,
        manager: InspectionManager,
        isOnTheFly: Boolean
    ): Array<ProblemDescriptor>? {
        return ProblemDescriptor.EMPTY_ARRAY
    }
}
