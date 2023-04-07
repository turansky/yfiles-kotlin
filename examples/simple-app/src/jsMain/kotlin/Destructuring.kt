@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.algorithms.YPoint
import yfiles.algorithms.YPoint.Companion.plus
import yfiles.collections.*

fun destructuring() {
    val p1 = YPoint(4.0, 8.0)
    val p2 = YPoint(15.0, 16.0)
    val p3 = YPoint(23.0, 42.0)

    val (x, y) = p1 + p2 + p3

    val l: IList<String> = List()
    val (l1, l2, l3) = l

    val e: IEnumerable<String> = l
    val (e1, e2, e3) = e
}
