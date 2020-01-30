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
        .map { constDeclaration(it.first(), it.last()) }
        .joinToString(separator = "\n\n", postfix = "\n")

    context["yfiles.Hotkey"] = """
        package yfiles
        
        @JsName("String")
        external class Hotkey
        internal constructor()
        
        fun Hotkey(source:String):Hotkey = 
            source.unsafeCast<Hotkey>()
    """.trimIndent()

    context["yfiles.ResourceKey"] = """
        package yfiles
        
        @JsName("String")
        external class ResourceKey<T:Any>
        internal constructor()
        
        fun <T:Any> ResourceKey(source:String):ResourceKey<T> = 
            source.unsafeCast<ResourceKey<T>>()
        
        $keyDeclarations
    """.trimIndent()
}

private fun constDeclaration(
    key: String,
    defaultValue: String
): String {
    val name = key
        .replace(Regex("([a-z])([A-Z])"), "$1_$2")
        .replace(".", "__")
        .toUpperCase()

    return if (key.endsWith("Key")) {
        hotkeyDeclaration(key, name, defaultValue)
    } else {
        keyDeclaration(key, name, defaultValue)
    }
}

private fun keyDeclaration(
    key: String,
    name: String,
    defaultValue: String
): String {
    return """
        /**
         * Default value - "$defaultValue"
         */
         val $name: ResourceKey<String> = ResourceKey("$key")
    """.trimIndent()
}

private fun hotkeyDeclaration(
    key: String,
    name: String,
    defaultValue: String
): String {
    return """
        /**
         * Default hotkey - `$defaultValue`
         */
         val $name: ResourceKey<Hotkey> = ResourceKey("$key")
    """.trimIndent()
}
