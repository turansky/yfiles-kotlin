package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.JS_ANY

internal const val INODE_TYPE = "yfiles.layout.INodeType"

internal fun generateNodeTypeUtils(context: GeneratorContext) {
    // language=kotlin
    context[INODE_TYPE] =
        """
            external interface INodeType
            
            inline fun INodeType(source:Any):INodeType = 
                source.unsafeCast<INodeType>()
        """.trimIndent()
}

internal fun applyNodeTypeHacks(source: Source) {
    source.types()
        .optFlatMap(PROPERTIES)
        .filter { it[NAME] in "nodeTypes" }
        .forEach { it.replaceInType(",$JS_ANY>", ",$INODE_TYPE>") }
}
