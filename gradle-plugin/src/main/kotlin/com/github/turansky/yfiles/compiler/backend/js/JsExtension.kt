package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.backend.common.asClassMetadata
import com.github.turansky.yfiles.compiler.backend.common.implementsYFilesInterface
import com.github.turansky.yfiles.compiler.backend.common.implementsYObjectDirectly
import com.github.turansky.yfiles.compiler.backend.common.isYFilesInterface
import com.github.turansky.yfiles.compiler.diagnostic.*
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.*
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.defineProperty
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.prototypeOf
import org.jetbrains.kotlin.js.backend.ast.JsBlock
import org.jetbrains.kotlin.js.backend.ast.JsFunction
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
        if (descriptor.isExternal) {
            return
        }

        when (descriptor.kind) {
            CLASS -> context.generateClass(declaration, descriptor, translator)
            OBJECT, INTERFACE, ENUM_CLASS -> context.checkInterfaces(declaration, descriptor)
            else -> {
                // do nothing
            }
        }

        if (descriptor.isCompanionObject) {
            context.enrichCompanionObject(descriptor)
        }
    }
}

private fun TranslationContext.checkInterfaces(
    declaration: KtPureClassOrObject,
    descriptor: ClassDescriptor
) {
    if (descriptor.implementsYFilesInterface) {
        reportError(declaration, BASE_CLASS__INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
    }
}

private fun TranslationContext.generateClass(
    declaration: KtPureClassOrObject,
    descriptor: ClassDescriptor,
    translator: DeclarationBodyVisitor
) {
    when {
        descriptor.implementsYObjectDirectly ->
            generateCustomYObject(declaration, descriptor)
        descriptor.implementsYFilesInterface ->
            generateBaseClass(declaration, descriptor, translator)
    }
}

private fun TranslationContext.generateCustomYObject(
    declaration: KtPureClassOrObject,
    descriptor: ClassDescriptor
) {
    if (descriptor.getSuperInterfaces().size != 1) {
        reportError(declaration, YOBJECT__INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
        return
    }

    addDeclarationStatement(fixType(descriptor))
}

private fun TranslationContext.generateBaseClass(
    declaration: KtPureClassOrObject,
    descriptor: ClassDescriptor,
    translator: DeclarationBodyVisitor
) {
    val interfaces = descriptor.getSuperInterfaces()

    when {
        descriptor.isInline ->
            reportError(declaration, BASE_CLASS__INLINE_CLASS_NOT_SUPPORTED)
        descriptor.isData ->
            reportError(declaration, BASE_CLASS__DATA_CLASS_NOT_SUPPORTED)
        descriptor.isCompanionObject ->
            reportError(declaration, BASE_CLASS__COMPANION_OBJECT_NOT_SUPPORTED)

        interfaces.any { !it.isYFilesInterface() } ->
            reportError(declaration, BASE_CLASS__INTERFACE_MIXING_NOT_SUPPORTED)

        else -> {
            val classId = descriptor.name.identifier
            val baseClassName = generateName(classId, "BaseClass")
            val baseClass = declareConstantValue(
                suggestedName = baseClassName,
                value = baseClass(interfaces, baseClassName)
            )

            translator.addInitializerStatement(constructorSuperCall(baseClass))

            declareConstantValue(
                suggestedName = generateName(classId, "prototypeConfigured"),
                value = configurePrototype(descriptor, baseClass)
            ).also { addDeclarationStatement(it.makeStmt()) }
        }
    }
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
            getter = JsFunction(
                scope(),
                JsBlock(
                    JsReturn(JsNameRef(YCLASS, toValueReference(descriptor)))
                ),
                "\$class proxy for companion object"
            )
        ).makeStmt()
    )
}
