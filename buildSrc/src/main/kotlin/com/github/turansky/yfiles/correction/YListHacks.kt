package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

private fun ylist(generic: String): String =
    "$YLIST<$generic>"

internal fun applyYListHacks(source: Source) {
    fixYList(source)
    fixMethodParameter(source)

    source.types("YList")
        .flatMap(CONSTANTS)
        .forEach {
            it[TYPE] = it[TYPE].replace("<T>", "<*>")
        }
}

private fun fixYList(source: Source) {
    source.type("YList")
        .fixGeneric()
}

private fun JSONObject.fixGeneric() {
    setSingleTypeParameter()

    get(IMPLEMENTS).apply {
        put(0, getString(0).replace("<$JS_ANY>", "<T>"))
    }

    (flatMap(CONSTRUCTORS) + flatMap(METHODS))
        .flatMap { it.optFlatMap(PARAMETERS) + it.returnsSequence() }
        .plus(flatMap(PROPERTIES))
        .forEach {
            val newType = when (val type = it[TYPE]) {
                JS_ANY, JS_OBJECT -> "T"
                ICURSOR -> "$ICURSOR<T>"

                else -> type
                    .replace("<$JS_ANY>", "<T>")
                    .replace("<$JS_OBJECT>", "<T>")
            }
            it[TYPE] = newType
        }
}

private fun fixMethodParameter(source: Source) {
    source.types(
        "YList",
        "LayoutGraph",
        "MultiPageLayout"
    ).optFlatMap(METHODS)
        .forEach {
            val methodName = it[NAME]

            it.optFlatMap(PARAMETERS)
                .filter { it[TYPE] == YLIST }
                .forEach {
                    val generic = getGeneric(methodName, it[NAME])
                    it.fixTypeGeneric(generic)
                }
        }
}

private fun getGeneric(
    methodName: String,
    parameterName: String,
): String {
    when {
        methodName == "splice" && parameterName == "list" ->
            return "T"

        methodName == "setNodeOrder" && parameterName == "list" ->
            return NODE

        methodName == "createSegmentInfoComparer" && parameterName == "channels" ->
            return "yfiles.router.Channel"
    }

    return when (parameterName) {
        "nodeLabels" -> INODE_LABEL_LAYOUT
        "edgeLabels" -> IEDGE_LABEL_LAYOUT
        "selfLoops" -> EDGE

        "edgeIds", "originalEdgeIds" -> YID

        else -> throw IllegalStateException("No generic found!")
    }
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(TYPE) == YLIST)

    set(TYPE, ylist(generic))
}
