package com.yworks.yfiles.api.generator

internal val ROOT_PACKAGE = ""

internal fun fixPackage(pkg: String): String {
    return when {
        pkg.startsWith("system.") ->
            "${ROOT_PACKAGE}yfiles.lang." + pkg.removePrefix("system.")

        // WA for PropertyChangedEventHandler
        pkg.startsWith("yfiles.system.") ->
            "${ROOT_PACKAGE}yfiles.lang." + pkg.removePrefix("yfiles.system.")

        pkg.startsWith("yfiles.") ->
            ROOT_PACKAGE + pkg

        else -> pkg
    }
}