package com.yworks.yfiles.api.generator

internal enum class YfilesModule(val id: String) {
    COMPLETE("complete"),
    VIEW("view"),
    LAYOUT("layout"),

    LANG("lang"),

    VIEW_COMPONENT("view-component"),
    VIEW_EDITOR("view-editor"),
    VIEW_FOLDING("view-folding"),
    VIEW_TABLE("view-table"),
    VIEW_GRAPHML("view-graphml"),
    VIEW_LAYOUT_BRIDGE("view-layout-bridge"),
    ALGORITHMS("algorithms"),
    LAYOUT_TREE("layout-tree"),
    LAYOUT_ORGANIC("layout-organic"),
    LAYOUT_HIERARCHIC("layout-hierarchic"),
    LAYOUT_ORTHOGONAL("layout-orthogonal"),
    LAYOUT_ORTHOGONAL_COMPACT("layout-orthogonal-compact"),
    LAYOUT_FAMILYTREE("layout-familytree"),
    LAYOUT_MULTIPAGE("layout-multipage"),
    LAYOUT_RADIAL("layout-radial"),
    LAYOUT_SERIESPARALLEL("layout-seriesparallel"),
    ROUTER_POLYLINE("router-polyline"),
    ROUTER_OTHER("router-other");

    val path = "yfiles/$id"

    companion object {
        val YFILES_PACKAGE = fixPackage("yfiles.")
        val LANG_PACKAGE = fixPackage("yfiles.lang")

        fun findModule(pkg: String): YfilesModule {
            return if (pkg == LANG_PACKAGE) {
                LANG
            } else {
                COMPLETE
            }
        }

        fun getQualifier(pkg: String): String? {
            return when {
                pkg == LANG_PACKAGE -> null
                pkg.startsWith(YFILES_PACKAGE) -> pkg.removePrefix(YFILES_PACKAGE)
                else -> throw IllegalArgumentException("Invalid package: $pkg")
            }
        }
    }

    override fun toString(): String {
        return path
    }
}