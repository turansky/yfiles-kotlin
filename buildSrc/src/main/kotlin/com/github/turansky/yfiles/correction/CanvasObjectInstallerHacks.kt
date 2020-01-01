package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ICANVAS_OBJECT_INSTALLER
import com.github.turansky.yfiles.IMODEL_ITEM
import org.json.JSONObject

internal fun applyCanvasObjectInstallerHacks(source: Source) {
    source.types()
        .filter { it[ID].run { startsWith("yfiles.view.") && endsWith("Installer") } }
        .filter { it[GROUP] == "interface" }
        .onEach {
            it.setSingleTypeParameter(
                name = if (it[ID] == ICANVAS_OBJECT_INSTALLER) "in T" else "T",
                bound = IMODEL_ITEM
            )

            it.fixUserObjectType("T")
        }
        .filter { it.has(IMPLEMENTS) }
        .onEach { require(it[IMPLEMENTS].single() == ICANVAS_OBJECT_INSTALLER) }
        .forEach { it[IMPLEMENTS] = arrayOf("$ICANVAS_OBJECT_INSTALLER<T>") }
}

private fun JSONObject.fixUserObjectType(type: String) {
    optFlatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] == "item" }
        .onEach { it.changeNullability(false) }
        .forEach { it[TYPE] = type }
}
