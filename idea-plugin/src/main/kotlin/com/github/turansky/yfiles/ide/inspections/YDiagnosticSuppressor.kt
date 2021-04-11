package com.github.turansky.yfiles.ide.inspections

import com.github.turansky.yfiles.ide.js.isYEnum
import com.github.turansky.yfiles.ide.js.isYFiles
import com.github.turansky.yfiles.ide.js.isYFilesInterface
import com.github.turansky.yfiles.ide.js.locatedInYFilesPackage
import com.intellij.psi.PsiElement
import com.intellij.psi.impl.source.tree.LeafPsiElement
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.diagnostics.Diagnostic
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters1
import org.jetbrains.kotlin.diagnostics.DiagnosticWithParameters2
import org.jetbrains.kotlin.diagnostics.Errors.NOTHING_TO_INLINE
import org.jetbrains.kotlin.diagnostics.Errors.WRONG_MODIFIER_CONTAINING_DECLARATION
import org.jetbrains.kotlin.js.resolve.diagnostics.ErrorsJs.*
import org.jetbrains.kotlin.lexer.KtModifierKeywordToken
import org.jetbrains.kotlin.psi.*
import org.jetbrains.kotlin.resolve.BindingContext
import org.jetbrains.kotlin.resolve.diagnostics.DiagnosticSuppressor
import org.jetbrains.kotlin.types.KotlinType

private const val EXTERNAL_PRIVATE_CONSTRUCTOR = "private member of class"
private const val EXTERNAL_EXTENSION_FUNCTION = "extension function"

class YDiagnosticSuppressor : DiagnosticSuppressor {
    override fun isSuppressed(
        diagnostic: Diagnostic,
    ): Boolean =
        false

    override fun isSuppressed(
        diagnostic: Diagnostic,
        bindingContext: BindingContext?,
    ): Boolean {
        bindingContext ?: return false

        val psiElement = diagnostic.psiElement
        val factory = diagnostic.factory

        return when (factory) {
            // TODO: use in IR
            /*
            CANNOT_CHECK_FOR_EXTERNAL_INTERFACE,
            USELESS_IS_CHECK
            -> psiElement is KtIsExpression
                    && psiElement.typeReference.isYFilesInterface(bindingContext)

            UNCHECKED_CAST_TO_EXTERNAL_INTERFACE
            -> psiElement is KtBinaryExpressionWithTypeRHS
                    && psiElement.right.isYFilesInterface(bindingContext)
             */

            EXTERNAL_INTERFACE_AS_REIFIED_TYPE_ARGUMENT,
            -> psiElement is KtCallExpression || psiElement is KtTypeReference
                    && diagnostic.reifiedType.isYFilesInterface()

            WRONG_EXTERNAL_DECLARATION,
            -> when (psiElement) {
                is KtPrimaryConstructor,
                -> diagnostic.messageParameter == EXTERNAL_PRIVATE_CONSTRUCTOR
                        && psiElement.isYFilesConstructor(bindingContext)

                is KtNamedFunction,
                -> diagnostic.messageParameter == EXTERNAL_EXTENSION_FUNCTION
                        && psiElement.locatedInYFilesObject

                else -> false
            }

            EXTERNAL_CLASS_CONSTRUCTOR_PROPERTY_PARAMETER,
            -> psiElement is KtParameter
                    && psiElement.isYFilesConstructorParameter(bindingContext)

            NON_ABSTRACT_MEMBER_OF_EXTERNAL_INTERFACE,
            -> psiElement is KtCallableDeclaration
                    && psiElement.isYFilesInterfaceMember(bindingContext)

            WRONG_MODIFIER_CONTAINING_DECLARATION,
            -> psiElement is LeafPsiElement
                    && diagnostic.keywordToken == "final"
                    && psiElement.parentDeclaration?.isYFilesInterfaceMember(bindingContext) ?: false

            NESTED_CLASS_IN_EXTERNAL_INTERFACE,
            -> psiElement is KtObjectDeclaration
                    && psiElement.isYFilesInterfaceCompanion(bindingContext)

            // TODO: check type parameter
            NON_EXTERNAL_DECLARATION_IN_INAPPROPRIATE_FILE,
            -> psiElement.isYFilesExtension()

            NOTHING_TO_INLINE,
            -> psiElement.isYFilesExtensionModifier()

            else -> false
        }
    }
}

private val Diagnostic.reifiedType: KotlinType?
    get() = when (this) {
        is DiagnosticWithParameters1<*, *> -> a as? KotlinType
        else -> null
    }

private val Diagnostic.messageParameter: String?
    get() = when (this) {
        is DiagnosticWithParameters1<*, *> -> a as? String
        else -> null
    }

private val Diagnostic.keywordToken: String?
    get() = when (this) {
        is DiagnosticWithParameters2<*, *, *> -> (a as? KtModifierKeywordToken)?.value
        else -> null
    }

private val LeafPsiElement.parentDeclaration: KtCallableDeclaration?
    get() = parent?.parent as? KtCallableDeclaration

private fun KtTypeReference?.isYFilesInterface(
    context: BindingContext,
): Boolean =
    context[BindingContext.TYPE, this]
        .isYFilesInterface()

private fun KotlinType?.isYFilesInterface(): Boolean {
    this ?: return false

    val descriptor = constructor.declarationDescriptor as? ClassDescriptor
        ?: return false

    return descriptor.isYFilesInterface()
}

private fun KtPrimaryConstructor.isYFilesConstructor(
    context: BindingContext,
): Boolean {
    val descriptor = context[BindingContext.CLASS, parent] ?: return false
    return descriptor.locatedInYFilesPackage
}

private fun KtParameter.isYFilesConstructorParameter(
    context: BindingContext,
): Boolean {
    val constructor = parent?.parent as? KtPrimaryConstructor ?: return false
    return constructor.isYFilesConstructor(context)
}

private fun KtCallableDeclaration.isYFilesInterfaceMember(
    context: BindingContext,
): Boolean {
    if (this !is KtProperty && this !is KtNamedFunction)
        return false

    val descriptor = context[BindingContext.CLASS, parent?.parent] ?: return false
    return descriptor.isYFilesInterface()
}

private fun KtObjectDeclaration.isYFilesInterfaceCompanion(
    context: BindingContext,
): Boolean {
    if (!isCompanion()) return false
    val descriptor = context[BindingContext.CLASS, parent?.parent] ?: return false
    return descriptor.isYFilesInterface() || descriptor.isYEnum
}

private fun PsiElement.isYFilesExtension(): Boolean {
    val declaration: KtCallableDeclaration? = when (this) {
        is KtProperty -> this
        is KtPropertyAccessor -> parent as? KtProperty

        is KtNamedFunction -> this

        else -> null
    }

    return declaration.locatedInYFilesFile
}

private fun PsiElement.isYFilesExtensionModifier(): Boolean =
    (this as? LeafPsiElement)
        ?.parentDeclaration
        .locatedInYFilesFile

private val KtCallableDeclaration?.locatedInYFilesFile: Boolean
    get() {
        val file = this?.parent as? KtFile
            ?: return false

        return file.packageFqName.isYFiles
    }

private val KtNamedFunction.locatedInYFilesObject: Boolean
    get() {
        val parentObject = this.parent.parent as? KtObjectDeclaration
            ?: return false

        return parentObject.fqName?.isYFiles ?: false
    }
