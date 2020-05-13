package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.HTML_ELEMENT

private const val TOOL_TIP_CONTENT = "yfiles.view.ToolTipContent"

internal fun generateTooltipUtils(context: GeneratorContext) {
    // language=kotlin
    context[TOOL_TIP_CONTENT] = """
            |external interface ToolTipContent
            |
            |fun ToolTipContent(source:$HTML_ELEMENT):$TOOL_TIP_CONTENT = 
            |    source.unsafeCast<$TOOL_TIP_CONTENT>()
            |
            |fun ToolTipContent(source:String):$TOOL_TIP_CONTENT = 
            |    source.unsafeCast<$TOOL_TIP_CONTENT>()
        """.trimMargin()
}

internal fun applyTooltipHacks(source: Source) {
    source.type("ToolTip").apply {
        property("content")[TYPE] = TOOL_TIP_CONTENT

        flatMap(METHODS)
            .optFlatMap(PARAMETERS)
            .filter { it[NAME].endsWith("Content") }
            .forEach { it[TYPE] = TOOL_TIP_CONTENT }
    }

    source.type("ToolTipQueryEventArgs")
        .property("toolTip")[TYPE] = TOOL_TIP_CONTENT

    source.type("MouseHoverInputMode").apply {
        method("getToolTipContent")[RETURNS][TYPE] = TOOL_TIP_CONTENT

        flatMap(METHODS)
            .optFlatMap(PARAMETERS)
            .filter { it[NAME] == "content" }
            .forEach { it[TYPE] = TOOL_TIP_CONTENT }
    }
}
