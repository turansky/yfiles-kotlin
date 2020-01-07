package com.github.turansky.yfiles.ide

import com.intellij.codeInspection.ProblemHighlightType.GENERIC_ERROR
import com.intellij.codeInspection.ProblemsHolder
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiElementVisitor
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.*
import org.jetbrains.kotlin.idea.inspections.AbstractKotlinInspection
import org.jetbrains.kotlin.idea.project.platform
import org.jetbrains.kotlin.idea.search.usagesSearch.descriptor
import org.jetbrains.kotlin.lexer.KtTokens.INLINE_KEYWORD
import org.jetbrains.kotlin.platform.js.isJs
import org.jetbrains.kotlin.psi.KtClass
import org.jetbrains.kotlin.psi.KtClassOrObject
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

    override fun visitClassOrObject(classOrObject: KtClassOrObject) {
        val descriptor = classOrObject.descriptor as? ClassDescriptor
            ?: return

        if (descriptor.isExternal) {
            return
        }

        when (descriptor.kind) {
            CLASS -> visitClass(classOrObject as KtClass, descriptor)

            OBJECT,
            INTERFACE,
            ENUM_CLASS -> checkInterfaces(classOrObject, descriptor)

            else -> {
                // do nothing
            }
        }
    }

    private fun visitClass(
        klass: KtClass,
        descriptor: ClassDescriptor
    ) {
        if (!descriptor.implementsYFilesInterface) {
            return
        }

        klass.inlineModifier?.also {
            registerProblem(it, "Unexpected modifier for yFiles class")
        }

        if (descriptor.implementsYObjectDirectly) {
            if (descriptor.getSuperClassNotAny() != null || descriptor.getSuperInterfaces().size != 1) {
                registerSuperTypesError(klass, "YObject expected as single super type")
            }
        } else {
            if (descriptor.getSuperInterfaces().any { !it.isYFilesInterface() }) {
                registerSuperTypesError(klass, "Only yFiles interfaces expected")
            }
        }
    }

    private fun checkInterfaces(
        classOrObject: KtClassOrObject,
        descriptor: ClassDescriptor
    ) {
        if (descriptor.implementsYFilesInterface) {
            val keyword = classOrObject.getDeclarationKeyword() ?: return
            registerProblem(keyword, "Ordinary class expected by yFiles interface(s)")
        }
    }

    private fun registerProblem(
        psiElement: PsiElement,
        message: String
    ) {
        holder.registerProblem(
            psiElement,
            message,
            GENERIC_ERROR
        )
    }

    private fun registerSuperTypesError(
        classOrObject: KtClassOrObject,
        message: String
    ) {
        val superTypeList = classOrObject.getSuperTypeList()
            ?: return

        registerProblem(superTypeList, message)
    }
}

private val KtClass.inlineModifier: PsiElement?
    get() = modifierList?.getModifier(INLINE_KEYWORD)
