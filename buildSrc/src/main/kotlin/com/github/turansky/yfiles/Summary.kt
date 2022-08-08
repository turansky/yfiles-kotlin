package com.github.turansky.yfiles

internal fun summary(source: String): String {
    val result = source
        .fixApiLinks()
        .fixMarkdown()

    return result.trimStart()
        .takeIf { !it.startsWith("- ") }
        ?: result
}

private val TYPE_CLEAN_REGEX_1 = Regex("( data-type=\"[a-zA-Z.]+)<[^\"]+")
private val TYPE_CLEAN_REGEX_2 = Regex("( data-type=\"[a-zA-Z.]+)&lt;[^\"]+")

private val TYPE_REGEX = Regex("<api-link data-type=\"([a-zA-Z0-9.]+)\"\\s?></api-link>")
private val TYPE_TEXT_REGEX = Regex("<api-link data-type=\"([a-zA-Z0-9.]+)\" data-text=\"([^\"]+)\"></api-link>")

private val MEMBER_REGEX = Regex("<api-link data-type=\"([a-zA-Z0-9.]+)\" data-member=\"([a-zA-Z0-9_]+)\"></api-link>")
private val MEMBER_TEXT_REGEX = Regex("<api-link data-type=\"([a-zA-Z0-9.]+)\" data-member=\"([a-zA-Z0-9_]+)\" data-text=\"([^\"]+)\"></api-link>")

private val DIGIT_CLEAN_REGEX = Regex("(\\.[0-9]+)d")

private fun String.fixApiLinks(): String {
    return this
        .replace("""data-member="create factory method"""", """data-member="create"""")
        .replace(" data-text=\"\"", "")
        .replace(" infinite\"\"=\"\"", "")
        .replace(TYPE_CLEAN_REGEX_1, "$1")
        .replace(TYPE_CLEAN_REGEX_2, "$1")
        .replace(TYPE_REGEX, "[$1]")
        .replace(TYPE_TEXT_REGEX, "[$2][$1]")
        .replace(MEMBER_REGEX, "[$1.$2]")
        .replace(MEMBER_TEXT_REGEX, "[$3][$1.$2]")
        .replace("[$JS_OBJECT.", "[$YOBJECT.")
        .replace("[yfiles.lang.Object.", "[$YOBJECT.")
        .replace("[$JS_STRING]", "[$STRING]")
        .replace("[$JS_NUMBER]", "[Number]")
        .replace("[$JS_NUMBER.", "[$DOUBLE.")
        .replace("[Number.", "[$DOUBLE.")
        .replace("[$JS_BOOLEAN]", "[$BOOLEAN]")
        .replace("more NaN values", "more `NaN` values")
        .replace(">evt<", ">event<")
        .replace(">evt.", ">event.")
        .replace("&apos;", "'")
        .replace("&quot;", "\"")
        .also { check("<api-link" !in it) }
}

private val ENCODED_GENERIC_START = Regex("(<code>[^<]*)&lt;")

private fun String.fixMarkdown(): String {
    return replace(ENCODED_GENERIC_START, "$1<")
        .replace(" &lt;default> ", " `<default>` ")
        .replace(" &lt;img> ", " `<img>` ")
        .replace(" IEnumerable>TSource<)", " IEnumerable<TSource>)")
        .replace(" IEnumerable>TSource&lt; ", " `IEnumerable<TSource>` ")
        .replace("IEnumerable&lt;IEdge&gt;", "IEnumerable<IEdge>")
        .replace(
            "@link y.layout.hierarchic.incremental.NodeData#TYPE_NORMAL normal nodes}",
            "[normal nodes][yfiles.hierarchic.NodeDataType.NORMAL]"
        )
        .replace(
            "[ShapeProcessingContext..coordinateConverter]",
            "[ShapeProcessingContext.coordinateConverter]"
        )
        .replace(" &lt; ", " < ")
        .replace(" &lt;0", " <0")
        .replace("B&#xE9;zier", "Bézier")
        .replace("""<pre class="programlisting"><code class="js">""", "\n```\n")
        .replace("""<pre class="programlisting"><code class='js'>""", "\n```\n")
        .replace("""<pre class="programlisting"><code class="javascript">""", "\n```\n")
        .replace("""<pre class="programlisting"><code class="css">""", "\n```\n")
        .replace("<pre><code>", "\n```\n")
        .replace("</code></pre>", "\n```\n")
        .replace("<code>", "`")
        .replace("</code>", "`")
        .replace("<p>", "")
        .replace("</p>", "\n")
        .replace("<b>", "**")
        .replace("</b>", "**")
        .replace("<i>", "*")
        .replace("</i>", "*")
        .replace("<em>", "*")
        .replace("</em>", "*")
        .replace("<ul>      ", "<ul>")
        .replace("</li>      ", "</li>")
        .replace("</li>    ", "</li>")
        .replace("<ul><li>", "\n$LIST_MARKER ")
        .replace("</li><li>", "\n$LIST_MARKER ")
        .replace("</li></ul>", "\n\n")
        .replace(DIGIT_CLEAN_REGEX, "$1")
        .removeRedundantEndLines()
        .addHotkeyHighlight()
}

private fun String.removeRedundantEndLines(): String =
    if (endsWith("\n")) {
        removeSuffix("\n")
            .removeRedundantEndLines()
    } else {
        this
    }

private val HOTKEY_SUMMARY_DESCRIPTION = setOf(
    "The default shortcut for this command is ",
    "The default shortcuts for this command are ",
    "The default shortcut for this is "
)

private fun String.addHotkeyHighlight(): String {
    if (HOTKEY_SUMMARY_DESCRIPTION.none { it in this })
        return this

    return split("\n")
        .map { line ->
            val description = HOTKEY_SUMMARY_DESCRIPTION
                .firstOrNull { line.startsWith(it) }
                ?: return@map line

            line.removePrefix(description)
                .removeSuffix(".")
                .replace(" and ", "` and `")
                .replace(" or ", "` or `")
                .let { "$description`$it`." }
        }
        .joinToString("\n")
}
