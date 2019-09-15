package com.github.turansky.yfiles.compiler.extensions

import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind
import org.jetbrains.kotlin.js.facade.exceptions.UnsupportedFeatureException
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameOrNull
import org.jetbrains.kotlin.resolve.descriptorUtil.fqNameUnsafe
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
            .filter { it.companionObjectDescriptor != null }
            .mapNotNull { it.fqNameOrNull() }
            .map { it.asString() }
            .filter { it.startsWith("yfiles.") }
            .toList()

        if (yfilesInterfaces.isEmpty()) {
            return
        }

        val type = descriptor.fqNameUnsafe.asString()
        val message = """
            | $type couldn't implement following interfaces: ${yfilesInterfaces.joinToString()}.
            | yFiles interface implementation not supported for non-external classes yet.
        """.trimMargin()

        throw UnsupportedFeatureException(
            message,
            IllegalStateException()
        )
    }
}