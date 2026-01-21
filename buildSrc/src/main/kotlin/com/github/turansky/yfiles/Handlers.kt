package com.github.turansky.yfiles

internal fun getHandlerData(listenerType: String): HandlerData {
    if (listenerType.startsWith("$EVENT_HANDLER1<")) {
        return getEventHandlerData(listenerType.between("<", ">"))
    }

    return when (listenerType) {
        "yfiles.lang.EventHandler" ->
            EMPTY_HANDLER_DATA

        "yfiles.lang.PropertyChangedEventHandler" ->
            PROPERTY_HANDLER_DATA

        "yfiles.graph.NodeLayoutChangedHandler" ->
            HandlerData(
                handlerType = "(node: $INODE, oldLayout: yfiles.geometry.Rect) -> Unit",
                listenerBody = "{ node, oldLayout, _ -> handler(node, oldLayout) }"
            )

        "yfiles.graph.BendLocationChangedHandler" ->
            HandlerData(
                handlerType = "(bend: yfiles.graph.IBend, oldLocation: yfiles.geometry.Point) -> Unit",
                listenerBody = "{ bend, oldLocation, _ -> handler(bend, oldLocation) }"
            )

        else -> throw IllegalArgumentException("No handler data for $listenerType")
    }
}

private fun getEventHandlerData(eventType: String): HandlerData {
    if (eventType.startsWith("$ITEM_EVENT_ARGS<")) {
        val itemType = eventType.between("<", ">")
        return HandlerData(
            handlerType = "(item:$itemType) -> Unit",
            listenerBody = "{ event, _ -> handler(event.item) }"
        )
    }

    if (eventType.startsWith("yfiles.graph.ItemChangedEventArgs<")) {
        val (itemType, valueType) = eventType.between("<", ">").split(",")
        return HandlerData(
            handlerType = "(item:$itemType, oldValue: $valueType?) -> Unit",
            listenerBody = "{ event, _ -> handler(event.item, event.oldValue) }"
        )
    }

    if (eventType.startsWith("yfiles.input.SelectionEventArgs<")) {
        val itemType = eventType.between("<", ">")
        return HandlerData(
            handlerType = "(context:$IINPUT_MODE_CONTEXT, selection: yfiles.collections.IObservableCollection<$itemType>) -> Unit",
            listenerBody = "{ event, _ -> handler(event.context, event.selection) }"
        )
    }

    return when (eventType) {
        "yfiles.lang.EventArgs",
        -> EMPTY_HANDLER_DATA

        "yfiles.lang.PropertyChangedEventArgs",
        -> PROPERTY_HANDLER_DATA

        "yfiles.input.InputModeEventArgs",
        -> INPUT_MODE_HANDLER_DATA

        "yfiles.input.TextEventArgs",
        -> TEXT_HANDLER_DATA

        "yfiles.input.MarqueeSelectionEventArgs",
        -> MARQUEE_HANDLER_DATA

        "yfiles.input.LassoSelectionEventArgs",
        -> LASSO_HANDLER_DATA

        "yfiles.input.HoveredItemChangedEventArgs",
        -> HOVER_HANDLER_DATA

        else ->
            HandlerData(
                handlerType = "(event:$eventType) -> Unit",
                listenerBody = "{ event, _ -> handler(event) }"
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
        listenerBody = "{ event, _ -> handler(event.propertyName) }"
    )

private val INPUT_MODE_HANDLER_DATA =
    HandlerData(
        handlerType = "(context:$IINPUT_MODE_CONTEXT) -> Unit",
        listenerBody = "{ event, _ -> handler(event.context) }"
    )

private val TEXT_HANDLER_DATA =
    HandlerData(
        handlerType = "(context:$IINPUT_MODE_CONTEXT, text:String) -> Unit",
        listenerBody = "{ event, _ -> handler(event.context, event.text) }"
    )

private val MARQUEE_HANDLER_DATA =
    HandlerData(
        handlerType = "(rectangle:yfiles.geometry.Rect) -> Unit",
        listenerBody = "{ event, _ -> handler(event.rectangle) }"
    )

private val LASSO_HANDLER_DATA =
    HandlerData(
        handlerType = "(path:yfiles.geometry.GeneralPath) -> Unit",
        listenerBody = "{ event, _ -> handler(event.path) }"
    )

private val HOVER_HANDLER_DATA =
    HandlerData(
        handlerType = "(item:$IMODEL_ITEM?, oldItem:$IMODEL_ITEM?) -> Unit",
        listenerBody = "{ event, _ -> handler(event.item, event.oldItem) }"
    )

internal data class HandlerData(
    val handlerType: String,
    val listenerBody: String,
)
