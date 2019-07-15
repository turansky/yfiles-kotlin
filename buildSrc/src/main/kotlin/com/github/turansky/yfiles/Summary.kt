package com.github.turansky.yfiles

internal fun summary(source: String): String {
    return source
        .fixApiLinks()
        .fixMarkdown()
}


private fun String.fixApiLinks(): String {
    val typeCleanRegex1 = Regex("( data-type=\"[a-zA-Z.]+)<[^\"]+")
    val typeCleanRegex2 = Regex("( data-type=\"[a-zA-Z.]+)&lt;[^\"]+")
    val typeRegex = Regex("<api-link data-type=\"([a-zA-Z0-9.]+)\"></api-link>")
    val typeTextRegex = Regex("<api-link data-type=\"([a-zA-Z0-9.]+)\" data-text=\"([^\"]+)\"></api-link>")
    val memberRegex = Regex("<api-link data-type=\"([a-zA-Z.]+)\" data-member=\"([a-zA-Z0-9_]+)\"></api-link>")
    val memberTextRegex = Regex("<api-link data-type=\"([a-zA-Z.]+)\" data-member=\"([a-zA-Z0-9_]+)\" data-text=\"([^\"]+)\"></api-link>")
    return this
        .replace(" data-text=\"\"", "")
        .replace(" infinite\"\"=\"\"", "")
        .replace(typeCleanRegex1, "$1")
        .replace(typeCleanRegex2, "$1")
        .replace(typeRegex, "[$1]")
        .replace(typeTextRegex, "[$2][$1]")
        .replace(memberRegex, "[$1.$2]")
        .replace(memberTextRegex, "[$3][$1.$2]")
        .replace("[string]", "[String]")
        .replace("[number]", "[Number]")
        .replace("[boolean]", "[Boolean]")
        .also { check(!it.contains("<api-link")) }
}

private fun String.fixMarkdown(): String =
    replace(Regex("(<code>[^<]*)&lt;"), "$1<")
        .replace("<code>", "`")
        .replace("</code>", "`")
        .replace("<b>", "**")
        .replace("</b>", "**")
        .replace("<i>", "*")
        .replace("</i>", "*")
        .replace("<ul><li>", "\n+ ")
        .replace("</li><li>", "\n+ ")
        .replace("</li></ul>", "")