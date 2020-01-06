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
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.lexer.KtTokens.DATA_KEYWORD
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

        when {
            descriptor.isInline -> registerModifierProblem(klass, INLINE_KEYWORD, "yFiles interface implementing not supported for inline classes")
            descriptor.isData -> registerModifierProblem(klass, DATA_KEYWORD, "yFiles interface implementing not supported for data classes")
        }

        if (descriptor.implementsYObjectDirectly) {
            if (descriptor.getSuperClassNotAny() != null) {
                registerSuperTypesError(klass, "YObject direct inheritor couldn't have super class")
            }

            if (descriptor.getSuperInterfaces().size != 1) {
                registerSuperTypesError(klass, "YObject direct inheritor couldn't implement another interfaces")
            }
        } else {
            if (descriptor.getSuperInterfaces().any { !it.isYFilesInterface() }) {
                registerSuperTypesError(klass, "yFiles interfaces could't be mixed with non-yFiles interfaces")
            }
        }
    }

    private fun checkInterfaces(
        classOrObject: KtClassOrObject,
        descriptor: ClassDescriptor
    ) {
        if (descriptor.implementsYFilesInterface) {
            registerSuperTypesError(classOrObject, "yFiles interface implementing supported only for ordinal classes")
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

    private fun registerModifierProblem(
        klass: KtClass,
        tokenType: KtModifierKeywordToken,
        message: String
    ) {
        val modifier = klass.modifierList
            ?.getModifier(tokenType)
            ?: return

        registerProblem(modifier, message)
    }
}
