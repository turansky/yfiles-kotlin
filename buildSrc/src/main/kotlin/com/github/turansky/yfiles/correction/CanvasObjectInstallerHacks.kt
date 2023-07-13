package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

internal fun applyCanvasObjectInstallerHacks(source: Source) {
    source.type("ICanvasObjectInstaller")
        .method("create")
        .get(RETURNS)
        .addGeneric("T")

    source.types()
        .filter { it[ID].run { startsWith("yfiles.view.") && endsWith("Installer") } }
        .filter { it[GROUP] == "interface" }
        .onEach {
            it.setSingleTypeParameter(
                name = if (it[ID] == ICANVAS_OBJECT_INSTALLER) "in T" else "T",
                bound = IMODEL_ITEM
            )
        }
        .onEach { it.fixUserObjectType("T") }
        .filter { it.has(IMPLEMENTS) }
        .onEach { require(it[IMPLEMENTS].single() == ICANVAS_OBJECT_INSTALLER) }
        .forEach { it[IMPLEMENTS] = arrayOf("$ICANVAS_OBJECT_INSTALLER<T>") }

    source.types()
        .filter { it[ID].run { startsWith("yfiles.graph.") && endsWith("Decorator") } }
        .optFlatMap(PROPERTIES)
        .filter { it[TYPE].endsWith("Installer>") }
        .forEach {
            it[TYPE] = it[TYPE].run {
                val typeParameter = this.between("<", ",")
                replace(">", "<$typeParameter>>")
            }
        }

    source.types()
        .filter { it[ID].run { startsWith("yfiles.view.") && endsWith("Installer") } }
        .filter { it[GROUP] == "class" }
        .forEach {
            val name = it[NAME]
            val typeParameter = when {
                name.startsWith("Node") -> INODE
                name.startsWith("Edge") -> IEDGE
                name.startsWith("Port") -> IPORT
                name.startsWith("Label") -> ILABEL

                else -> {
                    it.setSingleTypeParameter(bound = IMODEL_ITEM)
                    "T"
                }
            }

            if (it.has(IMPLEMENTS)) {
                it[IMPLEMENTS] = it[IMPLEMENTS].map { "$it<$typeParameter>" }
            }

            it.fixUserObjectType(typeParameter)
        }

    source.types()
        .filter { it[ID].run { startsWith("yfiles.view.") && endsWith("Manager") } }
        .optFlatMap(METHODS)
        .filter { it[NAME] == "getInstaller" }
        .forEach { it[RETURNS].addGeneric(it.firstParameter[TYPE]) }
}

private val ITEM_NAMES = setOf(
    "item",
    "userObject"
)

private fun JSONObject.fixUserObjectType(type: String) {
    optFlatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] in ITEM_NAMES }
        .onEach { it.changeNullability(false) }
        .forEach { it[TYPE] = type }
}
