package com.yworks.yfiles.api.generator

internal val DOUBLE_METHODS = setOf(
    "getNumber",
    "distanceSq",
    "distance",
    "getDistance",
    "distanceTo",
    "calculateLength",
    "sum",
    "findRayIntersection",
    "findLineIntersection",
    "scalarProduct",
    "distanceToSegment",
    "commitPositionsSmoothly",
    "manhattanDistanceTo",
    "calculateScaleForWidth",
    "calculateScaleForHeight",
    "getCenterX",
    "getCenterY",
    "getX",
    "getY",
    "getWidth",
    "getHeight",
    "getBridgeWidth",
    "getBridgeHeight",
    "getXOffsetForLayoutOrientation",
    "getYOffsetForLayoutOrientation",
    "getSmoothingLength",
    "getMinimumFirstSegmentLength",
    "getMinimumOctilinearSegmentLength",
    "getMinimumLastSegmentLength",
    "getMinimumDistance",
    "getMinimumNodeDistance",
    "getMinimumLayerHeight",
    "getLayerAlignment",
    "getMinDistance",
    "getProfit",
    "getValueAt",
    "getValue",
    "getMin",
    "getSlope",
    "getMax",
    "getMaxValue",
    "getMinValue",
    "getDistanceTo",
    "getLength"
)

internal val DOUBLE_PROPERTIES = setOf(
    "c",
    "min",
    "max",
    "minValue",
    "maxValue",
    "slope",
    "end",
    "dissimilarityValue",
    "distance",
    "top",
    "left",
    "bottom",
    "right",
    "xOffset",
    "deltaX",
    "deltaY",
    "x",
    "y",
    "width",
    "height",
    "minX",
    "minY",
    "maxX",
    "maxY",
    "centerX",
    "centerY",
    "anchorX",
    "anchorY",
    "upX",
    "upY",
    "angle",
    "initialAngle",
    "fixedRadius",
    "minimumRadius",
    "lastAppliedRadius",
    "spacingBetweenFamilyMembers",
    "offsetForFamilyNodes",
    "lastX",
    "lastY",
    "length",
    "currentEndPointX",
    "currentEndPointY",
    "verticalInsets",
    "horizontalInsets",
    "x2",
    "y2",
    "vectorLength",
    "squaredVectorLength",
    "area",
    "offset",
    "edgeOverlapPenalty",
    "nodeOverlapPenalty",
    "profit",
    "actualMinSize",
    "actualSize",
    "minimumSize",
    "yOffset",
    "ratio",
    "cropLength",
    "scale",
    "nodeScalingFactor",
    "maximumNodeSize",
    "minimumNodeSize",
    "nodeHalo",
    "desiredAspectRatio",
    "nodeToNodeDistance",
    "nodeToEdgeDistance",
    "edgeToEdgeDistance",
    "defaultPortBorderGapRatio",
    "minimumFirstSegmentLength",
    "minimumLastSegmentLength",
    "minimumLength",
    "minimumDistance",
    "maximumDistance",
    "minimumSlope",
    "minimumOctilinearSegmentLength",
    "minimumLayerDistance",
    "thickness",
    "layerAlignment",
    "minimumLayerHeight",
    "backLoopPenalty",
    "crossingPenalty",
    "overUsagePenalty",
    "minimumSublayerDistance",
    "swimLaneCrossingWeight",
    "laneTightness",
    "minimumLaneWidth",
    "leftLaneInset",
    "rightLaneInset",
    "computedLanePosition",
    "computedLaneWidth",
    "spacing",
    "snapDistance",
    "orthogonalSnapDistance",
    "preferredMinimalEdgeDistance",
    "nodeBorderWidthRatio",
    "sourcePortCandidateHitRadius",
    "gridSnapDistance",
    "snapLineExtension",
    "horizontalGridWidth",
    "verticalGridWidth",
    "finishRadius",
    "maximumSnapDistance",
    "weight",
    "customProfitModelRatio",
    "totalMilliseconds",
    "totalSeconds",
    "totalMinutes",
    "minimumWidth",
    "leftInset",
    "rightInset",
    "computedWidth",
    "originalWidth",
    "originalPosition",
    "computedPosition",
    "tightness",
    "componentSpacing",
    "maximumError",
    "straightControlPointRatio",
    "bundlingQuality",
    "bundlingStrength",
    "edgeSpacing",
    "targetRatio",
    "fixedWidth",
    "rotationAngle",
    "preferredHeight",
    "preferredWidth",
    "scaleFactorY",
    "scaleFactorX",
    "translateX",
    "translateY",
    "customProfit",
    "overlapPenalty",
    "xAlignment",
    "yAlignment",
    "cost",
    "distanceToEdge",
    "minimumHeight",
    "topInset",
    "bottomInset",
    "computedHeight",
    "originalHeight",
    "lineDistance",
    "minimumTableDistance",
    "verticalAlignment",
    "horizontalAlignment",
    "groupNodeCompactness",
    "initialTemperature",
    "finalTemperature",
    "gravityFactor",
    "iterationFactor",
    "preferredEdgeLength",
    "preferredNodeDistance",
    "qualityTimeRatio",
    "clusteringQuality",
    "compactnessFactor",
    "preferredMinimumNodeToEdgeDistance",
    "splitSegmentLength",
    "splitNodeSize",
    "aspectRatio",
    "minimumSegmentLength",
    "minimumNodeToNodeDistance",
    "layerSpacing",
    "maximumChildSectorAngle",
    "minimumBendAngle",
    "radius",
    "sectorStart",
    "sectorSize",
    "location",
    "minimumBackboneSegmentLength",
    "costs",
    "heuristicCosts",
    "minimumEdgeToEdgeDistance",
    "minimumNodeCornerDistance",
    "preferredPolylineSegmentLength",
    "maximumPolylineSegmentRatio",
    "minimumNodeToEdgeDistance",
    "originX",
    "originY",
    "center",
    "gridOriginX",
    "gridOriginY",
    "preferredDistance",
    "absJoinEndDistance",
    "relJoinEndDistance",
    "edgeLengthPenalty",
    "bendPenalty",
    "edgeCrossingPenalty",
    "nodeCrossingPenalty",
    "groupNodeCrossingPenalty",
    "nodeLabelCrossingPenalty",
    "edgeLabelCrossingPenalty",
    "minimumNodeToEdgeDistancePenalty",
    "minimumGroupNodeToEdgeDistancePenalty",
    "minimumEdgeToEdgeDistancePenalty",
    "minimumNodeCornerDistancePenalty",
    "minimumFirstLastSegmentLengthPenalty",
    "bendsInNodeToEdgeDistancePenalty",
    "monotonyViolationPenalty",
    "partitionGridCellReentrancePenalty",
    "portViolationPenalty",
    "invalidEdgeGroupingPenalty",
    "singleSideSelfLoopPenalty",
    "maximumNonOrthogonalSegmentRatio",
    "borderGapToPortGapRatio",
    "minimumPolylineSegmentLength",
    "preferredOctilinearSegmentLength",
    "inset",
    "extraCropLength",
    "textSize",
    "zoom",
    "selfLoopDistance",
    "smoothingLength",
    "roundRectArcRadius",
    "targetArrowScale",
    "targetArrowRatio",
    "sourceArrowScale",
    "sourceArrowRatio",
    "verticalDistance",
    "horizontalDistance",
    "bendDistance",
    "nodeLabelSpacing",
    "edgeLabelSpacing",
    "angleSum",
    "minimumBusSegmentDistance",
    "busAlignment",
    "preferredAspectRatio",
    "minimumChannelSegmentDistance",
    "minimumSlopeHeight",
    "minimumRootDistance",
    "minimumSubtreeDistance",
    "doubleLineSpacingRatio",
    "minimumBusDistance",
    "connectorX",
    "connectorY",
    "zoomThreshold",
    "defaultBridgeWidth",
    "clipMargin",
    "defaultBridgeHeight",
    "devicePixelRatio",
    "hitTestRadius",
    "hitTestRadiusTouch",
    "minimumZoom",
    "maximumZoom",
    "mouseWheelZoomFactor",
    "mouseWheelScrollFactor",
    "fontSize",
    "lineSpacing",
    "horizontalSpacing",
    "verticalSpacing",
    "visibilityThreshold",
    "wheelDelta",
    "radiusX",
    "radiusY",
    "miterLimit",
    "verticalOffset",
    "horizontalOffset",
    "maximumTargetZoom",
    "minimumNodeCentrality",
    "maximumNodeCentrality",
    "minimumEdgeCentrality",
    "maximumEdgeCentrality",
    "scaleFactor",
    "epsilon",
    "criticalEdgePriority",
    "portBorderGapRatios",
    "cutoff"
)