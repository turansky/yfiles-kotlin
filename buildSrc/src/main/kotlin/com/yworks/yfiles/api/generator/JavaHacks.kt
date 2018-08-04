package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE

internal object JavaHacks {
    // yfiles.api.json correction required
    private val CLONE_REQUIRED = listOf(
        "yfiles.geometry.Matrix",
        "yfiles.geometry.MutablePoint",
        "yfiles.geometry.MutableSize"
    )

    private val CLONE_OVERRIDE = "@Override public native void clone(): ${OBJECT_TYPE}"

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
                "@Override public native tryMergeUnit(unit: IUndoUnit): Boolean",
                "@Override public native tryReplaceUnit(unit: IUndoUnit): Boolean"
            )

            className == "yfiles.graph.EdgePathLabelModel" || className == "yfiles.graph.EdgeSegmentLabelModel"
            -> lines(
                "@Override public native findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter",
                "@Override public native getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter>",
                "@Override public native getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle"
            )

            className == "yfiles.graph.FreeLabelModel"
            -> "@Override public native findBestParameter(label: ILabel, model: ILabelModel, layout: yfiles.geometry.IOrientedRectangle): ILabelModelParameter"

            className == "yfiles.graph.GenericLabelModel"
            -> lines(
                "@Override public native canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean",
                "@Override public native getParameters(label: ILabel, model: ILabelModel): yfiles.collections.IEnumerable<ILabelModelParameter>",
                "@Override public native convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension",
                "@Override public native getGeometry(label: ILabel, layoutParameter: ILabelModelParameter): yfiles.geometry.IOrientedRectangle"
            )

            className == "yfiles.graph.GenericPortLocationModel"
            -> lines(
                "@Override public native canConvert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): Boolean",
                "@Override public native convert(context: yfiles.graphml.IWriteContext, value: ${OBJECT_TYPE}): yfiles.graphml.MarkupExtension",
                "@Override public native getEnumerator(): yfiles.collections.IEnumerator<IPortLocationModelParameter>"
            )

            className == "yfiles.input.PortRelocationHandleProvider"
            -> "@Override public native getHandle(context: IInputModeContext, edge: yfiles.graph.IEdge, sourceHandle: Boolean): IHandle"

            className == "yfiles.styles.Arrow"
            -> lines(
                "@Override val length: Number",
                "    get()",
                "@Override public native getBoundsProvider(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, directionVector: yfiles.geometry.Point): yfiles.view.IBoundsProvider",
                "@Override public native getVisualCreator(edge: yfiles.graph.IEdge, atSource: Boolean, anchor: yfiles.geometry.Point, direction: yfiles.geometry.Point): yfiles.view.IVisualCreator",
                CLONE_OVERRIDE
            )

            className == "yfiles.styles.GraphOverviewSvgVisualCreator" || className == "yfiles.view.GraphOverviewCanvasVisualCreator"
            -> lines(
                "@Override public native createVisual(context: yfiles.view.IRenderContext): yfiles.view.Visual",
                "@Override public native updateVisual(context: yfiles.view.IRenderContext, oldVisual: yfiles.view.Visual): yfiles.view.Visual"
            )

            className == "yfiles.view.DefaultPortCandidateDescriptor"
            -> lines(
                "@Override public native createVisual(context: IRenderContext): Visual",
                "@Override public native updateVisual(context: IRenderContext, oldVisual: Visual): Visual",
                "@Override public native isInBox(context: yfiles.input.IInputModeContext, rectangle: yfiles.geometry.Rect): Boolean",
                "@Override public native isVisible(context: ICanvasContext, rectangle: yfiles.geometry.Rect): Boolean",
                "@Override public native getBounds(context: ICanvasContext): yfiles.geometry.Rect",
                "@Override public native isHit(context: yfiles.input.IInputModeContext, location: yfiles.geometry.Point): Boolean",
                "@Override public native isInPath(context: yfiles.input.IInputModeContext, lassoPath: yfiles.geometry.GeneralPath): Boolean"
            )

            className == "yfiles.styles.VoidPathGeometry"
            -> lines(
                "@Override public native getPath(): yfiles.geometry.GeneralPath",
                "@Override public native getSegmentCount(): Number",
                "@Override public native getTangent(ratio: Number): yfiles.geometry.Tangent",
                "@Override public native getTangent(segmentIndex: Number, ratio: Number): yfiles.geometry.Tangent"
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