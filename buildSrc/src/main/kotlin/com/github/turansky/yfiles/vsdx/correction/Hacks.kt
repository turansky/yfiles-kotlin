package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.ABSTRACT
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_STRING
import com.github.turansky.yfiles.YCLASS
import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

private val TYPE_MAP = mapOf(
    "Class" to YCLASS,

    "Insets" to "yfiles.geometry.Insets",
    "Point" to "yfiles.geometry.Point",
    "Size" to "yfiles.geometry.Size",

    "IModelItem" to "yfiles.graph.IModelItem",
    "IEdge" to "yfiles.graph.IEdge",
    "ILabel" to "yfiles.graph.ILabel",
    "IGraph" to "yfiles.graph.IGraph",

    "IEdgeStyle" to "yfiles.styles.IEdgeStyle",

    "GraphComponent" to "yfiles.view.GraphComponent",

    "Color" to "yfiles.view.Color",
    "Fill" to "yfiles.view.Fill",
    "Stroke" to "yfiles.view.Stroke",
    "LinearGradient" to "yfiles.view.LinearGradient",

    "Font" to "yfiles.view.Font",
    "HorizontalTextAlignment" to "yfiles.view.HorizontalTextAlignment",
    "VerticalTextAlignment" to "yfiles.view.VerticalTextAlignment",

    "[LinearGradient,RadialGradient]" to "yfiles.view.LinearGradient",

    "[CropEdgePathsPredicate,boolean]" to "CropEdgePathsPredicate",

    "[number,vsdx.Value<number>]" to "Value<number>",
    "[vsdx.PageLike,vsdx.Shape]" to "PageLike",
    "[Document,string]" to "Document", // ??? SvgDocument

    // TODO: use data interface instead
    "Promise<{data:string,format:string}>" to "Promise<$JS_ANY>"
)

internal fun applyVsdxHacks(api: JSONObject) {
    val source = VsdxSource(api)

    fixTypes(source)
    fixOptionTypes(source)
    fixGeneric(source)
    fixMethodModifier(source)

    source.types()
        .filter { it.has(J_METHODS) }
        .forEach {
            val methods = it.jsequence(J_METHODS)
                .filter { !it.has(J_RETURNS) || !it.getJSONObject(J_RETURNS).getString(J_TYPE).startsWith("Promise<") }
                .toList()

            it.put(J_METHODS, methods)
        }
}

private fun fixPackage(source: VsdxSource) {
    source.types()
        .forEach {
            val id = it.getString(J_ID)
            it.put(J_ID, "yfiles.$id")
        }

    source.functionSignatures.apply {
        keySet().toSet().forEach { id ->
            put("yfiles.$id", get(id))
        }
    }
}

private fun JSONObject.fixType() {
    put(J_TYPE, getFixedType(getString(J_TYPE)))
}

private fun getFixedType(type: String): String {
    TYPE_MAP.get(type)?.also {
        return it
    }

    if (type.startsWith("IEnumerator<") ||
        type.startsWith("IEnumerable<") ||
        type.startsWith("IListEnumerable<") ||
        type.startsWith("IList<")
    ) {
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
                .toTypedArray()

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
        .getJSONObject("vsdx.ComparisonFunction")
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