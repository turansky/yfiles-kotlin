package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext

private const val TOOL_TIP_CONTENT = "yfiles.view.ToolTipContent"

internal fun generateTooltipUtils(context: GeneratorContext) {
    // language=kotlin
    context[TOOL_TIP_CONTENT] = """
            |package yfiles.view
            |
            |import org.w3c.dom.HTMLElement
            |
            |external interface ToolTipContent
            |
            |fun ToolTipContent(source:HTMLElement):ToolTipContent = 
            |    source.unsafeCast<ToolTipContent>()
            |
            |fun ToolTipContent(source:String):ToolTipContent = 
            |    source.unsafeCast<ToolTipContent>()
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
