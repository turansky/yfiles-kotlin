@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.algorithms.YPoint
import yfiles.algorithms.component1
import yfiles.algorithms.component2
import yfiles.algorithms.plus

fun destructuring() {
    val p1 = YPoint(4.0, 8.0)
    val p2 = YPoint(15.0, 16.0)
    val p3 = YPoint(23.0, 42.0)

    val (x, y) = p1 + p2 + p3
}
