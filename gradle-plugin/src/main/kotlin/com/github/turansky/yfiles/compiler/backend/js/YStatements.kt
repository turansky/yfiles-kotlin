package com.github.turansky.yfiles.compiler.backend.js

import com.github.turansky.yfiles.compiler.backend.common.BASE_CLASS_NAME
import com.github.turansky.yfiles.compiler.backend.common.LANG_PACKAGE
import com.github.turansky.yfiles.compiler.backend.common.YCLASS_NAME
import org.jetbrains.kotlin.descriptors.ClassDescriptor
import org.jetbrains.kotlin.descriptors.findClassAcrossModuleDependencies
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.jsAssignment
import org.jetbrains.kotlin.ir.backend.js.transformers.irToJs.prototypeOf
import org.jetbrains.kotlin.ir.backend.js.utils.Namer.CONSTRUCTOR_NAME
import org.jetbrains.kotlin.ir.backend.js.utils.Namer.JS_OBJECT_CREATE_FUNCTION
import org.jetbrains.kotlin.js.backend.ast.*
import org.jetbrains.kotlin.js.translate.context.Namer.CALL_FUNCTION
import org.jetbrains.kotlin.js.translate.context.TranslationContext
import org.jetbrains.kotlin.js.translate.utils.addFunctionButNotExport
import org.jetbrains.kotlin.name.ClassId
import org.jetbrains.kotlin.name.Name.identifier
import org.jetbrains.kotlin.resolve.DescriptorUtils.getFunctionByName

private val YCLASS_ID = ClassId(LANG_PACKAGE, YCLASS_NAME)
private val FIX_TYPE = identifier("fixType")

private infix fun JsExpression.assignTo(value: JsExpression): JsStatement =
    jsAssignment(this, value).makeStmt()

private fun TranslationContext.fixType(
    descriptor: ClassDescriptor
): JsStatement {
    val yclassCompanion = currentModule.findClassAcrossModuleDependencies(YCLASS_ID)!!
        .companionObjectDescriptor!!

    val fixType = getFunctionByName(
        yclassCompanion.unsubstitutedMemberScope,
        FIX_TYPE
    )

    return JsInvocation(
        toValueReference(fixType),
        toValueReference(descriptor),
        JsStringLiteral(descriptor.name.identifier)
    ).makeStmt()
}

internal fun TranslationContext.baseClass(
    interfaces: List<ClassDescriptor>,
    name: String
): JsExpression {
    val arguments = interfaces
        .map { toValueReference(it) }
        .toTypedArray()

    return JsInvocation(
        findFunction(LANG_PACKAGE, BASE_CLASS_NAME),
        *arguments
    ).let { wrapBaseClass(it, name) }
}

// TODO: remove after ticket fix
//  https://youtrack.jetbrains.com/issue/KT-34735
private fun TranslationContext.wrapBaseClass(
    baseClass: JsExpression,
    name: String
): JsExpression =
    addFunctionButNotExport(
        JsName(generateName("create", name)),
        jsFunction(
            "$name factory method",
            JsReturn(baseClass)
        )
    ).let { JsInvocation(it.makeRef()) }

internal fun constructorSuperCall(parentClass: JsExpression): JsStatement =
    JsInvocation(
        JsNameRef(CALL_FUNCTION, parentClass),
        JsThisRef()
    ).makeStmt()

internal fun TranslationContext.configurePrototype(
    descriptor: ClassDescriptor,
    baseClass: JsExpression,
    fixType: Boolean = false
) {
    declareConstantValue(
        suggestedName = generateName(descriptor, "prototypeConfigured"),
        value = configurePrototypeMethod(descriptor, baseClass, fixType)
    ).also { addDeclarationStatement(it.makeStmt()) }
}

private fun TranslationContext.configurePrototypeMethod(
    descriptor: ClassDescriptor,
    baseClass: JsExpression,
    fixType: Boolean
): JsExpression {
    val classId = descriptor.name.identifier
    val classRef = toValueReference(descriptor)
    val classPrototype = prototypeOf(classRef)

    val statements = mutableListOf(
        classPrototype assignTo JsInvocation(JS_OBJECT_CREATE_FUNCTION, prototypeOf(baseClass)),
        JsNameRef(CONSTRUCTOR_NAME, classPrototype) assignTo classRef,
        JsReturn(JsBooleanLiteral(true))
    ).also {
        if (fixType) {
            it.add(2, fixType(descriptor))
        }
    }.toList()

    return addFunctionButNotExport(
        JsName(generateName("configure", classId, "prototype")),
        jsFunction(
            "$classId prototype configuration",
            statements
        )
    ).let { JsInvocation(it.makeRef()) }
}
