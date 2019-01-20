package com.yworks.yfiles.api.generator

internal object JavaHacks {
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.view.DefaultPortCandidateDescriptor"
            -> lines(
                "@Override public native Visual createVisual(IRenderContext context);",
                "@Override public native Visual updateVisual(IRenderContext context, Visual oldVisual);",
                "@Override public native boolean isInBox(yfiles.input.IInputModeContext context, yfiles.geometry.Rect rectangle);",
                "@Override public native boolean isVisible(ICanvasContext context, yfiles.geometry.Rect rectangle);",
                "@Override public native yfiles.geometry.Rect getBounds(ICanvasContext context);",
                "@Override public native boolean isHit(yfiles.input.IInputModeContext context, yfiles.geometry.Point location);",
                "@Override public native boolean isInPath(yfiles.input.IInputModeContext context, yfiles.geometry.GeneralPath lassoPath);"
            )

            else -> ""
        }

        result = result
            .replace("(yfiles.", "(com.yworks.yfiles.")
            .replace(" yfiles.", " com.yworks.yfiles.")

        return result
    }

    private fun lines(vararg lines: String): String {
        return lines.joinToString("\n")
    }
}