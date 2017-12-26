package com.yworks.yfiles.api.generator

internal object MixinHacks {
    fun getImplementedTypes(className: String, implementedTypes: List<String>): List<String> {
        return when (className) {
            "yfiles.collections.Map" -> listOf("yfiles.collections.IMap<TKey, TValue>")
            "yfiles.geometry.IRectangle" -> existedItem("yfiles.geometry.IPoint", implementedTypes)
            "yfiles.geometry.IMutableRectangle" -> existedItem("yfiles.geometry.IRectangle", implementedTypes)
            "yfiles.geometry.MutableRectangle" -> existedItem("yfiles.geometry.IMutableRectangle", implementedTypes)
            "yfiles.geometry.IMutableOrientedRectangle" -> existedItem("yfiles.geometry.IOrientedRectangle", implementedTypes)
            "yfiles.geometry.OrientedRectangle" -> existedItem("yfiles.geometry.IMutableOrientedRectangle", implementedTypes)
            else -> implementedTypes
        }
    }

    private fun existedItem(item: String, items: List<String>): List<String> {
        if (items.contains(item)) {
            return listOf(item)
        }

        throw IllegalArgumentException("Item '$item' not contains in item list '$items'")
    }

    private val MUST_BE_ABSTRACT_CLASSES = listOf(
            "yfiles.collections.ICollection",
            "yfiles.collections.IList",
            "yfiles.collections.IMap",
            "yfiles.collections.IListEnumerable",
            "yfiles.collections.IObservableCollection",
            "yfiles.view.ICanvasObjectGroup",
            "yfiles.view.ISelectionModel",
            "yfiles.view.IStripeSelection",
            "yfiles.view.IGraphSelection",

            "yfiles.graph.IColumn",
            "yfiles.graph.IRow"
    )

    fun defineLikeAbstractClass(className: String, functions: List<Method>, properties: List<Property>): Boolean {
        if (className in MUST_BE_ABSTRACT_CLASSES) {
            return true
        }

        return functions.any { !it.abstract } || properties.any { !it.abstract }
    }
}