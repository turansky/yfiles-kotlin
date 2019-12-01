package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICOLLECTION
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import org.json.JSONObject

private fun collection(generic: String): String =
    "$ICOLLECTION<$generic>"

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
    }.forEach { it.fixTypeGeneric("yfiles.layout.PortCandidate") }

    source.type("PartitionGrid")
        .flatMap(METHODS)
        .single { it[ID] == "PartitionGrid-method-createCellSpanId(yfiles.collections.ICollection,yfiles.collections.ICollection)" }
        .apply {
            firstParameter.fixTypeGeneric("yfiles.layout.RowDescriptor")
            secondParameter.fixTypeGeneric("yfiles.layout.ColumnDescriptor")
        }
}

private fun JSONObject.fixTypeGeneric(generic: String) {
    require(get(TYPE) in DEFAULT_COLLECTIONS)

    set(TYPE, collection(generic))
}
