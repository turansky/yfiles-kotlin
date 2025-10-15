package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICOMPARABLE
import org.json.JSONObject

internal fun applyComparableHacks(source: Source) {
    source.type("IComparable") {
        setSingleTypeParameter(bound = "IComparable<T>")
        configureCompareTo("T")
    }

    source.types(
        "LayoutGridCell",
        "LayoutGridColumn",
        "LayoutGridRow",
        "SnapResult",
        "TimeSpan",
        "Point"
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
