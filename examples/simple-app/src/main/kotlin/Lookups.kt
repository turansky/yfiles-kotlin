@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.graph.DefaultGraph
import yfiles.graph.IGraph
import yfiles.graph.lookup
import yfiles.input.IHitTestable
import yfiles.lang.TimeSpan
import yfiles.lang.yclass

fun lookups() {
    val graph: IGraph = DefaultGraph()
    val node = graph.createNode()

    // for classes
    val t1 = node lookup TimeSpan.yclass // 'TimeSpan?'
    val t2 = node lookup TimeSpan        // 'TimeSpan?'

    val t3: TimeSpan? = node.lookup()    // reified lookup type
    val t4 = node.lookup<TimeSpan>()     // 'TimeSpan?'

    // for interfaces
    val h1 = node lookup IHitTestable.yclass // 'IHitTestable?'
    val h2 = node lookup IHitTestable        // 'IHitTestable?'
}
