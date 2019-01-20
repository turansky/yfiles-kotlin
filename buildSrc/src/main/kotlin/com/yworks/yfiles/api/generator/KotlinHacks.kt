package com.yworks.yfiles.api.generator

internal object KotlinHacks {
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.view.DefaultPortCandidateDescriptor"
            -> lines(
                "override fun createVisual(context: IRenderContext): Visual = definedExternally",
                "override fun updateVisual(context: IRenderContext, oldVisual: Visual): Visual = definedExternally",
                "override fun isInBox(context: yfiles.input.IInputModeContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                "override fun isVisible(context: ICanvasContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                "override fun getBounds(context: ICanvasContext): yfiles.geometry.Rect = definedExternally",
                "override fun isHit(context: yfiles.input.IInputModeContext, location: yfiles.geometry.Point): Boolean = definedExternally",
                "override fun isInPath(context: yfiles.input.IInputModeContext, lassoPath: yfiles.geometry.GeneralPath): Boolean = definedExternally"
            )

            else -> ""
        }

        result = result.replace(": yfiles.", ": com.yworks.yfiles.")

        return result
    }

    private fun lines(vararg lines: String): String {
        return lines.joinToString("\n")
    }
}