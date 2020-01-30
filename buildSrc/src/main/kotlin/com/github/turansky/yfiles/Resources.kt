package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.*
import org.json.JSONObject

internal fun generateResourceTypes(
    devguide: JSONObject,
    context: GeneratorContext
) {
    val keyDeclarations = devguide
        .flatMap(CHILDREN)
        .optFlatMap(CHILDREN)
        .optFlatMap(CHILDREN)
        .optFlatMap(CHILDREN)
        .first { it.opt(ID) == "tab_resource_defaults" }
        .get(CONTENT)
        .between("<tbody ", "</tbody>")
        .splitToSequence("</para>")
        .map { it.substringAfterLast(">") }
        .chunked(2)
        .filter { it.size == 2 }
        .map { keyDeclaration(it.first(), it.last()) }
        .joinToString(separator = "\n\n", postfix = "\n")

    context["yfiles.ResourceKey"] = """
        package yfiles
        
        @JsName("String")
        external class ResourceKey
        internal constructor()
        
        fun ResourceKey(source:String):ResourceKey = 
            source.unsafeCast<ResourceKey>()
        
        @JsName("String")
        external class Hotkey
        internal constructor()
        
        fun Hotkey(source:String):Hotkey = 
            source.unsafeCast<Hotkey>()
        
        $keyDeclarations
    """.trimIndent()
}

private fun keyDeclaration(key: String, defaultValue: String): String {
    val name = key.replace(".", "_")
    return """
        /**
         * Default value - $defaultValue
         */
         val $name = "$key"
    """.trimIndent()
}
