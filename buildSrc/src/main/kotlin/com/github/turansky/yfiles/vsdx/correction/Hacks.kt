package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

private val YFILES_TYPE_MAP = sequenceOf(
    YCLASS,

    "yfiles.collections.IEnumerator",
    "yfiles.collections.IEnumerable",

    "yfiles.geometry.Insets",
    "yfiles.geometry.Point",
    "yfiles.geometry.Size",

    "yfiles.graph.IModelItem",
    "yfiles.graph.INode",
    "yfiles.graph.IEdge",
    "yfiles.graph.ILabel",
    "yfiles.graph.IPort",
    "yfiles.graph.IGraph",

    "yfiles.graph.ILabelModelParameter",
    "yfiles.graph.InteriorStretchLabelModel",

    "yfiles.styles.INodeStyle",
    "yfiles.styles.IEdgeStyle",
    "yfiles.styles.ILabelStyle",
    "yfiles.styles.IPortStyle",

    "yfiles.styles.ArcEdgeStyle",
    "yfiles.styles.PolylineEdgeStyle",
    "yfiles.styles.VoidEdgeStyle",
    "yfiles.styles.IEdgePathCropper",

    "yfiles.styles.DefaultLabelStyle",

    "yfiles.styles.ImageNodeStyle",
    "yfiles.styles.PanelNodeStyle",
    "yfiles.styles.ShapeNodeStyle",

    "yfiles.styles.NodeStylePortStyleAdapter",
    "yfiles.styles.VoidPortStyle",

    "yfiles.view.GraphComponent",

    "yfiles.view.Color",
    "yfiles.view.Fill",
    "yfiles.view.Stroke",
    "yfiles.view.LinearGradient",

    "yfiles.view.Font",
    "yfiles.view.HorizontalTextAlignment",
    "yfiles.view.VerticalTextAlignment"
).associate { it.substringAfterLast(".") to it }

private val TYPE_MAP = YFILES_TYPE_MAP + mapOf(
    "[LinearGradient,RadialGradient]" to "yfiles.view.LinearGradient",

    "[CropEdgePathsPredicate,boolean]" to "CropEdgePathsPredicate",

    "[number,vsdx.Value<number>]" to "Value<number>",
    "[vsdx.PageLike,vsdx.Shape]" to "PageLike",
    "[Document,string]" to "Document", // ??? SvgDocument

    // TODO: use data interface instead
    "Promise<{data:string,format:string}>" to "Promise<$JS_ANY>",
    "Promise<{master:vsdx.Master,fillStyle:vsdx.StyleSheet,lineStyle:vsdx.StyleSheet,textStyle:vsdx.StyleSheet}>" to "Promise<$JS_ANY>",
    "Promise<[{master:vsdx.Master,fillStyle:vsdx.StyleSheet,lineStyle:vsdx.StyleSheet,textStyle:vsdx.StyleSheet},null]>" to "Promise<$ANY?>"
)

private val COLLECTION_INTERFACES = setOf(
    "IEnumerator<",
    "IEnumerable<",
    "IListEnumerable<",
    "IList<"
)

internal fun applyVsdxHacks(api: JSONObject) {
    val source = VsdxSource(api)

    fixPackage(source)

    fixTypes(source)
    fixOptionTypes(source)
    fixGeneric(source)
    fixMethodModifier(source)
    fixSummary(source)
}

private fun String.fixVsdxPackage(): String =
    replace("vsdx.", "yfiles.vsdx.")

private fun fixPackage(source: VsdxSource) {
    source.types()
        .forEach {
            val id = it.getString(J_ID)
            it.put(J_ID, "yfiles.$id")
        }

    source.types()
        .filter { it.has(J_EXTENDS) }
        .forEach {
            it.put(J_EXTENDS, it.getString(J_EXTENDS).fixVsdxPackage())
        }

    source.types()
        .filter { it.has(J_IMPLEMENTS) }
        .forEach {
            val implementedTypes = it.getJSONArray(J_IMPLEMENTS)
                .asSequence()
                .map { it as String }
                .map { it.fixVsdxPackage() }
                .toList()

            it.put(J_IMPLEMENTS, implementedTypes)
        }

    source.functionSignatures.apply {
        keySet().toSet().forEach { id ->
            val functionSignature = getJSONObject(id)
            if (functionSignature.has(J_RETURNS)) {
                functionSignature.getJSONObject(J_RETURNS)
                    .fixType()
            }

            put(id.fixVsdxPackage(), functionSignature)
            remove(id)
        }
    }
}

private fun JSONObject.fixType() {
    if (has(J_SIGNATURE)) {
        val signature = getFixedType(getString(J_SIGNATURE))
            .fixVsdxPackage()

        put(J_SIGNATURE, signature)
    } else {
        val type = getFixedType(getString(J_TYPE))
            .fixVsdxPackage()

        put(J_TYPE, type)
    }
}

private fun getFixedType(type: String): String {
    TYPE_MAP.get(type)?.also {
        return it
    }

    if (COLLECTION_INTERFACES.any { type.startsWith(it) }) {
        return "yfiles.collections.$type"
    }

    if (type.startsWith("[$JS_STRING,")) {
        return JS_STRING
    }

    if (type.startsWith("{")) {
        return JS_ANY
    }

    return type
}

private fun fixTypes(source: VsdxSource) {
    source.types()
        .filter { it.has(J_IMPLEMENTS) }
        .forEach {
            val implementedTypes = it.getJSONArray(J_IMPLEMENTS)
                .asSequence()
                .map { it as String }
                .map { getFixedType(it) }
                .toList()

            it.put(J_IMPLEMENTS, implementedTypes)
        }

    source.types()
        .flatMap {
            (it.optJsequence(J_CONSTRUCTORS) + it.optJsequence(J_STATIC_METHODS) + it.optJsequence(J_METHODS))
                .flatMap {
                    it.optJsequence(J_PARAMETERS) + if (it.has(J_RETURNS)) {
                        sequenceOf(it.getJSONObject(J_RETURNS))
                    } else {
                        emptySequence()
                    }
                }
                .plus(it.optJsequence(J_PROPERTIES))
        }
        .forEach { it.fixType() }

    source.functionSignatures
        .run {
            keySet().asSequence().map {
                getJSONObject(it)
            }
        }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .forEach { it.fixType() }
}

private fun fixOptionTypes(source: VsdxSource) {
    source.type("CachingMasterProvider")
        .jsequence(J_CONSTRUCTORS)
        .single()
        .apply {
            parameter("optionsOrNodeStyleType").apply {
                put(J_NAME, "nodeStyleType")
                put(J_TYPE, "$YCLASS<yfiles.styles.INodeStyle>")
            }

            parameter("edgeStyleType")
                .addGeneric("yfiles.styles.IEdgeStyle")
            parameter("portStyleType")
                .addGeneric("yfiles.styles.IPortStyle")
            parameter("labelStyleType")
                .addGeneric("yfiles.styles.ILabelStyle")
        }

    source.type("CustomEdgeProvider")
        .jsequence(J_CONSTRUCTORS)
        .single()
        .parameter("edgeStyleType")
        .addGeneric("yfiles.styles.IEdgeStyle")

    source.type("VssxStencilProviderFactory").apply {
        sequenceOf(
            "createMappedEdgeProvider" to "yfiles.styles.IEdgeStyle",
            "createMappedLabelProvider" to "yfiles.styles.ILabelStyle",
            "createMappedNodeProvider" to "yfiles.styles.INodeStyle",
            "createMappedPortProvider" to "yfiles.styles.IPortStyle"
        ).forEach { (methodName, styleGeneric) ->
            methodParameters(methodName, "styleType")
                .forEach { it.addGeneric(styleGeneric) }
        }
    }
}

private fun fixGeneric(source: VsdxSource) {
    source.functionSignatures
        .getJSONObject("yfiles.vsdx.ComparisonFunction")
        .setSingleTypeParameter()

    source.type("Value").apply {
        staticMethod("fetch")
            .apply {
                setSingleTypeParameter("TValue")
                firstParameter.put(J_NAME, "o")
            }

        staticMethod("formula")
            .setSingleTypeParameter("TValue")
    }
}

private fun fixMethodModifier(source: VsdxSource) {
    source.types("IMasterProvider", "IShapeProcessingStep")
        .jsequence(J_METHODS)
        .forEach { it.getJSONArray(J_MODIFIERS).put(ABSTRACT) }
}

private val YFILES_API_REGEX = Regex("<a href=\"https://docs.yworks.com/yfileshtml/#/api/([a-zA-Z]+)\">([a-zA-Z]+)</a>")
private val VSDX_API_REGEX = Regex("<a href=\"#/api/([a-zA-Z]+)\">([a-zA-Z]+)</a>")

private fun JSONObject.fixSummary() {
    if (!has(J_SUMMARY)) {
        return
    }

    val summary = getString(J_SUMMARY)
        .replace(YFILES_API_REGEX) {
            val type = YFILES_TYPE_MAP.getValue(it.groupValues.get(1))
            "[$type]"
        }
        .replace(VSDX_API_REGEX, "[$1]")
        .replace("\r\n", " ")
        .replace("\r", " ")
        .replace("</p>", "")
        .replace("<p>", "\n\n")

    put(J_SUMMARY, summary)
}

private fun fixSummary(source: VsdxSource) {
    source.type("VsdxExport")
        .jsequence(J_METHODS)
        .jsequence(J_PARAMETERS)
        .filter { it.has(J_SUMMARY) }
        .forEach {
            val summary = it.getString(J_SUMMARY)
                .replace("""data-member="createDefault()"""", """data-member="createDefault"""")

            it.put(J_SUMMARY, summary)
        }

    source.types()
        .onEach { it.fixSummary() }
        .onEach { it.optJsequence(J_PROPERTIES).forEach { it.fixSummary() } }
        .flatMap { it.optJsequence(J_CONSTRUCTORS) + it.optJsequence(J_STATIC_METHODS) + it.optJsequence(J_METHODS) }
        .onEach { it.fixSummary() }
        .flatMap { it.optJsequence(J_PARAMETERS) }
        .forEach { it.fixSummary() }
}