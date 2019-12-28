package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.json.get

internal fun applyVisualTemplateHacks(source: Source) {
    fixClass(source)
}

private fun fixClass(source: Source) {
    source.type("IVisualTemplate").apply {
        setSingleTypeParameter()

        flatMap(METHODS)
            .map { it[PARAMETERS]["dataObject"] }
            .onEach { it.changeNullability(false) }
            .forEach { it[TYPE] = "T" }
    }
}

internal fun getVisualTemplateParameter(className: String): String =
    when (className) {
        "HandleInputMode" -> "IHandle"
        "DefaultStripeInputVisualizationHelper" -> "IStripe"
        else -> "*"
    }
