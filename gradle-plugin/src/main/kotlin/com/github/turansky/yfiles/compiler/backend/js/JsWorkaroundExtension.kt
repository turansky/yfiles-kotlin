package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.backend.common.LANG_PACKAGE
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.ClassKind.CLASS
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.declaration.DeclarationBodyVisitor
import org.jetbrains.kotlin.js.translate.extensions.JsSyntheticTranslateExtension
import org.jetbrains.kotlin.js.translate.utils.addFunctionButNotExport
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.psi.KtPureClassOrObject
import org.jetbrains.kotlin.resolve.DescriptorUtils.getFunctionByName

private val KOTLIN_WORKAROUNDS = ClassId(LANG_PACKAGE, identifier("KotlinWorkarounds"))
private val APPLY = identifier("apply")

val CONTEXT = "context"

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

        val name = descriptor.name.identifier
        context.apply {
            declareConstantValue(
                suggestedName = generateName("KT_34770", name, "applied"),
                value = fixProperties(descriptor)
            ).also { addDeclarationStatement(it.makeStmt()) }
        }
    }
}

private fun TranslationContext.fixProperties(
    descriptor: ClassDescriptor
): JsExpression {
    val name = descriptor.name.identifier

    val applyWorkaround = currentModule
        .findClassAcrossModuleDependencies(KOTLIN_WORKAROUNDS)!!
        .unsubstitutedMemberScope
        .let { getFunctionByName(it, APPLY) }

    val context = JsParameter(JsName(CONTEXT))
    return addFunctionButNotExport(
        JsName(generateName("applyWorkaround", "KT_34770", name)),
        JsFunction(
            scope(),
            JsBlock(
                JsInvocation(
                    toValueReference(applyWorkaround),
                    JsNameRef(CONTEXT)
                ).makeStmt(),
                JsReturn(JsBooleanLiteral(true))
            ),
            "$name 'KT-34770' fix method"
        ).apply { parameters.add(context) }
    ).let { JsInvocation(it.makeRef(), JsThisRef()) }
}
