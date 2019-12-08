package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.backend.common.LANG_PACKAGE
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.CLASS
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.psi.KtPureClassOrObject

// TODO: remove after fix - https://youtrack.jetbrains.com/issue/KT-34770
class JsWorkaroundExtension : JsSyntheticTranslateExtension {
    private val CONFIGURABLE_PROPERTIES = LANG_PACKAGE.child(identifier("ConfigurableProperties"))

    override fun generateClassSyntheticParts(
        declaration: KtPureClassOrObject,
        descriptor: ClassDescriptor,
        translator: DeclarationBodyVisitor,
        context: TranslationContext
    ) {
        when {
            descriptor.kind != CLASS -> return
            descriptor.isExternal -> return
            descriptor.isInline -> return
            descriptor.isData -> return
            !descriptor.annotations.hasAnnotation(CONFIGURABLE_PROPERTIES) -> return
        }
    }
}
