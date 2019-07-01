package com.github.turansky.yfiles

internal fun getHandlerData(listenerType: String): HandlerData =
    when {
        listenerType == "yfiles.lang.EventHandler" || listenerType == "yfiles.lang.EventHandler1<yfiles.lang.EventArgs>" -> {
            HandlerData(
                handlerType = "() -> Unit",
                listenerBody = "{ _, _ -> handler() }"
            )
        }

        listenerType.startsWith("yfiles.lang.EventHandler1<") -> {
            val argsType = between(listenerType, "<", ">")
            HandlerData(
                handlerType = "(args:$argsType) -> Unit",
                listenerBody = "{ _, args -> handler(args) }"
            )
        }

        listenerType == "yfiles.lang.PropertyChangedEventHandler" -> {
            HandlerData(
                handlerType = "(propertyName:String) -> Unit",
                listenerBody = "{ _, args -> handler(args.propertyName) }"
            )
        }

        listenerType == "yfiles.graph.NodeLayoutChangedHandler" -> {
            HandlerData(
                handlerType = "(node: yfiles.graph.INode, oldLayout: yfiles.geometry.Rect) -> Unit",
                listenerBody = "{ _, node, oldLayout -> handler(node, oldLayout) }"
            )
        }

        listenerType == "yfiles.graph.BendLocationChangedHandler" -> {
            HandlerData(
                handlerType = "(bend: yfiles.graph.IBend, oldLocation: yfiles.geometry.Point) -> Unit",
                listenerBody = "{ _, bend, oldLocation -> handler(bend, oldLocation) }"
            )
        }

        else -> throw IllegalArgumentException("No handler data for $listenerType")
    }

internal data class HandlerData(
    val handlerType: String,
    val listenerBody: String
)