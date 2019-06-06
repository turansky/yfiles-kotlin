package com.github.turansky.yfiles

internal fun fixPackage(pkg: String): String {
    return when {
        pkg.startsWith("system.") ->
            "yfiles.lang." + pkg.removePrefix("system.")

        // WA for PropertyChangedEventHandler
        pkg.startsWith("yfiles.system.") ->
            "yfiles.lang." + pkg.removePrefix("yfiles.system.")

        pkg.startsWith("yfiles.") ->
            pkg

        else -> pkg
    }
}