package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.*
import com.github.turansky.yfiles.json.removeAllObjects
import com.github.turansky.yfiles.json.strictRemove
import org.json.JSONArray
import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

private const val FROM = "from"
private const val CREATE = "create"

private const val QII = "qii"

private val TYPE_ALIAS_REGEX = Regex("(\\w+)\\['\\w+']")
private val COMPARISON_REGEX = Regex("function\\(([\\w.]+), ?([\\w.]+)\\):number")
private val FUNCTION_SIGNATURE_REGEX = Regex("function\\(([^)]*)\\):([a-zA-Z0-9.]+)")

internal fun File.readJson(): JSONObject =
    readText(UTF_8)
        .run { substring(indexOf("{")) }
        .fixInsetsDeclaration()
        .let { JSONObject(it) }

internal fun File.readApiJson(action: JSONObject.() -> Unit): JSONObject =
    readJson()
        .apply { fixArrayDeclaration() }
        .apply { fixTypeAliasing() }
        .apply { removeNamespaces() }
        .apply { fixInsetsDeclaration() }
        .apply { mergeDeclarations() }
        .apply { removeFromFactories() }
        .apply { removeRedundantCreateFactories() }
        .toString()
        .replace("yfiles.geometry.IPoint[]", "Array<yfiles.geometry.IPoint>")
        .replace("yfiles.layout.LabelLayoutData[]", "Array<yfiles.layout.LabelLayoutData>")
        .replace(COMPARISON_REGEX, "yfiles.lang.Comparison1<$1>")
        .replace("yfiles.analysis.LayoutGraphAlgorithms.DfsNodeVisited", "yfiles.analysis.DfsNodeVisited")
        .replace("yfiles.analysis.LayoutGraphAlgorithms.DfsNodeVisiting", "yfiles.analysis.DfsNodeVisiting")
        .replace("yfiles.analysis.LayoutGraphAlgorithms.DfsEdgeTraversed", "yfiles.analysis.DfsEdgeTraversed")
        .replace("yfiles.analysis.LayoutGraphAlgorithms.DfsEdgeTraversing", "yfiles.analysis.DfsEdgeTraversing")
        .replace("yfiles.analysis.LayoutGraphAlgorithms.DfsNextTreeVisiting", "yfiles.analysis.DfsNextTreeVisiting")
        .replace("yfiles.analysis.LayoutGraphAlgorithms.DfsNextTreeVisited", "yfiles.analysis.DfsNextTreeVisited")
        .replace("yfiles.labeling.GenericLabeling.EdgeLabelCandidateProcessor", "yfiles.labeling.EdgeLabelCandidateProcessor")
        .replace("yfiles.labeling.GenericLabeling.NodeLabelCandidateProcessor", "yfiles.labeling.NodeLabelCandidateProcessor")
        .replace("IEnumerableConvertible<IAnimation|WebGLAnimation>", "IEnumerableConvertible<IAnimation>")
        .replace("\"FocusOptions\"", "\"web.dom.FocusOptions\"")
        .replace("any[]", "Array<Any>")
        .replaceFunctionSignatures()
        .fixSystemPackage()
        .run { JSONObject(this) }
        .apply(action)
        .apply { fixFunctionSignatures() }
        .toString()
        .run { JSONObject(this) }

private fun String.fixSystemPackage(): String =
    replace("\"yfiles.system.", "\"yfiles.lang.")
        .replace("\"system.", "\"yfiles.lang.")

private fun String.fixInsetsDeclaration(): String =
    replace("yfiles.algorithms.Insets", "yfiles.algorithms.YInsets")

private fun Any.fixArrayDeclaration() {
    when (this) {
        is JSONObject -> fixArrayDeclaration()
        is JSONArray -> fixArrayDeclaration()
    }
}

private fun JSONObject.fixArrayDeclaration() {
    for (key in keys()) {
        get(key).fixArrayDeclaration()
    }

    val type = optString("type")
        .ifEmpty { return }

    if (optString("dimension") != "[]")
        return

    remove("dimension")
    put("type", "Array<$type>")
}

private fun Any.fixTypeAliasing() {
    when (this) {
        is JSONObject -> fixTypeAliasing()
        is JSONArray -> fixTypeAliasing()
    }
}

private fun JSONArray.fixTypeAliasing() {
    for (item in this) {
        item.fixTypeAliasing()
    }
}

private fun JSONObject.fixTypeAliasing() {
    for (key in keys()) {
        get(key).fixTypeAliasing()
    }

    val type = optString("type")
        .ifEmpty { return }

    put("type", type.replace(TYPE_ALIAS_REGEX, TAG))
}

private fun JSONArray.fixArrayDeclaration() {
    for (item in this) {
        item.fixArrayDeclaration()
    }
}

private fun JSONObject.fixInsetsDeclaration() =
    flatMap(TYPES)
        .firstOrNull { it[ID] == "yfiles.algorithms.YInsets" }
        ?.also { it[NAME] = "YInsets" }

private fun JSONObject.mergeDeclarations() {
    flatMap(TYPES)
        .forEach {
            it.merge(PROPERTIES, STATIC_PROPERTIES)
            it.merge(METHODS, STATIC_METHODS)
        }
}

private fun JSONObject.merge(
    key: JArrayKey,
    staticKey: JArrayKey,
) {
    if (!has(staticKey)) {
        return
    }

    if (has(key)) {
        val items = get(key)
        flatMap(staticKey).forEach { items.put(it) }
    } else {
        set(key, get(staticKey))
    }

    strictRemove(staticKey)
}

private fun JSONObject.removeNamespaces() {
    val types = flatMap(NAMESPACES)
        .flatMap { it.optFlatMap(NAMESPACES).flatMap(TYPES) + it.optFlatMap(TYPES) }
        .toList()

    set(TYPES, types)
}

private fun String.replaceFunctionSignatures() : String {
    var str = this
    generateSequence { FUNCTION_SIGNATURE_REGEX.find(str) }.forEach { result ->
        val arguments = result.groupValues[1]
            .split(",")
            .joinToString(", ") {
                getKotlinType(it) ?: it
            }
            .replace("unknown", ANY)

        val returnType = result.groupValues[2].let { getKotlinType(it) ?: it }
        str = str.replace(result.value, "($arguments) -> $returnType")
    }
    return str
}

private fun JSONObject.fixFunctionSignatures() {
    val signatureMap = getJSONObject("functionSignatures")
    val signatures = JSONArray()
    signatureMap.keySet().forEach { key ->
        signatures.put(
            signatureMap.getJSONObject(key)
                .also { it.put("id", key) }
        )
    }

    put("functionSignatures", signatures)
}

private fun JSONObject.removeFromFactories() {
    flatMap(TYPES).forEach { type ->
        val fromFactory = type.optFlatMap(METHODS)
            .firstOrNull { it.isFromFactory() }
            ?: return@forEach

        if (type.has(REMARKS) && " are converted to " in type[REMARKS]) {
            fromFactory.firstParameter[TYPE] = JS_STRING
        } else {
            type[METHODS].removeAll { it === fromFactory }
        }
    }
}

private fun JSONObject.isFromFactory(): Boolean =
    isStaticMethod(FROM) && get(PARAMETERS).length() == 1

private fun JSONObject.removeRedundantCreateFactories() {
    flatMap(TYPES)
        .filter { it[GROUP] == "interface" }
        .mapNotNull { it.opt(METHODS) }
        .forEach { methods ->
            methods.removeAllObjects { it.isRedundantCreateFactory() }

            methods.asSequence()
                .filterIsInstance<JSONObject>()
                .filter { it.isStaticMethod(CREATE) }
                .forEach { it.put(QII, true) }
        }
}

private fun JSONObject.isRedundantCreateFactory(): Boolean =
    isStaticMethod(CREATE)
            && optString(QII) == "!0"
            && get(PARAMETERS).length() != 1

private fun JSONObject.isStaticMethod(name: String): Boolean =
    STATIC in get(MODIFIERS) && get(NAME) == name
