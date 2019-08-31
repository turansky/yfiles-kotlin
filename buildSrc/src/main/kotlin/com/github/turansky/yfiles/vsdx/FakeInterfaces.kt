package com.github.turansky.yfiles.vsdx

import com.github.turansky.yfiles.Interface
import com.github.turansky.yfiles.correction.J_ID
import com.github.turansky.yfiles.correction.J_METHODS
import com.github.turansky.yfiles.correction.J_NAME
import com.github.turansky.yfiles.json.jObject

internal fun fakeVsdxInterfaces(): List<Interface> {
    return listOf(
        fakeVsdxInterface("yfiles.collections.IEnumerable", "getEnumerator"),
        fakeVsdxInterface("yfiles.collections.IListEnumerable", "get")
    )
}

private fun fakeVsdxInterface(id: String, methodName: String): Interface {
    return Interface(
        jObject(
            J_ID to id,
            J_NAME to id,
            J_METHODS to arrayOf(
                jObject(
                    J_NAME to methodName
                )
            )
        )
    )
}
