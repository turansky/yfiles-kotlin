package com.github.turansky.yfiles.compiler.extensions

import com.github.turansky.yfiles.compiler.diagnostic.YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED
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

        @Suppress("NON_EXHAUSTIVE_WHEN")
        when (descriptor.kind) {
            CLASS -> generateClass(descriptor, context)
            OBJECT, INTERFACE -> checkInterfaces(declaration, descriptor, context)
        }
    }

    private fun checkInterfaces(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        context: TranslationContext
    ) {
        val invalidImplementation = descriptor
            .getSuperInterfaces()
            .any { it.isYFiles() }

        if (invalidImplementation) {
            context.reportError(declaration, YFILES_INTERFACE_IMPLEMENTING_NOT_SUPPORTED)
        }
    }

    private fun generateClass(
        descriptor: ClassDescriptor,
        context: TranslationContext
    ) {
        val superInterfaces = descriptor.getSuperInterfaces()
        if (superInterfaces.isEmpty()) {
            return
        }

        val yfilesInterfaces = superInterfaces
            .filter { it.isYFiles() }

        if (yfilesInterfaces.isEmpty()) {
            return
        }

        val arguments = yfilesInterfaces
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
