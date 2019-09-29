package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.diagnostic.YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED
import com.github.turansky.yfiles.compiler.diagnostic.YOBJECT_INTERFACE_IMPLEMENTING_NOT_SUPPORTED
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.*
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.jsAssignment
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.prototypeOf
import org.jetbrains.kotlin.ir.backend.js.utils.Namer.JS_OBJECT_CREATE_FUNCTION
import org.jetbrains.kotlin.js.backend.ast.JsInvocation
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.js.translate.reference.ReferenceTranslator.translateAsValueReference
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
            CLASS -> generateClass(declaration, descriptor, context)
            OBJECT, INTERFACE, ENUM_CLASS -> checkInterfaces(declaration, descriptor, context)
            else -> {
                /* do nothing */
            }
        }
    }

    private fun checkInterfaces(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        context: TranslationContext
    ) {
        if (descriptor.implementsYFilesInterface) {
            context.reportError(declaration, YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
        }
    }

    private fun generateClass(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        context: TranslationContext
    ) {
        when {
            descriptor.extendsYObject ->
                generateCustomYObject(declaration, descriptor, context)
            descriptor.implementsYFilesInterface ->
                generateBaseClass(descriptor, context)
        }
    }

    private fun generateCustomYObject(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        context: TranslationContext
    ) {
        if (descriptor.getSuperInterfaces().isNotEmpty()) {
            context.reportError(declaration, YOBJECT_INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
        }
    }

    private fun generateBaseClass(
        descriptor: ClassDescriptor,
        context: TranslationContext
    ) {
        val interfaces = descriptor.getSuperInterfaces()

        val arguments = interfaces
            .map { translateAsValueReference(it, context) }
            .toTypedArray()

        val baseClass = JsInvocation(
            context.findFunction("yfiles.lang", "BaseClass"),
            *arguments
        )

        val assignment = jsAssignment(
            prototypeOf(translateAsValueReference(descriptor, context)),
            JsInvocation(JS_OBJECT_CREATE_FUNCTION, baseClass)
        )

        context.addDeclarationStatement(assignment.makeStmt())
    }
}
