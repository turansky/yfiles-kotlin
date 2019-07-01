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

private fun getEventHandlerData(argsType: String): HandlerData =
    when (argsType) {
        "yfiles.lang.EventArgs" ->
            EMPTY_HANDLER_DATA

        "yfiles.lang.PropertyChangedEventArgs" ->
            PROPERTY_HANDLER_DATA

        "yfiles.input.InputModeEventArgs" ->
            INPUT_MODE_HANDLER_DATA

        else -> {
            println(argsType)
            HandlerData(
                handlerType = "(args:$argsType) -> Unit",
                listenerBody = "{ _, args -> handler(args) }"
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
        listenerBody = "{ _, args -> handler(args.propertyName) }"
    )

private val INPUT_MODE_HANDLER_DATA =
    HandlerData(
        handlerType = "(context:IInputModeContext) -> Unit",
        listenerBody = "{ _, args -> handler(args.context) }"
    )

internal data class HandlerData(
    val handlerType: String,
    val listenerBody: String
)