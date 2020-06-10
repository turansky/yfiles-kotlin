package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import org.json.JSONObject

internal fun fixNullability(source: Source) {
    fixConstructorNullability(source)
    fixPropertyNullability(source)

    fixCollectionsNullability(source)
    fixGraphNullability(source)
    fixGraphmlNullability(source)
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
    val excludedTypes = setOf(
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

            "GraphWrapperBase",
            "CompositeLayoutStage",

            "AspectRatioComponentLayerer",
            "ConstraintIncrementalLayerer"
        ).flatMap { it.flatMap(CONSTRUCTORS) }
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }

    source.type("MultiComponentLayerer")
        .flatMap(CONSTRUCTORS)
        .single()
        .firstParameter
        .changeNullability(false)

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
        ).flatMap { it.flatMap(CONSTRUCTORS) }
        .flatMap(PARAMETERS)
        .filter { it[TYPE].startsWith(YCLASS) }
        .filter { it[NAME] != "deserializerTargetType" }
        .forEach { it.changeNullability(false) }

    source.types(
            "ItemClickedEventArgs",
            "ItemTappedEventArgs",
            "TableItemClickedEventArgs",
            "TableItemTappedEventArgs",
            "ItemSelectionChangedEventArgs"
        ).flatMap { it.flatMap(CONSTRUCTORS) }
        .map { it.firstParameter }
        .forEach { it.changeNullability(false) }
}

private fun fixPropertyNullability(source: Source) {
    fun JSONObject.hasNullDefaultValue(): Boolean {
        if (!has(DEFAULT)) {
            return false
        }

        val default = get(DEFAULT)
        return default.has(VALUE) && default[VALUE] == "null"
    }

    source.types()
        .flatMap { it.optFlatMap(PROPERTIES) }
        .filter { it[NAME] == "coreLayout" || it.hasNullDefaultValue() }
        .forEach { it.changeNullability(true) }
}

private fun fixCollectionsNullability(source: Source) {
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

        "publishItemChanged",
        "onItemAdded",
        "onItemChanged",
        "onItemRemoved",

        "addFirst",
        "addLast",
        "containsAll",
        "findCell",
        "getInfo",
        "insertAfter",
        "insertBefore",
        "lastIndexOf",
        "push",
        "removeAll",
        "setInfo",
        "sort",
        "splice"
    )

    val excludedTypes = setOf(
        "boolean",
        "number"
    )

    val excludedParameters = setOf(
        "match",
        "refCell"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> {
        var includedMethods = INCLUDED_METHODS
        when (type[NAME]) {
            "List" -> includedMethods = includedMethods - listOf("push", "sort", "splice")
            "Mapper" -> includedMethods = includedMethods - "delete"
        }

        return type.flatMap(METHODS)
            .filter { it[NAME] in includedMethods }
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
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .filterNot { it[NAME] in excludedParameters }
        .forEach { it.changeNullability(false) }

    source.type("IEnumerable")
        .method("indexOf")
        .get(PARAMETERS)
        .remove(1)
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

    val excludedTypes = setOf(
        "boolean",
        "number"
    )

    source.types(
            "ISelectionModel",
            "DefaultSelectionModel",
            "GraphSelection",
            "StripeSelection",

            "GroupingSupport"
        ).flatMap { it.flatMap(METHODS) }
        .filter { it[NAME] in INCLUDED_METHODS }
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }

    source.type("IGraph")
        .method("applyLayout")
        .also {
            it[PARAMETERS] = it[PARAMETERS].take(2)
        }
}

private fun fixGraphmlNullability(source: Source) {
    val EXCLUDED_METHODS = setOf(
        "onGraphMLParserHandleDeserialization",
        "onGraphMLParserQueryInputHandlers",
        "registerNodeStyleOutputHandler"
    )

    val INCLUDED_PARAMETERS = setOf(
        "sender",
        "source"
    )

    source.types(
            "GraphMLIOHandler"
        ).flatMap { it.flatMap(METHODS) }
        .filterNot { it[NAME] in EXCLUDED_METHODS }
        .optFlatMap(PARAMETERS)
        .filter { it[NAME] in INCLUDED_PARAMETERS }
        .filter { it[TYPE] == JS_OBJECT }
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

    val excludedMethods = setOf(
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

        "compareTo",

        "create"
    )

    val excludedParameters = setOf(
        "edgeCosts",
        "edgeWeights",

        "defaultValue",
        "dualsNM",

        "revMap",
        "reverseEdgeMap"
    )

    val excludedTypes = setOf(
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
        ).flatMap(METHODS)
        .filter { STATIC in it[MODIFIERS] }
        .filter { it.has(PARAMETERS) }
        .filterNot { it[ID] in EXCLUDED_METHOD_IDS }
        .filterNot { it[NAME] in excludedMethods }
        .flatMap(PARAMETERS)
        .filterNot { it[NAME] in excludedParameters }
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.flatMap(METHODS)
            .plus(type.optFlatMap(CONSTRUCTORS))
            .filterNot { it[ID] in EXCLUDED_METHOD_IDS }
            .filterNot { it[NAME] in excludedMethods }

    source.types(
            "AffineLine",
            "BorderLine",
            "Dendrogram",
            "DfsAlgorithm",
            "GraphPartitionManager",
            "IIntersectionHandler",
            "INodeDistanceProvider",
            "INodeSequencer",
            "LayoutGraphHider",
            "LineSegment",
            "PlanarEmbedding",
            "Point2D",
            "Rectangle2D",
            "YPoint",
            "YRectangle"
        ).flatMap { getAffectedMethods(it) }
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }

    val yMethodIds = setOf(
        "YOrientedRectangle-method-contains(yfiles.algorithms.YOrientedRectangle,number,number,number)",
        "YOrientedRectangle-constructor-YOrientedRectangle(yfiles.algorithms.YPoint,yfiles.algorithms.YDimension)",
        "YOrientedRectangle-constructor-YOrientedRectangle(yfiles.algorithms.YPoint,yfiles.algorithms.YDimension,yfiles.algorithms.YVector)",

        "YVector-method-add"
    )

    val yMethods = setOf(
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
        ).flatMap { it.flatMap(METHODS) + it.flatMap(CONSTRUCTORS) }
        .filter { it[ID] in yMethodIds || it.get(NAME) in yMethods }
        .flatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}

private fun fixLayoutNullability(source: Source) {
    val EXCLUDED_METHOD_IDS = setOf(
        "LayoutGraphUtilities-method-getBoundingBox(yfiles.layout.LayoutGraph,yfiles.algorithms.Node)",
        "LayoutGraphUtilities-method-getBoundingBox(yfiles.layout.LayoutGraph,yfiles.algorithms.Edge)"
    )

    val excludedMethods = setOf(
        "getLabelLayout",
        "getLayout",

        "setLabelLayout",
        "setLayout",
        "setPath",
        "setPoints",

        "getBoundingBoxOfEdges",
        "getBoundingBoxOfNodes",
        "routeEdgesParallel",

        "SliderEdgeLabelLayoutModel",

        "create"
    )

    val excludedTypes = setOf(
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
        ).flatMap(METHODS)
        .filter { STATIC in it[MODIFIERS] }
        .filter { it.has(PARAMETERS) }
        .filterNot { it[ID] in EXCLUDED_METHOD_IDS }
        .filterNot { it[NAME] in excludedMethods }
        .flatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.optFlatMap(METHODS)
            .plus(type.optFlatMap(CONSTRUCTORS))
            .filterNot { it[NAME] in excludedMethods }

    source.types(
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
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}

private fun fixCommonLayoutNullability(source: Source) {
    val excludedMethods = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val excludedTypes = setOf(
        "boolean",
        "number"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.flatMap(METHODS)
            .filterNot { it[NAME] in excludedMethods }

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
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}

private fun fixStageNullability(source: Source) {
    val excludedMethods = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val excludedTypes = setOf(
        "boolean",
        "number",

        "yfiles.layout.LayoutOrientation"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.flatMap(METHODS)
            .filterNot { it[NAME] in excludedMethods }

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
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}

private fun fixMultipageNullability(source: Source) {
    val excludedMethods = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val excludedTypes = setOf(
        "boolean",
        "number"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.flatMap(METHODS)
            .filterNot { it[NAME] in excludedMethods }

    source.types(
            "IElementFactory",
            "DefaultElementFactory",

            "IElementInfoManager",
            "LayoutContext",
            "MultiPageLayoutResult",

            "ILayoutCallback"
        ).flatMap { getAffectedMethods(it) }
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}

private fun fixHierarchicNullability(source: Source) {
    val EXCLUDED_METHOD_IDS = setOf(
        "HierarchicLayout-defaultmethod-createLayerConstraintFactory(yfiles.graph.IGraph)",
        "HierarchicLayout-defaultmethod-createSequenceConstraintFactory(yfiles.graph.IGraph)"
    )

    val excludedMethods = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val excludedTypes = setOf(
        "boolean",
        "number",

        "yfiles.hierarchic.NodeDataType"
    )

    val excludedParameters = setOf(
        "laneDescriptor",

        "left",
        "right",

        "predNode",
        "succ"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.flatMap(METHODS)
            .filterNot { it[ID] in EXCLUDED_METHOD_IDS }
            .filterNot { it[NAME] in excludedMethods }

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
        .optFlatMap(PARAMETERS)
        .filterNot { it[NAME] in excludedParameters }
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}

private fun fixRouterNullability(source: Source) {
    val excludedMethods = setOf(
        "applyLayout",
        "applyLayoutCore",

        "getEdgeInfo"
    )

    val excludedTypes = setOf(
        JS_BOOLEAN,
        JS_NUMBER,

        "yfiles.router.ChannelOrientation"
    )

    source.types(
            "BusRepresentations"
        ).flatMap(METHODS)
        .filter { it.has(PARAMETERS) }
        .filterNot { it[NAME] in excludedMethods }
        .flatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.optFlatMap(METHODS)
            .filterNot { it[NAME] in excludedMethods }
            .let {
                if (type[NAME].endsWith("Router")) {
                    it
                } else {
                    it + type.optFlatMap(CONSTRUCTORS)
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
            "Channel",
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
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}

private fun fixTreeNullability(source: Source) {
    val excludedMethods = setOf(
        "applyLayout",
        "applyLayoutCore"
    )

    val excludedTypes = setOf(
        "boolean",
        "number",

        "yfiles.tree.ParentConnectorDirection",
        "yfiles.tree.RootPlacement",
        "yfiles.tree.SubtreeArrangement"
    )

    fun getAffectedMethods(type: JSONObject): Sequence<JSONObject> =
        type.flatMap(METHODS)
            .filterNot { it[NAME] in excludedMethods }

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
        .optFlatMap(PARAMETERS)
        .filterNot { it[TYPE] in excludedTypes }
        .forEach { it.changeNullability(false) }
}
