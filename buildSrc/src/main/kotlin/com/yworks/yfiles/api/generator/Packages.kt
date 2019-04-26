package com.yworks.yfiles.api.generator

internal val ROOT_PACKAGE = "com.yworks."

internal fun fixPackage(pkg: String): String {
    return when {
        pkg.startsWith("system.") ->
            "${ROOT_PACKAGE}yfiles.lang." + pkg.removePrefix("system.")

        pkg.startsWith("yfiles.") ->
            ROOT_PACKAGE + pkg

        else -> pkg
    }
}