package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.YCLASS
import org.json.JSONObject

internal fun fixNullability(source: Source) {
    fixConstructorNullability(source)

    fixCollectionsNullability(source)
    fixGraphNullability(source)
    fixAlgorithmsNullability(source)
    fixLayoutNullability(source)
    fixCommonLayoutNullability(source)
    fixStageNullability(source)
    fixMultipageNullability(source)
    fixHierarchicNullability(source)
    fixRouterNullability(source)
    fixTreeNullability(source)
}

private fun fixConstructorNullability(source: Source) {
    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number"
    )

    source.types(
        "DpKeyBase",
        "EdgeDpKey",
        "GraphDpKey",
        "GraphObjectDpKey",
        "IEdgeLabelLayoutDpKey",
        "ILabelLayoutDpKey",
        "INodeLabelLayoutDpKey",
        "NodeDpKey",

        "MapEntry",

        "CompositeLayoutStage"
    ).flatMap { it.jsequence(J_CONSTRUCTORS) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }

    source.types(
        "LookupDecorator",
        "StripeDecorator",

        "MapperInputHandler",
        "MapperOutputHandler",
        "InputHandlerBase",
        "OutputHandlerBase",

        "DataMapAdapter",
        "MapperDataProviderAdapter",

        "PathBasedEdgeStyleRenderer",

        "ItemModelManager",
        "CollectionModelManager"
    ).flatMap { it.jsequence(J_CONSTRUCTORS) }
        .jsequence(J_PARAMETERS)
        .filter { it.getString(J_TYPE).startsWith(YCLASS) }
        .filter { it.getString(J_NAME) != "deserializerTargetType" }
        .forEach { it.changeNullability(false) }

    source.types(
        "ItemClickedEventArgs",
        "ItemTappedEventArgs",
        "TableItemClickedEventArgs",
        "TableItemTappedEventArgs",
        "ItemSelectionChangedEventArgs"
    ).flatMap { it.jsequence(J_CONSTRUCTORS) }
        .map { it.firstParameter }
        .forEach { it.changeNullability(false) }
}

private fun fixCollectionsNullability(source: Source) {
    val INCLUDED_METHOD_IDS = setOf(
        "YList-method-addAll(number,yfiles.collections.ICollection)"
    )

    val INCLUDED_METHODS = setOf(
        "get",

        "has",
        "includes",
        "indexOf",
        "containsValue",

        "add",
        "set",
        "insert",

        "remove",
        "delete",

        "fromConstant",
        "ofRepeat",
        "fill",

        "binarySearch",

        "publishItemChanged",
        "onItemAdded",
        "onItemChanged",
        "onItemRemoved",

        "addFirst",
        "addFirstCell",
        "addLast",
        "addLastCell",
        "containsAll",
        "cyclicPred",
        "cyclicSucc",
        "findCell",
        "getInfo",
        "insertAfter",
        "insertBefore",
        "insertCellAfter",
        "insertCellBefore",
        "lastIndexOf",
        "predCell",
        "push",
        "removeAll",
        "removeAtCursor",
        "removeCell",
        "retainAll",
        "setInfo",
        "sort",
        "splice",
        "succCell"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number"
    )

    val EXCLUDED_PARAMETERS = setOf(
        "comparer",
        "match"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> {
        var includedMethods = INCLUDED_METHODS
        when (type.getString(J_NAME)) {
            "List" -> includedMethods = includedMethods - listOf("push", "sort", "splice")
            "Mapper" -> includedMethods = includedMethods - "delete"
        }

        return (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filter { it.getString(J_ID) in INCLUDED_METHOD_IDS || it.getString(J_NAME) in includedMethods }
    }

    source.types(
        "IEnumerable",

        "ICollection",
        "ObservableCollection",

        "IList",
        "List",
        "YList",

        "IMap",
        "HashMap",

        "IMapper",
        "Mapper",
        "CreationProperties",
        "ResultItemMapping"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .filterNot { it.getString(J_NAME) in EXCLUDED_PARAMETERS }
        .forEach { it.changeNullability(false) }
}

private fun fixGraphNullability(source: Source) {
    val INCLUDED_METHODS = setOf(
        "isSelected",
        "setSelected",
        "removeDomainItem",

        "getDescendants",
        "getDescendantsBottomUp",
        "getPathToRoot",
        "isDescendant"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number"
    )

    source.types(
        "ISelectionModel",
        "DefaultSelectionModel",
        "GraphSelection",
        "StripeSelection",

        "GroupingSupport"
    ).flatMap { it.jsequence(J_METHODS) }
        .filter { it.getString(J_NAME) in INCLUDED_METHODS }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}

private fun fixAlgorithmsNullability(source: Source) {
    val EXCLUDED_METHOD_IDS = setOf(
        "Geom-method-calcIntersection(yfiles.algorithms.YPoint,yfiles.algorithms.YVector,yfiles.algorithms.YPoint,yfiles.algorithms.YVector)",
        "Geom-method-calcIntersection(yfiles.algorithms.YPoint,yfiles.algorithms.YPoint,yfiles.algorithms.YPoint,yfiles.algorithms.YPoint)",

        "LineSegment-method-intersects(yfiles.algorithms.YRectangle)",
        "LineSegment-method-intersects(yfiles.algorithms.YPoint)",

        "NetworkFlows-method-minCostFlow(yfiles.algorithms.Graph,yfiles.algorithms.Node,yfiles.algorithms.Node,yfiles.algorithms.IDataProvider,yfiles.algorithms.IDataProvider,yfiles.algorithms.IEdgeMap,yfiles.algorithms.INodeMap)",

        "ShortestPaths-method-bellmanFord(yfiles.algorithms.Graph,yfiles.algorithms.Node,boolean,yfiles.algorithms.IDataProvider,yfiles.algorithms.INodeMap,yfiles.algorithms.INodeMap)",
        "ShortestPaths-method-constructNodePath(yfiles.algorithms.Node,yfiles.algorithms.Node,Array<yfiles.algorithms.Edge>)",
        "ShortestPaths-method-constructNodePath(yfiles.algorithms.Node,yfiles.algorithms.Node,yfiles.algorithms.IDataProvider)",
        "ShortestPaths-method-uniform(yfiles.algorithms.Graph,yfiles.algorithms.Node,boolean,Array<number>,Array<yfiles.algorithms.Edge>)"
    )

    val EXCLUDED_METHODS = setOf(
        "AffineLine",

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

        "getValueAt",

        "compareTo"
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
            .plus(type.optJsequence(J_CONSTRUCTORS))
            .filterNot { it.getString(J_ID) in EXCLUDED_METHOD_IDS }
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

    source.types(
        "AffineLine",
        "BorderLine",
        "Dendrogram",
        "DfsAlgorithm",
        "Edge",
        "Graph",
        "GraphPartitionManager",
        "IIntersectionHandler",
        "INodeDistanceProvider",
        "INodeSequencer",
        "LayoutGraphHider",
        "LineSegment",
        "PlanarEmbedding",
        "Point2D",
        "Rectangle2D",
        "YNode",
        "YPoint",
        "YRectangle"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }

    val Y_METHOD_IDS = setOf(
        "YOrientedRectangle-method-contains(yfiles.algorithms.YOrientedRectangle,number,number,number)",
        "YOrientedRectangle-constructor-YOrientedRectangle(yfiles.algorithms.YPoint,yfiles.algorithms.YDimension)",
        "YOrientedRectangle-constructor-YOrientedRectangle(yfiles.algorithms.YPoint,yfiles.algorithms.YDimension,yfiles.algorithms.YVector)",

        "YVector-method-add"
    )

    val Y_METHODS = setOf(
        "adoptValues",
        "calcPoints",
        "calcPointsInDouble",
        "intersectionPoint",

        "angle",
        "getNormal",
        "orthoNormal",
        "rightOf",
        "scalarProduct"
    )

    source.types(
        "YOrientedRectangle",
        "YVector"
    ).flatMap { it.jsequence(J_METHODS) + it.jsequence(J_STATIC_METHODS) + it.jsequence(J_CONSTRUCTORS) }
        .filter { it.getString(J_ID) in Y_METHOD_IDS || it.getString(J_NAME) in Y_METHODS }
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
        "routeEdgesParallel",

        "SliderEdgeLabelLayoutModel"
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
        "yfiles.layout.RowAlignment",

        "yfiles.layout.DiscreteNodeLabelPositions",
        "yfiles.layout.DiscreteEdgeLabelPositions",
        "yfiles.layout.PortDirections"
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
        (type.optJsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .plus(type.optJsequence(J_CONSTRUCTORS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

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
        "PortConstraintConfigurator",
        "PortCandidateSet",
        "ItemCollectionMapping",

        "INodeLabelLayoutModel",
        "DiscreteNodeLabelLayoutModel",
        "FreeNodeLabelLayoutModel",

        "IEdgeLabelLayoutModel",
        "DiscreteEdgeLabelLayoutModel",
        "FreeEdgeLabelLayoutModel",
        "SliderEdgeLabelLayoutModel",

        "LabelCandidate",
        "EdgeLabelCandidate",
        "NodeLabelCandidate",

        "IIntersectionCalculator",
        "IPortCandidateMatcher",

        "IProfitModel",
        "SimpleProfitModel",
        "ExtendedLabelCandidateProfitModel"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}

private fun fixCommonLayoutNullability(source: Source) {
    val EXCLUDED_METHODS = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

    source.types(
        "SequentialLayout",

        "LabelingBase",
        "MISLabelingBase",

        "MultiPageLayout",
        "InteractiveOrganicLayout",
        "OrganicLayout",
        "PartialLayout",

        "ISeriesParallelLayoutPortAssignment",
        "DefaultSeriesParallelLayoutPortAssignment"
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

private fun fixMultipageNullability(source: Source) {
    val EXCLUDED_METHODS = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

    source.types(
        "IElementFactory",
        "DefaultElementFactory",

        "IElementInfoManager",
        "LayoutContext",
        "MultiPageLayoutResult",

        "ILayoutCallback"
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

        "ILayer",
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
        "PortConstraintOptimizerSameLayerData",

        "HierarchicLayout",
        "HierarchicLayoutCore",

        "ILayeredComponentsMerger",
        "DefaultLayeredComponentsMerger",

        "IPortAllocator",
        "DefaultPortAllocator",

        "IHierarchicLayoutNodePlacer",
        "SimplexNodePlacer",

        "IIncrementalHintsFactory",
        "ILayerConstraintFactory",
        "ISequenceConstraintFactory",
        "ILayoutDataProvider",
        "INodeData",

        "SelfLoopCalculator"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_NAME) in EXCLUDED_PARAMETERS }
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}

private fun fixRouterNullability(source: Source) {
    val EXCLUDED_METHODS = setOf(
        "applyLayout",
        "applyLayoutCore",

        "getEdgeInfo"
    )

    val EXCLUDED_TYPES = setOf(
        "boolean",
        "number",

        "yfiles.router.ChannelOrientation"
    )

    val EXCLUDED_PARAMETERS = setOf<String>()

    source.types(
        "BusRepresentations"
    ).jsequence(J_STATIC_METHODS)
        .filter { it.has(J_PARAMETERS) }
        .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.optJsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }
            .let {
                if (type.getString(J_NAME).endsWith("Router")) {
                    it
                } else {
                    it + type.optJsequence(J_CONSTRUCTORS)
                }
            }

    source.types(
        "IDynamicDecomposition",
        "IDecompositionListener",
        "IGraphPartitionExtension",
        "IPartition",
        "IObstaclePartition",
        "GraphPartition",
        "GraphPartitionExtensionAdapter",
        "DynamicObstacleDecomposition",

        "BusRouterBusDescriptor",
        "CellSegmentInfo",
        "EdgeCellInfo",
        "EdgeInfo",
        "Interval",
        "Obstacle",
        "OrthogonalInterval",
        "PartitionCell",
        "PartitionCellBorder",

        "EdgeRouterPath",
        "PathSearch",
        "PathSearchConfiguration",
        "PathSearchContext",
        "PathSearchExtension",
        "PathSearchResult",
        "SegmentGroup",
        "SegmentInfo",
        "SegmentInfoBase",

        "ChannelRoutingTool",
        "ChannelBasedPathRouting",
        "IEnterIntervalCalculator",

        "ChannelEdgeRouter",
        "EdgeRouter",
        "OrthogonalPatternEdgeRouter",
        "ParallelEdgeRouter"
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

        "yfiles.tree.ParentConnectorDirection",
        "yfiles.tree.RootPlacement",
        "yfiles.tree.SubtreeArrangement"
    )

    val EXCLUDED_PARAMETERS = setOf<String>()

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        (type.jsequence(J_METHODS) + type.optJsequence(J_STATIC_METHODS))
            .filterNot { it.getString(J_NAME) in EXCLUDED_METHODS }

    source.types(
        "IProcessor",

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
        "RootNodeAlignment",
        "RotatableNodePlacerMatrix",

        "AspectRatioTreeLayout",
        "BalloonLayout",

        "TreeLayout",
        "TreeComponentLayout",
        "TreeReductionStage"
    ).flatMap { getAffectedMethods(it) }
        .filter { it.has(J_PARAMETERS) }
        .jsequence(J_PARAMETERS)
        .filterNot { it.getString(J_NAME) in EXCLUDED_PARAMETERS }
        .filterNot { it.getString(J_TYPE) in EXCLUDED_TYPES }
        .forEach { it.changeNullability(false) }
}