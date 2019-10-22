package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.COLLECTION
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import org.json.JSONObject

private fun collection(generic: String): String =
    "$COLLECTION<$generic>"

private val DEFAULT_COLLECTIONS = setOf(
    collection(JS_ANY),
    collection(JS_OBJECT)
)

internal fun applyCollectionHacks(source: Source) {
    source.types(
        "PathRequest",
        "IEdgeData"
    ).flatMap {
        sequenceOf(
            it.property("sourcePortCandidates"),
            it.property("targetPortCandidates")
        )
    }
        .forEach { it.fixTypeGeneric("yfiles.layout.PortCandidate") }
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(getString(J_TYPE) in DEFAULT_COLLECTIONS)

    put(J_TYPE, collection(generic))
}