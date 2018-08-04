package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.OBJECT_TYPE

internal object JavaHacks {
    // yfiles.api.json correction required
    private val CLONE_REQUIRED = listOf(
        "yfiles.geometry.Matrix",
        "yfiles.geometry.MutablePoint",
        "yfiles.geometry.MutableSize"
    )

    private val CLONE_OVERRIDE = "@Override public native ${OBJECT_TYPE} clone();"

    // yfiles.api.json correction required
    fun getAdditionalContent(cn: String): String {
        val className = cn.removePrefix("com.yworks.")

        var result = when {
            className == "yfiles.algorithms.YList"
            -> lines(
                "@jsinterop.annotations.JsProperty(name=\"isReadOnly\")",
                "@Override",
                "public native boolean isReadOnly();",
                "@Override public native void add(${OBJECT_TYPE} item);"
            )


            className in CLONE_REQUIRED
            -> CLONE_OVERRIDE

            className == "yfiles.graph.CompositeUndoUnit"
            -> lines(
                "@Override public native boolean tryMergeUnit(IUndoUnit unit);",
                "@Override public native boolean tryReplaceUnit(IUndoUnit unit);"
            )

            className == "yfiles.graph.EdgePathLabelModel" || className == "yfiles.graph.EdgeSegmentLabelModel"
            -> lines(
                "@Override public native ILabelModelParameter findBestParameter(ILabel label, ILabelModel model, yfiles.geometry.IOrientedRectangle layout);",
                "@Override public native yfiles.collections.IEnumerable<ILabelModelParameter> getParameters(ILabel label, ILabelModel model);",
                "@Override public native yfiles.geometry.IOrientedRectangle getGeometry(ILabel label, ILabelModelParameter layoutParameter);"
            )

            className == "yfiles.graph.FreeLabelModel"
            -> "@Override public native ILabelModelParameter findBestParameter(ILabel label, ILabelModel model, yfiles.geometry.IOrientedRectangle layout);"

            className == "yfiles.graph.GenericLabelModel"
            -> lines(
                "@Override public native boolean canConvert(yfiles.graphml.IWriteContext context, ${OBJECT_TYPE} value);",
                "@Override public native yfiles.collections.IEnumerable<ILabelModelParameter> getParameters(ILabel label, ILabelModel model);",
                "@Override public native yfiles.graphml.MarkupExtension convert(yfiles.graphml.IWriteContext context, ${OBJECT_TYPE} value);",
                "@Override public native yfiles.geometry.IOrientedRectangle getGeometry(ILabel label, ILabelModelParameter layoutParameter);"
            )

            className == "yfiles.graph.GenericPortLocationModel"
            -> lines(
                "@Override public native boolean canConvert(yfiles.graphml.IWriteContext context, ${OBJECT_TYPE} value);",
                "@Override public native yfiles.graphml.MarkupExtension convert(yfiles.graphml.IWriteContext context, ${OBJECT_TYPE} value);",
                "@Override public native yfiles.collections.IEnumerator<IPortLocationModelParameter> getEnumerator();"
            )

            className == "yfiles.input.PortRelocationHandleProvider"
            -> "@Override public native IHandle getHandle(IInputModeContext context, yfiles.graph.IEdge edge, boolean sourceHandle);"

            className == "yfiles.styles.Arrow"
            -> lines(
                "@jsinterop.annotations.JsProperty(name=\"length\")",
                "@Override",
                "public native double getLength();",
                "@Override public native yfiles.view.IBoundsProvider getBoundsProvider(yfiles.graph.IEdge edge, boolean atSource, yfiles.geometry.Point anchor, yfiles.geometry.Point directionVector);",
                "@Override public native yfiles.view.IVisualCreator getVisualCreator(yfiles.graph.IEdge edge, boolean atSource, yfiles.geometry.Point anchor, yfiles.geometry.Point direction);",
                CLONE_OVERRIDE
            )

            className == "yfiles.styles.GraphOverviewSvgVisualCreator" || className == "yfiles.view.GraphOverviewCanvasVisualCreator"
            -> lines(
                "@Override public native yfiles.view.Visual createVisual(yfiles.view.IRenderContext context);",
                "@Override public native yfiles.view.Visual updateVisual(yfiles.view.IRenderContext context, yfiles.view.Visual oldVisual);"
            )

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

            className == "yfiles.styles.VoidPathGeometry"
            -> lines(
                "@Override public native yfiles.geometry.GeneralPath getPath();",
                "@Override public native double getSegmentCount();",
                "@Override public native yfiles.geometry.Tangent getTangent(double ratio);",
                "@Override public native yfiles.geometry.Tangent getTangent(double segmentIndex, double ratio);"
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