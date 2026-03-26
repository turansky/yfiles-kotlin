@file:Suppress(
    "EXTERNAL_INTERFACE_AS_REIFIED_TYPE_ARGUMENT",
    "unused",
    "UNUSED_VARIABLE",
)

import yfiles.collections.lookup
import yfiles.collections.lookupValue
import yfiles.graph.Graph
import yfiles.graph.IGraph
import yfiles.input.IHitTestable
import yfiles.lang.TimeSpan

fun lookups() {
    val graph: IGraph = Graph()
    val node = graph.createNode()

    // for classes
    val t13: TimeSpan? = node.lookup()      // reified lookup type
    val t14 = node.lookup<TimeSpan>()       // 'TimeSpan?'

    val t23: TimeSpan = node.lookupValue()  // reified lookup type
    val t24 = node.lookupValue<TimeSpan>()  // 'TimeSpan'

    // for interfaces
    val h13: IHitTestable? = node.lookup()      // reified lookup type
    val h14 = node.lookup<IHitTestable>()       // 'IHitTestable?'

    val h23: IHitTestable = node.lookupValue()  // reified lookup type
    val h24 = node.lookupValue<IHitTestable>()  // 'IHitTestable'
}
