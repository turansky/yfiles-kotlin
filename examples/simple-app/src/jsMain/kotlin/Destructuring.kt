@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.collections.*
import yfiles.geometry.Point

fun destructuring() {
    val p1 = Point(4.0, 8.0)
    val p2 = Point(15.0, 16.0)
    val p3 = Point(23.0, 42.0)

    val (x, y) = p1 + p2 + p3

    val l: IList<String> = List()
    val (l1, l2, l3) = l

    val e: IEnumerable<String> = l
    val (e1, e2, e3) = e
}
