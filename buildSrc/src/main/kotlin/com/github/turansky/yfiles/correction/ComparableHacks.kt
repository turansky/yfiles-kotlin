package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICOMPARABLE
import org.json.JSONObject

internal fun applyComparableHacks(source: Source) {
    source.type("IComparable") {
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
        ).flatMap(METHODS)
        .filter { it[NAME] == "compare" }
        .flatMap(PARAMETERS)
        .forEach { it.changeNullability(false) }
}

private fun JSONObject.configureCompareTo(type: String) {
    flatMap(METHODS)
        .filter { it[NAME] == "compareTo" }
        .singleOrNull()
        ?.apply {
            firstParameter.apply {
                set(NAME, "other")
                set(TYPE, type)
                changeNullability(false)
            }
        }
}
