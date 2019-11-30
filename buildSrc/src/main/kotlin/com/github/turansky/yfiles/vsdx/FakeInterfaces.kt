package com.github.turansky.yfiles.vsdx

import com.github.turansky.yfiles.Interface
import com.github.turansky.yfiles.correction.ID
import com.github.turansky.yfiles.correction.J_NAME
import com.github.turansky.yfiles.correction.METHODS
import com.github.turansky.yfiles.json.jObject

internal fun fakeVsdxInterfaces(): List<Interface> {
    return listOf(
        fakeVsdxInterface(
            id = "yfiles.collections.IEnumerable",
            methodNames = setOf("getEnumerator")
        ),
        fakeVsdxInterface(
            id = "yfiles.collections.IListEnumerable",
            methodNames = setOf("getEnumerator", "get")
        )
    )
}

private fun fakeVsdxInterface(id: String, methodNames: Set<String>): Interface {
    return Interface(
        jObject(
            ID to id,
            J_NAME to id,
            METHODS to methodNames.map {
                jObject(
                    J_NAME to it
                )
            }
        )
    )
}
