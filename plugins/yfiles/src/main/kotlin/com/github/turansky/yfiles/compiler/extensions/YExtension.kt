package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private val AFFECTED_CLASS_KINDS = setOf(
    ClassKind.CLASS,
    ClassKind.OBJECT
)

class YExtension : JsSyntheticTranslateExtension {
    override fun generateClassSyntheticParts(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        translator: DeclarationBodyVisitor,
        context: TranslationContext
    ) {
        if (descriptor.kind !in AFFECTED_CLASS_KINDS) {
            return
        }

        if (descriptor.isExternal) {
            return
        }

        val superInterfaces = descriptor.getSuperInterfaces()
        if (superInterfaces.isEmpty()) {
            return
        }

        val externalSuperInterfaces = superInterfaces
            .filter { it.isExternal }

        if (externalSuperInterfaces.isEmpty()) {
            return
        }

        val yfilesInterfaces = externalSuperInterfaces
            .filter { it.isYFiles() }

        if (yfilesInterfaces.isEmpty()) {
            return
        }

        val arguments = yfilesInterfaces
            .map { translateAsValueReference(it, context) }
            .toTypedArray()

        val invocation = JsInvocation(
            context.findFunction("yfiles.lang", "BaseClass"),
            *arguments
        )

        context.addDeclarationStatement(invocation.makeStmt())
    }
}