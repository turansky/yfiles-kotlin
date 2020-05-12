package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.backend.common.implementsYFilesInterface
import com.github.turansky.yfiles.compiler.backend.common.implementsYObjectDirectly
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.CLASS
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

class JsExtension : JsSyntheticTranslateExtension {
    override fun generateClassSyntheticParts(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        translator: DeclarationBodyVisitor,
        context: TranslationContext
    ) {
        when {
            descriptor.isExternal
            -> return

            descriptor.kind != CLASS
            -> return

            descriptor.implementsYObjectDirectly ->
                context.generateCustomYObject(descriptor, translator)

            descriptor.implementsYFilesInterface ->
                context.generateBaseClass(descriptor, translator)
        }
    }
}

private fun TranslationContext.generateCustomYObject(
    descriptor: ClassDescriptor,
    translator: DeclarationBodyVisitor
) {
    val yobject = descriptor.getSuperInterfaces().single()
    val baseClass = toValueReference(yobject)
    translator.addInitializerStatement(constructorSuperCall(baseClass))
    configurePrototype(descriptor, baseClass, true)
}

private fun TranslationContext.generateBaseClass(
    descriptor: ClassDescriptor,
    translator: DeclarationBodyVisitor
) {
    val baseClassName = generateName(descriptor, "BaseClass")
    val baseClass = declareConstantValue(
        suggestedName = baseClassName,
        value = baseClass(descriptor.getSuperInterfaces(), baseClassName)
    )

    translator.addInitializerStatement(constructorSuperCall(baseClass))
    configurePrototype(descriptor, baseClass)
}
