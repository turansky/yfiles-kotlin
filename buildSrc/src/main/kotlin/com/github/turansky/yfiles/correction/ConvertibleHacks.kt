package com.github.turansky.yfiles.correction

private val CONVERTIBLE_REGEX = Regex("(\\w+)Convertible")

internal fun fixConvertibles(source: Source) {
    source.types()
        .optFlatMap(METHODS)
        .optFlatMap(PARAMETERS)
        .filter { it.has(TYPE) && "Convertible" in it[TYPE] }
        .forEach {
            var type = it[TYPE]
            generateSequence { CONVERTIBLE_REGEX.find(type) }
                .forEach { match ->
                    type = type.replace(match.value, source.type(match.groupValues[1])[ID])
                }
            it[TYPE] = type
        }
}