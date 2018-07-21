package com.yworks.yfiles.api.generator

internal enum class YfilesModule(val id: String, val weight: Int) {
    COMPLETE("complete", 1000),
    VIEW("view", 100),
    LAYOUT("layout", 100),

    LANG("lang", 0),

    VIEW_COMPONENT("view-component", 1),
    VIEW_EDITOR("view-editor", 10),
    VIEW_FOLDING("view-folding", 10),
    VIEW_TABLE("view-table", 10),
    VIEW_GRAPHML("view-graphml", 10),
    VIEW_LAYOUT_BRIDGE("view-layout-bridge", 10),

    ALGORITHMS("algorithms", 1),
    LAYOUT_TREE("layout-tree", 20),
    LAYOUT_ORGANIC("layout-organic", 20),
    LAYOUT_HIERARCHIC("layout-hierarchic", 20),
    LAYOUT_ORTHOGONAL("layout-orthogonal", 40),
    LAYOUT_ORTHOGONAL_COMPACT("layout-orthogonal-compact", 50),
    LAYOUT_FAMILYTREE("layout-familytree", 40),
    LAYOUT_MULTIPAGE("layout-multipage", 40),
    LAYOUT_RADIAL("layout-radial", 40),
    LAYOUT_SERIESPARALLEL("layout-seriesparallel", 30),
    ROUTER_POLYLINE("router-polyline", 20),
    ROUTER_OTHER("router-other", 20);

    val path = "yfiles/$id"

    companion object {
        private val YFILES_PACKAGE = fixPackage("yfiles.")
        private val LANG_PACKAGE = fixPackage("yfiles.lang")

        private val MODULE_MAP = YfilesModule.values()
            .associateBy { it.id }

        fun findModule(modules: List<IModule>): YfilesModule {
            if (modules.size == 1) {
                val module = modules.first()
                val moduleId = module.moduleId

                if (moduleId != null) {
                    return MODULE_MAP[moduleId]
                            ?: throw IllegalArgumentException("Unable to calculate module by id: $moduleId")
                }

                return when (module.text) {
                    "All view modules" -> VIEW_COMPONENT
                    "All modules" -> VIEW_COMPONENT
                    else -> throw IllegalArgumentException("Unable to calculate module!")
                }
            }

            if (modules.size == 2) {
                if (modules.any { it.text == "All layout modules" } && modules.any { it.moduleId == VIEW_LAYOUT_BRIDGE.id }) {
                    return VIEW_LAYOUT_BRIDGE
                }
            }

            return modules
                .map { it.moduleId ?: throw IllegalArgumentException("No moduleId defined for module: $it") }
                .map { MODULE_MAP[it] ?: throw IllegalArgumentException("No module found with id: $it") }
                .sortedBy { it.weight }
                .first()
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