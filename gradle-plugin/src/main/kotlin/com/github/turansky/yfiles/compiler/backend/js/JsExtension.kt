package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.backend.common.asClassMetadata
import com.github.turansky.yfiles.compiler.backend.common.implementsYFilesInterface
import com.github.turansky.yfiles.compiler.backend.common.implementsYObjectDirectly
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.CLASS
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.defineProperty
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.prototypeOf
import org.jetbrains.kotlin.js.backend.ast.JsNameRef
import org.jetbrains.kotlin.js.backend.ast.JsReturn
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.descriptorUtil.getSuperInterfaces

private const val YCLASS = "\$class"

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

            descriptor.kind == CLASS
            -> context.generateClass(descriptor, translator)

            descriptor.isCompanionObject
            -> context.enrichCompanionObject(descriptor)
        }
    }
}

private fun TranslationContext.generateClass(
    descriptor: ClassDescriptor,
    translator: DeclarationBodyVisitor
) {
    when {
        descriptor.implementsYObjectDirectly ->
            generateCustomYObject(descriptor, translator)

        descriptor.implementsYFilesInterface ->
            generateBaseClass(descriptor, translator)
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

private fun TranslationContext.enrichCompanionObject(
    companionDescriptor: ClassDescriptor
) {
    val descriptor = companionDescriptor.containingDeclaration as? ClassDescriptor
        ?: return

    if (!descriptor.implementsYObjectDirectly) {
        return
    }

    companionDescriptor.asClassMetadata() ?: return

    // TODO: add ClassMetadata generic check

    val constructor = companionDescriptor.constructors.single()
    addDeclarationStatement(
        defineProperty(
            receiver = prototypeOf(toValueReference(constructor)),
            name = YCLASS,
            getter = jsFunction(
                "\$class proxy for companion object",
                JsReturn(JsNameRef(YCLASS, toValueReference(descriptor)))
            )
        ).makeStmt()
    )
}
