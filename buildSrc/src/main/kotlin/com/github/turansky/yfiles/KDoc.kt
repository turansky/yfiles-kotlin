package com.github.turansky.yfiles

// TODO: use Markdown after fix
//  https://youtrack.jetbrains.com/issue/KT-32640
fun link(text: String, href: String): String =
    """<a href="$href">$text</a>"""