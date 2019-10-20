package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

private fun ylist(generic: String): String =
    "$YLIST<$generic>"

internal fun applyYListHacks(source: Source) {
    fixYList(source)
    fixMethodParameter(source)
    fixReturnType(source)
}

private fun fixYList(source: Source) {
    source.type("YList")
        .fixGeneric()

    source.type("YNodeList")
        .addExtendsGeneric(NODE)

    source.type("EdgeList")
        .addExtendsGeneric(EDGE)
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter(bound = JS_ANY)

    getJSONArray(J_IMPLEMENTS).apply {
        put(0, getString(0).replace("<$JS_ANY>", "<T>"))
    }

    (jsequence(J_CONSTRUCTORS) + jsequence(J_METHODS))
        .flatMap { it.optJsequence(J_PARAMETERS) + it.returnsSequence() }
        .plus(jsequence(J_PROPERTIES))
        .forEach {
            val type = it.getString(J_TYPE)
            val newType = if (type == JS_ANY || type == JS_OBJECT) {
                "T"
            } else {
                type.replace("<$JS_ANY>", "<T>")
                    .replace("<$JS_OBJECT>", "<T>")
            }
            it.put(J_TYPE, newType)
        }
}

private fun JSONObject.returnsSequence(): Sequence<JSONObject> =
    if (has(J_RETURNS)) {
        sequenceOf(getJSONObject(J_RETURNS))
    } else {
        emptySequence()
    }

private fun fixMethodParameter(source: Source) {
    source.types(
        "OrthogonalPatternEdgeRouter"
    ).flatMap { it.jsequence(J_METHODS) + it.optJsequence(J_STATIC_METHODS) + it.optJsequence(J_CONSTRUCTORS) }
        .forEach {
            val methodName = it.getString(J_NAME)

            it.optJsequence(J_PARAMETERS)
                .filter { it.getString(J_TYPE) == YLIST }
                .forEach {
                    val generic = getGeneric(methodName, it.getString(J_NAME))
                    it.fixTypeGeneric(generic)
                }
        }
}

private fun getGeneric(
    methodName: String,
    parameterName: String
): String {
    println(methodName)

    return when (parameterName) {
        "path" -> YPOINT
        else -> throw IllegalStateException("No generic found!")
    }
}

private fun fixReturnType(source: Source) {
    sequenceOf(
        "INodeLabelLayoutModel" to NODE_LABEL_CANDIDATE,
        "DiscreteNodeLabelLayoutModel" to NODE_LABEL_CANDIDATE,
        "FreeNodeLabelLayoutModel" to NODE_LABEL_CANDIDATE,

        "IEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE,
        "DiscreteEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE,
        "FreeEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE,
        "SliderEdgeLabelLayoutModel" to EDGE_LABEL_CANDIDATE
    ).forEach { (className, generic) ->
        source.type(className)
            .method("getLabelCandidates")
            .fixReturnTypeGeneric(generic)
    }
}

private fun JSONObject.fixReturnTypeGeneric(generic: String) {
    getJSONObject(J_RETURNS)
        .fixTypeGeneric(generic)
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(getString(J_TYPE) == YLIST)

    put(J_TYPE, ylist(generic))
}