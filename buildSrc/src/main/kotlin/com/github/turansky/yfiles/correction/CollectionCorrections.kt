package com.github.turansky.yfiles.correction

internal fun fixCollections(source: Source) {
    fixConstantGenerics(source)
}

private fun fixConstantGenerics(source: Source) {
    source.type("IEnumerable") {
        constant("EMPTY").replaceInType("<T>", "<*>")
    }
}