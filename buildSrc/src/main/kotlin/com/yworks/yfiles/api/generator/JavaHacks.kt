package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE

internal object JavaHacks {
    // yfiles.api.json correction required
    private val CLONE_REQUIRED = listOf(
        "yfiles.geometry.Matrix",
        "yfiles.geometry.MutablePoint",
        "yfiles.geometry.MutableSize"
    )

    private val CLONE_OVERRIDE = "@Override fun clone(): ${OBJECT_TYPE} = definedExternally"

    // yfiles.api.json correction required
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.algorithms.YList"
            -> lines(
                "@Override val isReadOnly: Boolean",
                "    get() = definedExternally",
                "@Override fun add(item: ${OBJECT_TYPE}) = definedExternally"
            )


            className in CLONE_REQUIRED
            -> CLONE_OVERRIDE

            className == "yfiles.graph.CompositeUndoUnit"
            -> lines(
                "@Override fun tryMergeUnit(unit: IUndoUnit): Boolean = definedExternally",
                "@Override fun tryReplaceUnit(unit: IUndoUnit): Boolean = definedExternally"
            )

            className == "yfiles.graph.EdgePathLabelModel" || className == "yfiles.graph.EdgeSegmentLabelModel"
            -> lines(
                "@Override fun findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter = definedExternally",
                "@Override fun getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter> = definedExternally",
                "@Override fun getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle = definedExternally"
            )

            className == "yfiles.graph.FreeLabelModel"
            -> "@Override fun findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter = definedExternally"

            className == "yfiles.graph.GenericLabelModel"
            -> lines(
                "@Override fun canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean = definedExternally",
                "@Override fun getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter> = definedExternally",
                "@Override fun convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension = definedExternally",
                "@Override fun getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle = definedExternally"
            )

            className == "yfiles.graph.GenericPortLocationModel"
            -> lines(
                "@Override fun canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean = definedExternally",
                "@Override fun convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension = definedExternally",
                "@Override fun getEnumerator(): yfiles.collections.IEnumerator<IPortLocationModelParameter> = definedExternally"
            )

            className == "yfiles.input.PortRelocationHandleProvider"
            -> "@Override fun getHandle(context: IInputModeContext, edge: yfiles.graph.IEdge, sourceHandle: Boolean): IHandle = definedExternally"

            className == "yfiles.styles.Arrow"
            -> lines(
                "@Override val length: Number",
                "    get() = definedExternally",
                "@Override fun getBoundsProvider(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, directionVector: yfiles.geometry.Point): yfiles.view.IBoundsProvider = definedExternally",
                "@Override fun getVisualCreator(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, direction: yfiles.geometry.Point): yfiles.view.IVisualCreator = definedExternally",
                CLONE_OVERRIDE
            )

            className == "yfiles.styles.GraphOverviewSvgVisualCreator" || className == "yfiles.view.GraphOverviewCanvasVisualCreator"
            -> lines(
                "@Override fun createVisual(context: yfiles.view.IRenderContext): yfiles.view.Visual = definedExternally",
                "@Override fun updateVisual(context: yfiles.view.IRenderContext, oldVisual: yfiles.view.Visual): yfiles.view.Visual = definedExternally"
            )

            className == "yfiles.view.DefaultPortCandidateDescriptor"
            -> lines(
                "@Override fun createVisual(context: IRenderContext): Visual = definedExternally",
                "@Override fun updateVisual(context: IRenderContext, oldVisual: Visual): Visual = definedExternally",
                "@Override fun isInBox(context: yfiles.input.IInputModeContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                "@Override fun isVisible(context: ICanvasContext, rectangle: yfiles.geometry.Rect): Boolean = definedExternally",
                "@Override fun getBounds(context: ICanvasContext): yfiles.geometry.Rect = definedExternally",
                "@Override fun isHit(context: yfiles.input.IInputModeContext, location: yfiles.geometry.Point): Boolean = definedExternally",
                "@Override fun isInPath(context: yfiles.input.IInputModeContext, lassoPath: yfiles.geometry.GeneralPath): Boolean = definedExternally"
            )

            className == "yfiles.styles.VoidPathGeometry"
            -> lines(
                "@Override fun getPath(): yfiles.geometry.GeneralPath = definedExternally",
                "@Override fun getSegmentCount(): Number = definedExternally",
                "@Override fun getTangent(ratio: Number): yfiles.geometry.Tangent = definedExternally",
                "@Override fun getTangent(segmentIndex: Number, ratio: Number): yfiles.geometry.Tangent = definedExternally"
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