package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE

internal object KotlinHacks {
    private val CLONE_OVERRIDE = "override fun clone(): ${OBJECT_TYPE} = definedExternally"

    // yfiles.api.json correction required
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.graph.GenericLabelModel"
            -> lines(
                "override fun canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean = definedExternally",
                "override fun getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter> = definedExternally",
                "override fun convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension = definedExternally",
                "override fun getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle = definedExternally"
            )

            className == "yfiles.graph.GenericPortLocationModel"
            -> lines(
                "override fun canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean = definedExternally",
                "override fun convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension = definedExternally",
                "override fun getEnumerator(): yfiles.collections.IEnumerator<IPortLocationModelParameter> = definedExternally"
            )

            className == "yfiles.input.PortRelocationHandleProvider"
            -> "override fun getHandle(context: IInputModeContext, edge: yfiles.graph.IEdge, sourceHandle: Boolean): IHandle = definedExternally"

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