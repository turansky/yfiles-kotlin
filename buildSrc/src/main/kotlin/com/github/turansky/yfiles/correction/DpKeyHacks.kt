package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.*
import com.github.turansky.yfiles.ContentMode.DELEGATE
import org.json.JSONObject

internal fun generateDpKeyDelegates(context: GeneratorContext) {
    // language=kotlin
    context[DP_KEY_BASE, DELEGATE] = """
        import yfiles.lang.findClass
        import yfiles.lang.yclass
        
        private fun <T:Any> $KCLASS<T>.toValueType(): $YCLASS<T> = 
            when (this) {
                $BOOLEAN::class -> $BOOLEAN.yclass
                $STRING::class -> $STRING.yclass
                
                $INT::class -> $INT.yclass
                
                Number::class,
                Float::class,
                $DOUBLE::class -> $DOUBLE.yclass
                
                else -> js.findClass() ?: $YOBJECT.yclass
            }.unsafeCast<$YCLASS<T>>()
        
        fun <T: $DP_KEY_BASE<*, V>, V: Any> dpKeyDelegate(
            createKey: ($YCLASS<V>, $YCLASS<*>, String) -> T,
            valueType: $YCLASS<V>,
            declaringType: $YCLASS<out $YOBJECT>
        ): $READ_ONLY_PROPERTY<Any?, T> =
            NamedDelegate { name -> 
                 createKey(valueType, declaringType, name)   
            }
        
        fun <T: $DP_KEY_BASE<*, V>, V: Any> dpKeyDelegate(
            createKey: ($YCLASS<V>, $YCLASS<*>, String) -> T,
            valueClass: $KCLASS<V>,
            declaringType: $YCLASS<out $YOBJECT>
        ): $READ_ONLY_PROPERTY<Any?, T> =
            dpKeyDelegate(createKey, valueClass.toValueType(), declaringType)
        
        private class NamedDelegate<T: Any>(
            private val create: (String) -> T
        ): $READ_ONLY_PROPERTY<Any?, T> {
            private lateinit var value: T

            override fun getValue(
                thisRef: Any?,
                property: $KPROPERTY<*>
            ): T {
                if (!::value.isInitialized) {
                    value = create(property.name)
                }
                
                return value
            }
        }
    """.trimIndent()

    for ((className, declaringClass) in DP_KEY_GENERIC_MAP) {
        if (className == DP_KEY_BASE_CLASS) {
            continue
        }

        val classId = "yfiles.algorithms.$className"
        val delegateName = className.removePrefix("I").replaceFirstChar { it.lowercase() }

        // language=kotlin
        context[classId, DELEGATE] = """
            inline fun <reified T: Any> $delegateName(): $READ_ONLY_PROPERTY<Any?, $className<T>> = 
                $delegateName($declaringClass.yclass)
                
            inline fun <reified T: Any> $delegateName(
                declaringType: $YCLASS<out $YOBJECT>
            ): $READ_ONLY_PROPERTY<Any?, $className<T>> = 
                dpKeyDelegate(::$className, T::class, declaringType)   
                 
            inline fun <T: $YOBJECT> $delegateName(
                valueType: $INTERFACE_METADATA<T>
            ): $READ_ONLY_PROPERTY<Any?, $className<T>> = 
                $delegateName(valueType, $declaringClass.yclass)
                
            inline fun <T: $YOBJECT> $delegateName(
                valueType: $INTERFACE_METADATA<T>,
                declaringType: $YCLASS<out $YOBJECT>
            ): $READ_ONLY_PROPERTY<Any?, $className<T>> = 
                dpKeyDelegate(::$className, valueType.yclass, declaringType)     
        """.trimIndent()
    }
}

internal fun applyDpKeyHacks(source: Source) {
    fixClass(source)
    fixProperties(source)
    fixMethodParameters(source)
}

private const val DP_KEY_BASE_CLASS = "DpKeyBase"
private const val DP_KEY_BASE_KEY = "TKey"

private const val DP_KEY_BASE_DECLARATION = "$DP_KEY_BASE<"

private val DP_KEY_GENERIC_MAP = mapOf(
    DP_KEY_BASE_CLASS to DP_KEY_BASE_KEY,

    "GraphDpKey" to GRAPH,

    "NodeDpKey" to NODE,
    "EdgeDpKey" to EDGE,
    "GraphObjectDpKey" to GRAPH_OBJECT,

    "ILabelLayoutDpKey" to "yfiles.layout.ILabelLayout",
    "IEdgeLabelLayoutDpKey" to IEDGE_LABEL_LAYOUT,
    "INodeLabelLayoutDpKey" to INODE_LABEL_LAYOUT
)

private fun fixClass(source: Source) {
    source.type(DP_KEY_BASE_CLASS) {
        addFirstTypeParameter(DP_KEY_BASE_KEY, YOBJECT)

        addProperty("valueType", "$YCLASS<TValue>")

        methodParameters(
            "equalsCore",
            "other",
            { true }
        ).single()
            .updateDpKeyGeneric(TYPE, DP_KEY_BASE_KEY)
    }

    for ((className, generic) in DP_KEY_GENERIC_MAP) {
        if (className == DP_KEY_BASE_CLASS) {
            continue
        }

        source.type(className)
            .updateDpKeyGeneric(EXTENDS, generic)
    }

    source.type("DpKeyItemCollection")
        .property("dpKey")
        .updateDpKeyGeneric(TYPE, "*")
}

internal fun dpKeyBase(typeParameter: String): String = "$DP_KEY_BASE<*,$typeParameter>"
internal fun graphDpKey(typeParameter: String): String = "$GRAPH_DP_KEY<$typeParameter>"
internal fun nodeDpKey(typeParameter: String): String = "yfiles.algorithms.NodeDpKey<$typeParameter>"
internal fun edgeDpKey(typeParameter: String): String = "yfiles.algorithms.EdgeDpKey<$typeParameter>"
internal fun labelDpKey(typeParameter: String): String = "yfiles.algorithms.ILabelLayoutDpKey<$typeParameter>"

private fun fixProperties(source: Source) {
    val typeMap = mapOf(
        "affectedNodesDpKey" to nodeDpKey(JS_BOOLEAN),
        "splitNodesDpKey" to nodeDpKey(JS_BOOLEAN),

        "centerNodesDpKey" to nodeDpKey(JS_BOOLEAN),

        "minSizeDataProviderKey" to nodeDpKey("yfiles.algorithms.YDimension"),
        "minimumNodeSizeDpKey" to nodeDpKey("yfiles.algorithms.YDimension"),

        "groupNodeInsetsDpKey" to nodeDpKey("yfiles.algorithms.YInsets"),

        "affectedEdgesDpKey" to edgeDpKey(JS_BOOLEAN),
        "interEdgesDpKey" to edgeDpKey(JS_BOOLEAN),
        "nonSeriesParallelEdgesDpKey" to edgeDpKey(JS_BOOLEAN),
        "nonSeriesParallelEdgeLabelSelectionKey" to edgeDpKey(JS_BOOLEAN),
        "nonTreeEdgeSelectionKey" to edgeDpKey(JS_BOOLEAN),

        "affectedLabelsDpKey" to labelDpKey(JS_BOOLEAN),
        "nonTreeEdgeLabelSelectionKey" to labelDpKey(JS_BOOLEAN)
    )

    val types = typeMap.keys
    source.types()
        .optFlatMap(PROPERTIES)
        .filter { it[NAME] in types }
        .filter { it[TYPE] == JS_ANY }
        .forEach { it[TYPE] = typeMap.getValue(it[NAME]) }

    source.type("InsetsGroupBoundsCalculator")
        .flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .single { it[NAME] == "groupNodeInsetsDPKey" }
        .also {
            val name = "groupNodeInsetsDpKey"
            it[NAME] = name
            it[TYPE] = typeMap.getValue(name)
        }

    source.type("MinimumSizeGroupBoundsCalculator")
        .flatMap(CONSTRUCTORS)
        .optFlatMap(PARAMETERS)
        .single { it[NAME] == "minSizeDataProviderKey" }
        .also {
            val name = "minimumNodeSizeDpKey"
            it[NAME] = name
            it[TYPE] = typeMap.getValue(name)
        }
}

private fun fixMethodParameters(source: Source) {
    source.types("IMapperRegistry", "MapperRegistry")
        .flatMap(METHODS)
        .forEach { method ->
            method.flatMap(PARAMETERS)
                .filter { it[NAME] == "tag" }
                .filter { it[TYPE] == JS_OBJECT }
                .forEach { it[TYPE] = dpKeyBase(if (method.has(TYPE_PARAMETERS)) "V" else "*") }
        }

    source.type("WeightedLayerer").also {
        sequenceOf(
            it.flatMap(CONSTRUCTORS)
                .flatMap(PARAMETERS)
                .single { it[NAME] == "key" },
            it.property("key")
        ).forEach { it[TYPE] = edgeDpKey(JS_INT) }
    }

    source.type("LabelingBase").apply {
        constant("LABEL_MODEL_DP_KEY").also {
            it.replaceInType("<$JS_ANY>", "<$YOBJECT>")
        }

        flatMap(METHODS)
            .filter { it[NAME] == "label" }
            .flatMap(PARAMETERS)
            .single { it[NAME] == "key" }
            .set(TYPE, labelDpKey(JS_BOOLEAN))
    }
}

private fun JSONObject.updateDpKeyGeneric(
    field: JStringKey,
    generic: String,
) {
    val value = get(field)

    // TODO: check
    if (field == EXTENDS && value == "yfiles.algorithms.ILabelLayoutDpKey<TValue>") {
        return
    }

    require(value.startsWith(DP_KEY_BASE_DECLARATION))
    set(field, value.replace(DP_KEY_BASE_DECLARATION, "$DP_KEY_BASE_DECLARATION$generic,"))
}
