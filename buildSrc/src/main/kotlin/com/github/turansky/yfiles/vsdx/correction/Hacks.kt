package com.github.turansky.yfiles.vsdx.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

private val YFILES_TYPE_MAP = sequenceOf(
    YCLASS,

    "yfiles.collections.IEnumerator",
    IENUMERABLE,

    "yfiles.geometry.Insets",
    "yfiles.geometry.Point",
    "yfiles.geometry.Size",
    "yfiles.geometry.Rect",

    IMODEL_ITEM,
    INODE,
    IEDGE,
    ILABEL,
    IPORT,
    IGRAPH,

    ILABEL_MODEL_PARAMETER,
    "yfiles.graph.InteriorStretchLabelModel",

    "yfiles.styles.INodeStyle",
    "yfiles.styles.IEdgeStyle",
    "yfiles.styles.ILabelStyle",
    "yfiles.styles.IPortStyle",

    "yfiles.styles.ArcEdgeStyle",
    "yfiles.styles.ArrowEdgeStyle",
    "yfiles.styles.ArrowNodeStyle",
    "yfiles.styles.GroupNodeStyle",
    "yfiles.styles.PolylineEdgeStyle",
    "yfiles.styles.RectangleNodeStyle",
    "yfiles.styles.BezierEdgeStyle",
    "yfiles.styles.VoidEdgeStyle",
    "yfiles.styles.IEdgePathCropper",

    "yfiles.styles.DefaultLabelStyle",

    "yfiles.styles.ImageNodeStyle",
    "yfiles.styles.PanelNodeStyle",
    "yfiles.styles.ShapeNodeStyle",

    "yfiles.styles.NodeStylePortStyleAdapter",
    "yfiles.styles.VoidPortStyle",

    "yfiles.view.GraphComponent",
    "yfiles.view.IRenderContext",
    VISUAL,

    "yfiles.view.Color",
    "yfiles.view.Fill",
    "yfiles.view.Stroke",
    "yfiles.view.LinearGradient",

    "yfiles.view.Font",
    "yfiles.view.HorizontalTextAlignment",
    "yfiles.view.VerticalTextAlignment"
).associateBy {
    if (it == YCLASS) "Class" else it.substringAfterLast(".")
}

private val TYPE_MAP = YFILES_TYPE_MAP + mapOf(
    "[LinearGradient,RadialGradient]" to "yfiles.view.LinearGradient",

    "[CropEdgePathsPredicate,boolean]" to "CropEdgePathsPredicate",

    "[number,vsdx.Value<number>]" to "Value<number>",
    "[vsdx.PageLike,vsdx.Shape]" to "PageLike",
    "[Document,Element,string]" to "SVGElement",
    "[vsdx.CoordinateConverter,vsdx.Media]" to "CoordinateConverter",

    "[Promise<$JS_VOID>,undefined]" to "Promise<$JS_VOID>",
    "Promise<{data:string,format:string}>" to "Promise<$IMAGE_DATA_RESPONSE>",
    "Promise<{master:vsdx.Master,fillStyle:vsdx.StyleSheet,lineStyle:vsdx.StyleSheet,textStyle:vsdx.StyleSheet}>" to "Promise<$MASTER_STATE>",
    "Promise<[{master:vsdx.Master,fillStyle:vsdx.StyleSheet,lineStyle:vsdx.StyleSheet,textStyle:vsdx.StyleSheet},null]>" to "Promise<$MASTER_STATE?>",
    "Promise<${VISUAL.substringAfterLast(".")}>" to "Promise<$VISUAL>"
)

private val COLLECTION_INTERFACES = setOf(
    "IEnumerator<",
    "IEnumerable<",
    "IListEnumerable<",
    "IList<"
)

internal fun applyVsdxHacks(api: JSONObject) {
    val source = VsdxSource(api)

    fixOptionsParameter(source)

    fixPackage(source)

    fixTypes(source)
    fixOptionTypes(source)
    fixGeneric(source)
    fixMethodModifier(source)
    fixSummary(source)
}

private fun fixOptionsParameter(source: VsdxSource) {
    source.types()
        .optFlatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME].endsWith("OrOptions") }
        .onEach { it[NAME] = it[NAME].removeSuffix("OrOptions") }
        .forEach { it[TYPE] = it[TYPE].between("[", ",", true) }
}

private fun String.fixVsdxPackage(): String =
    replace("vsdx.", "yfiles.vsdx.")

private fun fixPackage(source: VsdxSource) {
    source.types()
        .forEach {
            val id = it[ID]
            it[ID] = "yfiles.$id"
        }

    source.types()
        .filter { it.has(EXTENDS) }
        .forEach {
            it[EXTENDS] = it[EXTENDS].fixVsdxPackage()
        }

    source.types()
        .filter { it.has(IMPLEMENTS) }
        .forEach {
            it[IMPLEMENTS] = it[IMPLEMENTS]
                .asSequence()
                .map { it as String }
                .map { it.fixVsdxPackage() }
                .toList()
        }

    source.functionSignatures.apply {
        keySet().toSet().forEach { id ->
            val functionSignature = getJSONObject(id)
            if (functionSignature.has(RETURNS)) {
                functionSignature[RETURNS]
                    .fixType()
            }

            put(id.fixVsdxPackage(), functionSignature)
            remove(id)
        }
    }
}

private fun JSONObject.fixType() {
    if (has(SIGNATURE)) {
        val signature = getFixedType(get(SIGNATURE))
            .fixVsdxPackage()

        set(SIGNATURE, signature)
    } else {
        val type = getFixedType(get(TYPE))
            .fixVsdxPackage()

        set(TYPE, type)
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
        .filter { it.has(IMPLEMENTS) }
        .forEach {
            it[IMPLEMENTS] = it[IMPLEMENTS]
                .asSequence()
                .map { it as String }
                .map { getFixedType(it) }
                .toList()
        }

    source.types()
        .flatMap {
            (it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS))
                .flatMap {
                    it.optFlatMap(PARAMETERS) + if (it.has(RETURNS)) {
                        sequenceOf(it[RETURNS])
                    } else {
                        emptySequence()
                    }
                }
                .plus(it.optFlatMap(PROPERTIES))
        }
        .forEach { it.fixType() }

    source.functionSignatures
        .run {
            keySet().asSequence().map {
                getJSONObject(it)
            }
        }
        .optFlatMap(PARAMETERS)
        .forEach { it.fixType() }
}

private fun fixOptionTypes(source: VsdxSource) {
    source.type("CachingMasterProvider")
        .flatMap(CONSTRUCTORS)
        .single()
        .apply {
            parameter("optionsOrNodeStyleType").apply {
                set(NAME, "nodeStyleType")
                set(TYPE, "$YCLASS<yfiles.styles.INodeStyle>")
            }

            parameter("edgeStyleType")
                .addGeneric("yfiles.styles.IEdgeStyle")
            parameter("portStyleType")
                .addGeneric("yfiles.styles.IPortStyle")
            parameter("labelStyleType")
                .addGeneric("yfiles.styles.ILabelStyle")
        }

    source.type("CustomEdgeProvider")
        .flatMap(CONSTRUCTORS)
        .single()
        .parameter("edgeStyleType")
        .addGeneric("yfiles.styles.IEdgeStyle")

    source.type("VssxStencilProviderFactory") {
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
    source.type("Value") {
        method("fetch")
            .apply {
                setSingleTypeParameter("TValue")
                firstParameter[NAME] = "o"
            }

        method("formula")
            .setSingleTypeParameter("TValue")
    }

    source.functionSignature("yfiles.vsdx.ComparisonFunction")
        .setSingleTypeParameter("T")
}

private fun fixMethodModifier(source: VsdxSource) {
    source.types("IMasterProvider", "IShapeProcessingStep")
        .flatMap(METHODS)
        .forEach { it[MODIFIERS].put(ABSTRACT) }
}

private val YFILES_API_REGEX = Regex("""<api-link data-type="([a-zA-Z.]+)"\s*></api-link>""")
private val OLD_API_REGEX = Regex("""\{@link[\r\n\s]{0,3}([a-zA-Z.#]+)}""")
private val P_START_REGEX = Regex("""<p>\r\n\s{3,}""")
private val PRE_REGEX = Regex("""<pre>([\s\S]+?)</pre>""")

private val STANDARD_TYPE_MAP = sequenceOf(
    "Window",
    "Styles",
    "Promise",

    "addExportFinishedListener",

    "MasterProviderContext.coordinateConverter",
    "Page.pageHeight",
    "Page.pageWidth",
    "ShapeProcessingContext.coordinateConverter",
    "SvgSupport.applySvg",
    "VsdxExportConfiguration.margins",
    "VsdxIO.addGraph",
).associateBy { it }
    .plus(JS_BLOB to BLOB)
    .plus("GraphComponent.contentRect" to "yfiles.view.GraphComponent.contentRect")

private fun JSONObject.fixSummary() {
    if (!has(SUMMARY)) {
        return
    }

    val summary = get(SUMMARY)
        .replace(OLD_API_REGEX) {
            val member = it.groupValues[1]
                .replace("#", ".")

            "[$member]"
        }
        .replace(YFILES_API_REGEX) {
            val dataType = it.groupValues[1]
            val type = when {
                "." !in dataType && dataType.first().isLowerCase()
                -> dataType

                dataType.startsWith("vsdx.")
                -> dataType.removePrefix("vsdx.")

                dataType == "Connect.fromCell" || dataType == "Connect.toCell"
                -> "String"

                dataType == "Connect.fromPart" || dataType == "Connect.toPart"
                -> "Int"

                dataType == "Connect.fromSheet" || dataType == "Connect.toSheet"
                -> "Shape"

                else -> YFILES_TYPE_MAP[dataType] ?: STANDARD_TYPE_MAP.getValue(dataType)
            }

            "[$type]"
        }
        .replace(PRE_REGEX) {
            val code = it.groupValues[1]
                .replace("\r\n", "\n")

            "\n```$code```\n\n"
        }
        .replace(P_START_REGEX, "\n")
        .replace("<br>\r\n", "\n")
        .replace("\r\n", " ")
        .replace("\r", " ")
        .replace("</p>", "")
        .replace("<p>", "\n\n")
        .replace("\" ></api-link", "\"></api-link")

    set(SUMMARY, summary)
}

private fun fixSummary(source: VsdxSource) {
    source.type("VsdxExport")
        .flatMap(METHODS)
        .flatMap(PARAMETERS)
        .filter { it.has(SUMMARY) }
        .forEach {
            it[SUMMARY] = it[SUMMARY]
                .replace("""data-member="createDefault()"""", """data-member="createDefault"""")
        }

    source.types()
        .onEach { it.fixSummary() }
        .onEach { it.optFlatMap(PROPERTIES).forEach { it.fixSummary() } }
        .flatMap { it.optFlatMap(CONSTRUCTORS) + it.optFlatMap(METHODS) }
        .onEach { it.fixSummary() }
        .flatMap { it.optFlatMap(PARAMETERS) }
        .forEach { it.fixSummary() }

    source.types()
        .optFlatMap(METHODS)
        .filter { it.has(RETURNS) }
        .map { it[RETURNS] }
        .filter { it.has(DOC) }
        .forEach {
            it[DOC] = it[DOC]
                .replace("\r", " ")
        }
}
