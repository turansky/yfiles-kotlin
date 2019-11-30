package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICOMPARABLE
import org.json.JSONObject

internal fun applyComparableHacks(source: Source) {
    source.type("IComparable")
        .apply {
            setSingleTypeParameter(bound = "IComparable<T>")
            configureCompareTo("T")
        }

    source.types(
        "ColumnDescriptor",
        "PartitionCellIdEntry",
        "RowDescriptor",
        "SnapResult",
        "SwimlaneDescriptor",
        "TimeSpan",
        "YDimension",
        "YPoint"
    )
        .forEach {
            val id = it[ID]

            val implements = it[IMPLEMENTS]
            implements.put(
                implements.indexOf(ICOMPARABLE),
                "$ICOMPARABLE<$id>"
            )

            it.configureCompareTo(id)
        }

    source.types(
        "IComparer",
        "DefaultOutEdgeComparer",
        "NodeOrderComparer",
        "NodeWeightComparer"
    ).jsequence(METHODS)
        .filter { it[J_NAME] == "compare" }
        .jsequence(PARAMETERS)
        .forEach { it.changeNullability(false) }
}

private fun JSONObject.configureCompareTo(type: String) {
    jsequence(METHODS)
        .filter { it[J_NAME] == "compareTo" }
        .singleOrNull()
        ?.apply {
            firstParameter.apply {
                set(J_NAME, "other")
                set(J_TYPE, type)
                changeNullability(false)
            }
        }
}
