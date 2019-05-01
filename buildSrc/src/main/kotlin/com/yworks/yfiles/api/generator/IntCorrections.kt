package com.yworks.yfiles.api.generator

internal val INT_CONSTRUCTOR_PARAMETERS = setOf(
    "capacity",
    "enterSegmentNo"
)

internal val INT_METHODS = setOf(
    "getInt",
    "hashCode",
    "compareTo",
    "lastIndexOf",
    "length",
    "compare",
    "size",
    "indexOf",
    "binarySearch",
    "findIndex",
    "removeAll",
    "degree",
    "inDegree",
    "outDegree",
    "registerResource",
    "normalize",
    "assignNodeLayerWithDataProvider",
    "assignLayersToMap",
    "assignLayersFast",
    "createBend",
    "positionOf",
    "tar",
    "compareRenderOrder",
    "getObstacleHash",
    "getPriority",
    "push",
    "unshift",
    "getDirection",
    "getLevel",
    "getDepth",
    "getPreferredChildWedge",

    // ???
    "translateDirectionToReal",
    "translateDirectionToModel",

    // static
    "orientation",
    "sideOfCircle",
    "edgeBetweennessClustering",
    "biconnectedComponentGrouping",
    "hierarchicalClustering",
    "kMeansClustering",
    "minCostFlow",
    "calcMaxFlow",
    "calcMaxFlowMinCut",
    "simple",
    "simplex",
    "getPosition",
    "arrangeRectangleMultiRows",
    "arrangeRectangleRows"
)

internal val INT_PROPERTIES = setOf(
    "level",
    "index",
    "n",
    "e",
    "k",
    "size",
    "degree",
    "inDegree",
    "outDegree",
    "maximumDeviationAngle",
    "count",
    "maximumDuration",
    "randomizationRound",
    "priority",
    "layer",
    "position",
    "computedLaneIndex",
    "segmentIndex",
    "snapPanningThreshold",
    "pinchZoomThreshold",
    "maxRowLevel",
    "maxColumnLevel",
    "connections",
    "swimlanePos",
    "pageNo",
    "repulsion",
    "attraction",
    "lastWakeupTime",
    "gridSpacing",
    "treeSize",
    "chainSize",
    "cycleSize",
    "circleIndex",
    "minimumDistanceToNode",
    "minimumDistanceToEdge",
    "enterSegmentIndex",
    "exitSegmentIndex",
    "id",
    "remainingTime",
    "preferredChildWedge",
    "preferredRootWedge",
    "minimumEdgeLength",
    "r",
    "g",
    "a",
    "b",
    "charCode",
    "scrollAmount",
    "viewWidth",
    "viewHeight",
    "deviceIndex",
    "maximumNodesBeforeBus",
    "maximumNodesAfterBus",
    "randomizationRounds",

    // ???
    "maximumIterations",
    "maximumFlow",

    // static
    "cr570845"
)

internal val INT_METHOD_PARAMETERS = setOf(
    "index",
    "i",
    "columns",
    "rows",
    "side",

    "numObstaclesInFirstHalf",
    "numObstaclesInSecondHalf",
    "numObstaclesOnCut",

    "maxLayers",
    "dfsNumber",
    "compNumber",
    "maxCompNum",
    "maxCluster",
    "iterations",
    "type",

    // ???
    "direction"
)