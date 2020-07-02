@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.graph.DefaultGraph
import yfiles.graph.IGraph
import yfiles.graph.lookup
import yfiles.graph.lookupValue
import yfiles.input.IHitTestable
import yfiles.lang.TimeSpan

fun lookups() {
    val graph: IGraph = DefaultGraph()
    val node = graph.createNode()

    // for classes
    val t13: TimeSpan? = node.lookup()    // reified lookup type
    val t14 = node.lookup<TimeSpan>()     // 'TimeSpan?'

    val t23: TimeSpan = node.lookupValue()    // reified lookup type
    val t24 = node.lookupValue<TimeSpan>()    // 'TimeSpan'

    // for interfaces
    val h21 = node lookupValue IHitTestable.yclass // 'IHitTestable'
    val h22 = node lookupValue IHitTestable        // 'IHitTestable'

    val h23: IHitTestable = node.lookupValue()    // reified lookup type
    val h24 = node.lookupValue<IHitTestable>()    // 'IHitTestable'
}
