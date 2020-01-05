package com.github.turansky.yfiles.ide

import com.intellij.codeInspection.ProblemHighlightType
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.*
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtFile
import org.jetbrains.kotlin.psi.KtVisitorVoid
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperClassNotAny
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

class InheritanceInspection : AbstractKotlinInspection() {
    override fun buildVisitor(
        holder: ProblemsHolder,
        isOnTheFly: Boolean
    ): PsiElementVisitor {
        return YVisitor(holder)
    }
}

private class YVisitor(
    private val holder: ProblemsHolder
) : KtVisitorVoid() {
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

        when (descriptor.kind) {
            CLASS -> visitClass(klass, descriptor)

            OBJECT,
            INTERFACE,
            ENUM_CLASS -> checkInterfaces(klass, descriptor)

            else -> {
                // do nothing
            }
        }
    }

    private fun visitClass(
        klass: KtClass,
        descriptor: ClassDescriptor
    ) {
        if (descriptor.implementsYObjectDirectly) {
            if (descriptor.getSuperClassNotAny() != null) {
                registerSuperTypesError(klass, "YObject direct inheritor couldn't have super class")
            }

            if (descriptor.getSuperInterfaces().size != 1) {
                registerSuperTypesError(klass, "YObject direct inheritor couldn't implement another interfaces")
            }
        }
    }

    private fun checkInterfaces(
        klass: KtClass,
        descriptor: ClassDescriptor
    ) {
        if (descriptor.implementsYFilesInterface) {
            registerSuperTypesError(klass, "yFiles interface implementing supported only for ordinal classes")
        }
    }

    private fun registerSuperTypesError(
        klass: KtClass,
        message: String
    ) {
        holder.registerProblem(
            requireNotNull(klass.getSuperTypeList()),
            message,
            ProblemHighlightType.GENERIC_ERROR
        )
    }
}
