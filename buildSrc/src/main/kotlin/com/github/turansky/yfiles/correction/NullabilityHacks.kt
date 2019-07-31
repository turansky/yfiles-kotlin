package com.github.turansky.yfiles.correction

import org.json.JSONObject

internal fun fixNullability(source: Source) {
    fixAlgorithmsNullability(source)
    fixLayoutNullability(source)
    fixStageNullability(source)
    fixHierarchicNullability(source)
    fixTreeNullability(source)
}

private fun fixAlgorithmsNullability(source: Source) {
    val EXCLUDED_METHOD_IDS = setOf(
        "Geom-method-calcIntersection(yfiles.algorithms.YPoint,yfiles.algorithms.YVector,yfiles.algorithms.YPoint,yfiles.algorithms.YVector)",
        "Geom-method-calcIntersection(yfiles.algorithms.YPoint,yfiles.algorithms.YPoint,yfiles.algorithms.YPoint,yfiles.algorithms.YPoint)",

        "NetworkFlows-method-minCostFlow(yfiles.algorithms.Graph,yfiles.algorithms.Node,yfiles.algorithms.Node,yfiles.algorithms.IDataProvider,yfiles.algorithms.IDataProvider,yfiles.algorithms.IEdgeMap,yfiles.algorithms.INodeMap)",

        "ShortestPaths-method-bellmanFord(yfiles.algorithms.Graph,yfiles.algorithms.Node,boolean,yfiles.algorithms.IDataProvider,yfiles.algorithms.INodeMap,yfiles.algorithms.INodeMap)",
        "ShortestPaths-method-constructNodePath(yfiles.algorithms.Node,yfiles.algorithms.Node,Array<yfiles.algorithms.Edge>)",
        "ShortestPaths-method-constructNodePath(yfiles.algorithms.Node,yfiles.algorithms.Node,yfiles.algorithms.IDataProvider)",
        "ShortestPaths-method-uniform(yfiles.algorithms.Graph,yfiles.algorithms.Node,boolean,Array<number>,Array<yfiles.algorithms.Edge>)"
    )

    val EXCLUDED_METHODS = setOf(
        "simple",

        "aStar",
        "acyclic",
        "constructEdgePath",
        "dijkstra",
        "singleSource",
        "singleSourceSingleSink",

        "hide",
        "unhide",

        "contains",
        "moveToFirst",
        "moveToLast",

        "getValueAt"
    )

    val EXCLUDED_PARAMETERS = setOf(
        "edgeCosts",
        "edgeWeights",

        "defaultValue",
        "dualsNM",

        "revMap",
        "reverseEdgeMap"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number",

        "yfiles.algorithms.Linkage",
        "yfiles.algorithms.DistanceMetric",
        "yfiles.algorithms.GraphElementInsertion"
    )

    source.types(
        "AbortHandler",
        "AffineLine",
        "BipartitionAlgorithm",
        "CentralityAlgorithm",
        "Comparers",
        "Cursors",
        "CycleAlgorithm",
        "DataProviders",
        "Geom",
        "GraphChecker",
        "GraphConnectivity",
        "GroupAlgorithm",
        "IndependentSetAlgorithm",
        "IntersectionAlgorithm",
        "Maps",
        "NetworkFlowAlgorithm",
        "NodeOrderAlgorithm",
        "PathAlgorithm",
        "RankAssignmentAlgorithm",
        "SortingAlgorithm",
        "ShortestPathAlgorithm",
        "SpanningTreeAlgorithm",
        "TransitivityAlgorithm",
        "TreeAlgorithm",
        "TriangulationAlgorithm"
    ).jsequence(J_STATIC_METHODS)
        .filter { it.has(J_PARAMETERS) }
        .filterNot { it.getString(J_ID) in EXCLUDED_METHOD_IDS }
        .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_NAME) in EXCLUDED_PARAMETERS }
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }
            .plus(type.optJsequence(J_CONSTRUCTORS))

    source.types(
        "BorderLine",
        "Dendrogram",
        "DfsAlgorithm",
        "Edge",
        "Graph",
        "GraphPartitionManager",
        "INodeSequencer",
        "LayoutGraphHider",
        "PlanarEmbedding",
        "Point2D",
        "Rectangle2D",
        "YNode"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}

private fun fixLayoutNullability(source: Source) {
    val EXCLUDED_METHOD_IDS = setOf(
        "LayoutGraphUtilities-method-getBoundingBox(yfiles.layout.LayoutGraph,yfiles.algorithms.Node)",
        "LayoutGraphUtilities-method-getBoundingBox(yfiles.layout.LayoutGraph,yfiles.algorithms.Edge)"
    )

    val EXCLUDED_METHODS = setOf(
        "getLabelLayout",
        "getLayout",

        "setLabelLayout",
        "setLayout",
        "setPath",
        "setPoints",

        "getBoundingBoxOfEdges",
        "getBoundingBoxOfNodes",
        "routeEdgesParallel"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number",

        "yfiles.layout.LayoutOrientation",
        "yfiles.layout.MirrorModes",
        "yfiles.layout.SwimlanesMode",
        "yfiles.layout.PortSide",

        "yfiles.layout.NodeAlignment",
        "yfiles.layout.MultiRowConstraint",
        "yfiles.layout.RowAlignment"
    )

    source.types(
        "GraphTransformer",
        "LayoutGraphUtilities",
        "NodeHalo",
        "NormalizeGraphElementOrderStage",
        "PortConstraint",
        "Swimlanes"
    ).jsequence(J_STATIC_METHODS)
        .filter { it.has(J_PARAMETERS) }
        .filterNot { it.getString(J_ID) in EXCLUDED_METHOD_IDS }
        .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }
            .plus(type.optJsequence(J_CONSTRUCTORS))

    source.types(
        "LayoutGraph",
        "DefaultLayoutGraph",
        "CopiedLayoutGraph",

        "ILayoutGroupBoundsCalculator",
        "InsetsGroupBoundsCalculator",
        "MinimumSizeGroupBoundsCalculator",

        "EdgeLabelOrientationSupport",
        "LayoutGroupingSupport",
        "PartitionGrid",
        "PortConstraintConfigurator"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}

private fun fixStageNullability(source: Source) {
    val EXCLUDED_METHODS = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number",

        "yfiles.layout.LayoutOrientation"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

    source.types(
        "BendConverter",
        "ComponentLayout",
        "CompositeLayoutStage",
        "FixNodeLayoutStage",
        "HideGroupsStage",
        "IsolatedGroupComponentLayout",
        "LayoutMultiplexer",
        "MultiStageLayout",
        "OrientationLayout",
        "PortCalculator",
        "RecursiveGroupLayout",
        "ReverseEdgesStage",
        "SelfLoopRouter",

        "IPartitionFinder",
        "IPartitionInterEdgeRouter",
        "IPartitionPlacer"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}

private fun fixHierarchicNullability(source: Source) {
    val EXCLUDED_METHOD_IDS = setOf(
        "HierarchicLayout-defaultmethod-createLayerConstraintFactory(yfiles.graph.IGraph)",
        "HierarchicLayout-defaultmethod-createSequenceConstraintFactory(yfiles.graph.IGraph)"
    )

    val EXCLUDED_METHODS = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number",

        "yfiles.hierarchic.NodeDataType"
    )

    val EXCLUDED_PARAMETERS = setOf(
        "laneDescriptor",

        "left",
        "right",

        "predNode",
        "succ"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_ID) in EXCLUDED_METHOD_IDS }
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

    source.types(
        "IItemFactory",
        "IEdgeReverser",

        "ILayerer",
        "AsIsLayerer",
        "AspectRatioComponentLayerer",
        "BFSLayerer",
        "ConstraintIncrementalLayerer",
        "GivenLayersLayerer",
        "MultiComponentLayerer",
        "TopologicalLayerer",
        "WeightedLayerer",

        "ISequencer",
        "AsIsSequencer",
        "DefaultLayerSequencer",
        "GivenSequenceSequencer",

        "IDrawingDistanceCalculator",
        "DefaultDrawingDistanceCalculator",
        "TypeBasedDrawingDistanceCalculator",

        "IPortConstraintOptimizer",
        "PortCandidateOptimizer",
        "PortConstraintOptimizerBase",

        "HierarchicLayout",
        "HierarchicLayoutCore",

        "ILayeredComponentsMerger",
        "DefaultLayeredComponentsMerger",

        "IPortAllocator",
        "DefaultPortAllocator",

        "IHierarchicLayoutNodePlacer",
        "SimplexNodePlacer",

        "SelfLoopCalculator"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_NAME) in EXCLUDED_PARAMETERS }
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}

private fun fixTreeNullability(source: Source) {
    val EXCLUDED_METHODS = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number",

        "yfiles.tree.ParentConnectorDirection"
    )

    val EXCLUDED_PARAMETERS = setOf<String>()

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

    source.types(
        "ITreeLayoutNodePlacer",
        "IFromSketchNodePlacer",
        "AspectRatioNodePlacer",
        "AssistantNodePlacer",
        "BusNodePlacer",
        "CompactNodePlacer",
        "DefaultNodePlacer",
        "DelegatingNodePlacer",
        "DendrogramNodePlacer",
        "DoubleLineNodePlacer",
        "FreeNodePlacer",
        "GridNodePlacer",
        "GroupedNodePlacer",
        "LayeredNodePlacer",
        "LeafNodePlacer",
        "LeftRightNodePlacer",
        "NodePlacerBase",
        "RotatableNodePlacerBase",
        "SimpleNodePlacer",

        "ITreeLayoutPortAssignment",
        "DefaultTreeLayoutPortAssignment",

        "SubtreeShape",

        "TreeComponentLayout",
        "TreeReductionStage"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_NAME) in EXCLUDED_PARAMETERS }
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}