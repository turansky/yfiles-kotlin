package com.github.turansky.yfiles

internal fun getHandlerData(listenerType: String): HandlerData {
    if (listenerType.startsWith("yfiles.lang.EventHandler1<")) {
        return getEventHandlerData(between(listenerType, "<", ">"))
    }

    return when (listenerType) {
        "yfiles.lang.EventHandler" ->
            EMPTY_HANDLER_DATA

        "yfiles.lang.PropertyChangedEventHandler" ->
            PROPERTY_HANDLER_DATA

        "yfiles.graph.NodeLayoutChangedHandler" ->
            HandlerData(
                handlerType = "(node: yfiles.graph.INode, oldLayout: yfiles.geometry.Rect) -> Unit",
                listenerBody = "{ _, node, oldLayout -> handler(node, oldLayout) }"
            )

        "yfiles.graph.BendLocationChangedHandler" ->
            HandlerData(
                handlerType = "(bend: yfiles.graph.IBend, oldLocation: yfiles.geometry.Point) -> Unit",
                listenerBody = "{ _, bend, oldLocation -> handler(bend, oldLocation) }"
            )

        else -> throw IllegalArgumentException("No handler data for $listenerType")
    }
}

private fun getEventHandlerData(eventType: String): HandlerData {
    if (eventType.startsWith("yfiles.collections.ItemEventArgs<")) {
        val itemType = between(eventType, "<", ">")
        return HandlerData(
            handlerType = "(item:$itemType) -> Unit",
            listenerBody = "{ _, event -> handler(event.item) }"
        )
    }

    return when (eventType) {
        "yfiles.lang.EventArgs" ->
            EMPTY_HANDLER_DATA

        "yfiles.lang.PropertyChangedEventArgs" ->
            PROPERTY_HANDLER_DATA

        "yfiles.input.InputModeEventArgs" ->
            INPUT_MODE_HANDLER_DATA

        "yfiles.input.MarqueeSelectionEventArgs" ->
            MARQUEE_HANDLER_DATA

        "yfiles.input.LassoSelectionEventArgs" ->
            LASSO_HANDLER_DATA

        else ->
            HandlerData(
                handlerType = "(event:$eventType) -> Unit",
                listenerBody = "{ _, event -> handler(event) }"
            )
    }
}

private val EMPTY_HANDLER_DATA =
    HandlerData(
        handlerType = "() -> Unit",
        listenerBody = "{ _, _ -> handler() }"
    )

private val PROPERTY_HANDLER_DATA =
    HandlerData(
        handlerType = "(propertyName:String) -> Unit",
        listenerBody = "{ _, event -> handler(event.propertyName) }"
    )

private val INPUT_MODE_HANDLER_DATA =
    HandlerData(
        handlerType = "(context:yfiles.input.IInputModeContext) -> Unit",
        listenerBody = "{ _, event -> handler(event.context) }"
    )

private val MARQUEE_HANDLER_DATA =
    HandlerData(
        handlerType = "(rectangle:yfiles.geometry.Rect) -> Unit",
        listenerBody = "{ _, event -> handler(event.rectangle) }"
    )

private val LASSO_HANDLER_DATA =
    HandlerData(
        handlerType = "(selectionPath:yfiles.geometry.GeneralPath) -> Unit",
        listenerBody = "{ _, event -> handler(event.selectionPath) }"
    )

internal data class HandlerData(
    val handlerType: String,
    val listenerBody: String
)
