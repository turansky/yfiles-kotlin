package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.IENUMERABLE
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT
import com.github.turansky.yfiles.YOBJECT
import com.github.turansky.yfiles.json.removeAllObjects

internal fun applyLayoutStrictTypes(source: Source) {
    source.types("CopiedLayoutGraph", "LayoutGraphAdapter")
        .flatMap(METHODS)
        .flatMap { it.optFlatMap(PARAMETERS) + it.returnsSequence() }
        .filter { it[TYPE] == JS_OBJECT }
        .forEach { it[TYPE] = YOBJECT }

    source.type("LayoutGraphAdapter")
        .let { type -> sequenceOf("nodeObjects", "edgeObjects").map { type.method(it) } }
        .map { it[RETURNS] }
        .onEach { check(it[TYPE] == "$IENUMERABLE<$JS_ANY>") }
        .forEach { it[TYPE] = "$IENUMERABLE<$YOBJECT>" }

    source.type("LayoutGraphUtilities")[METHODS].removeAllObjects {
        it[ID] == "LayoutGraphUtilities-method-getBoundingBox(yfiles.layout.LayoutGraph,yfiles.algorithms.Node)"
    }
}
