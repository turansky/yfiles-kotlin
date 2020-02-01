@file:Suppress("unused")

import yfiles.lang.ResourceKeys.COPY
import yfiles.lang.ResourceKeys.COPY_KEY
import yfiles.lang.Resources.invariant
import yfiles.lang.get

fun resources() {
    println(invariant[COPY]) // Copy
    println(invariant[COPY_KEY]) // Action+C;Ctrl+Ins
}
