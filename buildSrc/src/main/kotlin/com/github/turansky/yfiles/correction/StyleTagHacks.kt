package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.INLINE
import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY
import com.github.turansky.yfiles.JS_OBJECT

private const val STYLE_TAG = "yfiles.styles.StyleTag"

internal fun generateStyleTagUtils(context: GeneratorContext) {
    // language=kotlin
    context[STYLE_TAG, INLINE] = """
            |external interface StyleTag
            |
            |inline fun StyleTag(source:Any):StyleTag = 
            |    source.unsafeCast<StyleTag>()
        """.trimMargin()
}

internal fun applyStyleTagHacks(source: Source) {
    val likeObjectTypes = setOf(
        JS_OBJECT,
        JS_ANY
    )

    source.types()
        .optFlatMap(PROPERTIES)
        .filter { it[NAME] == "styleTag" }
        .filter { it[TYPE] in likeObjectTypes }
        .forEach { it[TYPE] = STYLE_TAG }
}
