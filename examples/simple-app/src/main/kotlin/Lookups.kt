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

    val t1: TimeSpan? = node lookup TimeSpan.yclass
    val t2: TimeSpan? = node lookup TimeSpan
    val t3: TimeSpan? = node.lookup()
    val t4 = node.lookup<TimeSpan>()

    val h1: IHitTestable? = node lookup IHitTestable.yclass
    val h2: IHitTestable? = node lookup IHitTestable
}
