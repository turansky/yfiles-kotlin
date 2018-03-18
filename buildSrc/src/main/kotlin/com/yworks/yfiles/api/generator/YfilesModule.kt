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
        val LANG_PACKAGE = "yfiles.lang"

        fun findModule(pkg: String): YfilesModule {
            return if (pkg == LANG_PACKAGE) {
                LANG
            } else {
                COMPLETE
            }
        }

        fun getQualifier(pkg: String): String {
            return if (pkg == "yfiles.lang") {
                pkg
            } else {
                pkg.substring(pkg.indexOf(".") + 1)
            }
        }
    }

    override fun toString(): String {
        return path
    }
}