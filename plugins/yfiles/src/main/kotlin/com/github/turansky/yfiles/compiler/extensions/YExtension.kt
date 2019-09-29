package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.diagnostic.YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED
import com.github.turansky.yfiles.compiler.diagnostic.YOBJECT_INTERFACE_IMPLEMENTING_NOT_SUPPORTED
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.*
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

class YExtension : JsSyntheticTranslateExtension {
    override fun generateClassSyntheticParts(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        translator: DeclarationBodyVisitor,
        context: TranslationContext
    ) {
        if (descriptor.isExternal) {
            return
        }

        when (descriptor.kind) {
            CLASS -> context.generateClass(declaration, descriptor)
            OBJECT, INTERFACE, ENUM_CLASS -> context.checkInterfaces(declaration, descriptor)
            else -> {
                // do nothing
            }
        }
    }
}

private fun TranslationContext.checkInterfaces(
    declaration: KtPureClassOrObject,
    descriptor: ClassDescriptor
) {
    if (descriptor.implementsYFilesInterface) {
        reportError(declaration, YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
    }
}

private fun TranslationContext.generateClass(
    declaration: KtPureClassOrObject,
    descriptor: ClassDescriptor
) {
    when {
        descriptor.extendsYObject ->
            generateCustomYObject(declaration, descriptor)
        descriptor.implementsYFilesInterface ->
            generateBaseClass(descriptor)
    }
}

private fun TranslationContext.generateCustomYObject(
    declaration: KtPureClassOrObject,
    descriptor: ClassDescriptor
) {
    if (descriptor.getSuperInterfaces().isNotEmpty()) {
        reportError(declaration, YOBJECT_INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
        return
    }

    addDeclarationStatement(fixType(descriptor))
}

private fun TranslationContext.generateBaseClass(
    descriptor: ClassDescriptor
) {
    val interfaces = descriptor.getSuperInterfaces()

    addDeclarationStatement(
        setBaseClassPrototype(descriptor, interfaces)
    )
}
