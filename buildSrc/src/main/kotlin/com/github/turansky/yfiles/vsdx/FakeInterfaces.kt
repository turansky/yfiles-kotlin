package com.github.turansky.yfiles.vsdx

import com.github.turansky.yfiles.IENUMERABLE
import com.github.turansky.yfiles.ILIST_ENUMERABLE
import com.github.turansky.yfiles.Interface
import com.github.turansky.yfiles.correction.ID
import com.github.turansky.yfiles.correction.METHODS
import com.github.turansky.yfiles.correction.NAME
import com.github.turansky.yfiles.json.jObject

internal fun fakeVsdxInterfaces(): List<Interface> {
    return listOf(
        fakeVsdxInterface(
            id = IENUMERABLE,
            methodNames = setOf("getEnumerator")
        ),
        fakeVsdxInterface(
            id = ILIST_ENUMERABLE,
            methodNames = setOf("getEnumerator", "get")
        )
    )
}

private fun fakeVsdxInterface(id: String, methodNames: Set<String>): Interface {
    return Interface(
        jObject(
            ID to id,
            NAME to id,
            METHODS to methodNames.map {
                jObject(
                    NAME to it
                )
            }
        )
    )
}
