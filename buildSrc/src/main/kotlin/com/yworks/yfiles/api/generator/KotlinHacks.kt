package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE

internal object KotlinHacks {
    private val CLONE_OVERRIDE = "override fun clone(): ${OBJECT_TYPE} = definedExternally"

    // yfiles.api.json correction required
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.styles.Arrow"
            -> lines(
                "override val length: Number",
                "    get() = definedExternally",
                "override fun getBoundsProvider(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, directionVector: yfiles.geometry.Point): yfiles.view.IBoundsProvider = definedExternally",
                "override fun getVisualCreator(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, direction: yfiles.geometry.Point): yfiles.view.IVisualCreator = definedExternally",
                CLONE_OVERRIDE
            )

            className == "yfiles.styles.GraphOverviewSvgVisualCreator" || className == "yfiles.view.GraphOverviewCanvasVisualCreator"
            -> lines(
                "override fun createVisual(context: yfiles.view.IRenderContext): yfiles.view.Visual = definedExternally",
                "override fun updateVisual(context: yfiles.view.IRenderContext, oldVisual: yfiles.view.Visual): yfiles.view.Visual = definedExternally"
            )

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