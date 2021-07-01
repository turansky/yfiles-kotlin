package com.github.turansky.yfiles

import com.github.turansky.yfiles.correction.timeSpanExtensions

internal fun Class.getExtensions(): String? =
    when (classId) {
        "yfiles.lang.TimeSpan",
        -> timeSpanExtensions(this)

        else
        -> null
    }

internal fun Interface.getExtensions(): String? =
    when (classId) {
        // language=kotlin
        YOBJECT,
        -> """
            inline fun <T: $YOBJECT> T.getClass(): $YCLASS<out T> =
                $AS_DYNAMIC.getClass()
        """.trimIndent()

        // language=kotlin
        ILOOKUP,
        -> """
            inline fun <reified T : $YOBJECT> $ILOOKUP.lookup(): T? =
                lookup(T::class.js.yclass)     
                
            inline fun <T : $YOBJECT> $ILOOKUP.lookupValue(type: $YCLASS<T>):T = 
                requireNotNull(lookup(type)) {
                    "Unable to lookup type ${'$'}type"
                }
                
            inline fun <reified T : $YOBJECT> $ILOOKUP.lookupValue(): T =
                lookupValue(T::class.js.yclass)      
        """.trimIndent()

        IMAPPER_REGISTRY,
        -> getMapperRegistryExtensions()

        else -> null
    }

private fun getMapperRegistryExtensions(): String {
    return getMapperRegistryExtensions(GRAPH_DP_KEY, GRAPH, IGRAPH)
}

@Suppress("SameParameterValue")
private fun getMapperRegistryExtensions(
    dpKeyType: String,
    keyType: String,
    modelKeyType: String = keyType,
): String {
    var keyClass = "$keyType.yclass"
    if (keyType != modelKeyType)
        keyClass += ".unsafeCast<$YCLASS<$modelKeyType>>()"

    val valueClass = "tag.valueType"

    // language=kotlin
    return """
        inline fun <V : Any> IMapperRegistry.createConstantMapper( 
            tag: $dpKeyType<V> ,
            constant: V?
        ):IMapper<$modelKeyType, V> = 
            createConstantMapper($keyClass, $valueClass, tag, constant)
        
        inline fun <V : Any> IMapperRegistry.createDelegateMapper( 
            tag: $dpKeyType<V> ,
            noinline getter: MapperDelegate<$modelKeyType, V>
        ):IMapper<$modelKeyType, V> =
            createDelegateMapper($keyClass, $valueClass, tag, getter)

        inline fun <V : Any> IMapperRegistry.createMapper(tag: $dpKeyType<V>):Mapper<$modelKeyType, V> =
            createMapper($keyClass, $valueClass, tag)
    """.trimIndent()
}
