package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE

internal object JavaHacks {
    // yfiles.api.json correction required
    private val CLONE_REQUIRED = listOf(
        "yfiles.geometry.Matrix",
        "yfiles.geometry.MutablePoint",
        "yfiles.geometry.MutableSize"
    )

    private val CLONE_OVERRIDE = "@Override public native ${OBJECT_TYPE} void clone()"

    // yfiles.api.json correction required
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.algorithms.YList"
            -> lines(
                "@Override val isReadOnly: Boolean",
                "    get()",
                "@Override fun add(item: ${OBJECT_TYPE})"
            )


            className in CLONE_REQUIRED
            -> CLONE_OVERRIDE

            className == "yfiles.graph.CompositeUndoUnit"
            -> lines(
                "@Override public native boolean tryMergeUnit(unit: IUndoUnit)",
                "@Override public native boolean tryReplaceUnit(unit: IUndoUnit)"
            )

            className == "yfiles.graph.EdgePathLabelModel" || className == "yfiles.graph.EdgeSegmentLabelModel"
            -> lines(
                "@Override public native ILabelModelParameter findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle)",
                "@Override public native yfiles.collections.IEnumerable<ILabelModelParameter> getParameters(label: ILabel, model: ILabelModel)",
                "@Override public native yfiles.geometry.IOrientedRectangle getGeometry(label: ILabel, layoutParameter: ILabelModelParameter)"
            )

            className == "yfiles.graph.FreeLabelModel"
            -> "@Override public native ILabelModelParameter findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle)"

            className == "yfiles.graph.GenericLabelModel"
            -> lines(
                "@Override public native boolean canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE})",
                "@Override public native yfiles.collections.IEnumerable<ILabelModelParameter> getParameters(label: ILabel, model: ILabelModel)",
                "@Override public native yfiles.graphml.MarkupExtension convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE})",
                "@Override public native yfiles.geometry.IOrientedRectangle getGeometry(label: ILabel, layoutParameter: ILabelModelParameter)"
            )

            className == "yfiles.graph.GenericPortLocationModel"
            -> lines(
                "@Override public native boolean canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE})",
                "@Override public native yfiles.graphml.MarkupExtension convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE})",
                "@Override public native yfiles.collections.IEnumerator<IPortLocationModelParameter> getEnumerator()"
            )

            className == "yfiles.input.PortRelocationHandleProvider"
            -> "@Override public native IHandle getHandle(context: IInputModeContext, edge: yfiles.graph.IEdge, sourceHandle: boolean)"

            className == "yfiles.styles.Arrow"
            -> lines(
                "@Override val length: double",
                "    get()",
                "@Override public native yfiles.view.IBoundsProvider getBoundsProvider(edge: yfiles.graph.IEdge, atSource: boolean, anchor: yfiles.geometry.Point, directionVector: yfiles.geometry.Point)",
                "@Override public native yfiles.view.IVisualCreator getVisualCreator(edge: yfiles.graph.IEdge, atSource: boolean, anchor: yfiles.geometry.Point, direction: yfiles.geometry.Point)",
                CLONE_OVERRIDE
            )

            className == "yfiles.styles.GraphOverviewSvgVisualCreator" || className == "yfiles.view.GraphOverviewCanvasVisualCreator"
            -> lines(
                "@Override public native yfiles.view.Visual createVisual(context: yfiles.view.IRenderContext)",
                "@Override public native yfiles.view.Visual updateVisual(context: yfiles.view.IRenderContext, oldVisual: yfiles.view.Visual)"
            )

            className == "yfiles.view.DefaultPortCandidateDescriptor"
            -> lines(
                "@Override public native Visual createVisual(context: IRenderContext)",
                "@Override public native Visual updateVisual(context: IRenderContext, oldVisual: Visual)",
                "@Override public native boolean isInBox(context: yfiles.input.IInputModeContext, rectangle: yfiles.geometry.Rect)",
                "@Override public native boolean isVisible(context: ICanvasContext, rectangle: yfiles.geometry.Rect)",
                "@Override public native yfiles.geometry.Rect getBounds(context: ICanvasContext)",
                "@Override public native boolean isHit(context: yfiles.input.IInputModeContext, location: yfiles.geometry.Point)",
                "@Override public native boolean isInPath(context: yfiles.input.IInputModeContext, lassoPath: yfiles.geometry.GeneralPath)"
            )

            className == "yfiles.styles.VoidPathGeometry"
            -> lines(
                "@Override public native yfiles.geometry.GeneralPath getPath()",
                "@Override public native double getSegmentCount()",
                "@Override public native yfiles.geometry.Tangent getTangent(ratio: double)",
                "@Override public native yfiles.geometry.Tangent getTangent(segmentIndex: double, ratio: double)"
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