package com.yworks.yfiles.api.generator

internal fun fixPackage(pkg: String): String {
    return when {
        pkg.startsWith("system.") ->
            "com.yworks.yfiles.lang." + pkg.removePrefix("system.")

        pkg.startsWith("yfiles.") ->
            "com.yworks." + pkg

        else -> pkg
    }
}