@file:Suppress("unused")

import yfiles.graph.GraphItemTypes.*
import yfiles.input.GraphViewerInputMode
import yfiles.lang.or

fun flags() {
    GraphViewerInputMode {
        clickableItems = NODE or EDGE or LABEL
    }
}
