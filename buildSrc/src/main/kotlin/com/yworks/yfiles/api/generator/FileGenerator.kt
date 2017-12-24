package com.yworks.yfiles.api.generator

import org.gradle.api.GradleException
import java.io.File

class FileGenerator(private val types: List<JType>) {
    fun generate(directory: File) {
        directory.mkdirs()
        directory.deleteRecursively()

        types.forEach {
            val generatedFile = when (it) {
                is JClass -> ClassFile(it)
                is JInterface -> InterfaceFile(it)
                is JEnum -> EnumFile(it)
                else -> throw GradleException("Undefined type for generation: " + it)
            }

            generate(directory, generatedFile)
        }
    }

    private fun generate(directory: File, generatedFile: GeneratedFile) {
        val fqn = generatedFile.fqn
        val dir = directory.resolve(fqn.path)
        dir.mkdirs()

        val file = dir.resolve("${fqn.name}.kt")
        file.writeText("${generatedFile.header}\n${generatedFile.content()}")
    }

    class FQN(val fqn: String) {
        private val names = fqn.split(".")
        private val packageNames = names.subList(0, names.size - 1)

        val name = names.last()
        val packageName = packageNames.joinToString(separator = ".")
        val path = packageNames.joinToString(separator = "/")

        override fun equals(other: Any?): Boolean {
            return other is FQN && other.fqn == fqn
        }

        override fun hashCode(): Int {
            return fqn.hashCode()
        }
    }
}

private abstract class GeneratedFile(private val declaration: JType) {
    val className = declaration.fqn
    val fqn: FileGenerator.FQN = FileGenerator.FQN(className)

    val properties: List<JProperty>
        get() = declaration.properties
                .sortedBy { it.name }

    val staticFields: List<JField>
        get() = declaration.fields
                .sortedBy { it.name }

    val staticProperties: List<JProperty>
        get() = declaration.staticProperties
                .sortedBy { it.name }

    val staticFunctions: List<JMethod>
        get() = declaration.staticMethods
                .sortedBy { it.name }

    val staticDeclarations: List<JDeclaration>
        get() {
            return mutableListOf<JDeclaration>()
                    .union(staticFields)
                    .union(staticProperties)
                    .union(staticFunctions)
                    .toList()
        }

    val memberProperties: List<JProperty>
        get() = properties.filter { !it.static }

    val memberFunctions: List<JMethod>
        get() = declaration.methods
                .sortedBy { it.name }

    val header: String
        get() = "package ${fqn.packageName}\n"

    open protected fun parentTypes(): List<String> {
        return declaration.implementedTypes()
    }

    protected fun parentString(): String {
        val parentTypes = parentTypes()
        if (parentTypes.isEmpty()) {
            return ""
        }
        return ": " + parentTypes.joinToString(", ")
    }

    fun genericParameters(): String {
        return declaration.genericParameters()
    }

    open protected fun isStatic(): Boolean {
        return false
    }

    protected fun companionContent(): String {
        val items = staticDeclarations.map {
            it.toString()
        }

        if (items.isEmpty()) {
            return when {
                isStatic() -> ""
            // TODO: add companion only if needed
                else -> "    companion object {} \n\n"
            }
        }

        val result = items.joinToString("\n") + "\n"
        if (isStatic()) {
            return result
        }

        return "    companion object {\n" +
                result +
                "    }\n"
    }

    open fun content(): String {
        return listOf<JDeclaration>()
                .union(memberProperties)
                .union(memberFunctions)
                .union(listOf(Hacks.getAdditionalContent(declaration.fqn)))
                .joinToString("\n") + "\n"
    }
}

private class ClassFile(private val declaration: JClass) : GeneratedFile(declaration) {
    override fun isStatic(): Boolean {
        return declaration.static
    }

    private fun type(): String {
        if (isStatic()) {
            return "object"
        }

        val modificator = if (memberFunctions.any { it.abstract } || memberProperties.any { it.abstract }) {
            "abstract"
        } else {
            declaration.modificator
        }

        return modificator + " class"
    }

    private fun constructors(): String {
        val constructorSet = declaration.constructors.toSet()
        return constructorSet.map {
            it.toString()
        }.joinToString("\n") + "\n"
    }

    override fun parentTypes(): List<String> {
        val extendedType = declaration.extendedType()
        if (extendedType == null) {
            return super.parentTypes()
        }

        return listOf(extendedType)
                .union(super.parentTypes())
                .toList()
    }

    override fun content(): String {
        return "external ${type()} ${fqn.name}${genericParameters()}${parentString()} {\n" +
                companionContent() +
                constructors() +
                super.content() + "\n" +
                "}"
    }
}

private class InterfaceFile(declaration: JInterface) : GeneratedFile(declaration) {
    override fun content(): String {
        var content = super.content()
        val likeAbstractClass = MixinHacks.defineLikeAbstractClass(className, memberFunctions, memberProperties)
        if (!likeAbstractClass) {
            content = content.replace("abstract ", "")
                    .replace("open fun", "fun")
                    .replace("\n    get() = definedExternally", "")
                    .replace("\n    set(value) = definedExternally", "")
                    .replace(" = definedExternally", "")
        }

        val type = if (likeAbstractClass) "abstract class" else "interface"
        return "external $type ${fqn.name}${genericParameters()}${parentString()} {\n" +
                companionContent() +
                content + "\n" +
                "}\n"
    }
}

private class EnumFile(private val declaration: JEnum) : GeneratedFile(declaration) {
    override fun content(): String {
        val values = declaration.fields
                .map { "    val ${it.name}: ${it.nameOfClass} = definedExternally" }
                .joinToString("\n")
        return "external object ${fqn.name}: ${Types.ENUM_TYPE} {\n" +
                values + "\n\n" +
                super.content() + "\n" +
                "}\n"
    }
}

private class JsInfo {
    private val COMPLETE = "yfiles/complete"
    private val VIEW = "yfiles/view"
    private val LAYOUT = "yfiles/layout"

    private val LANG = "yfiles/lang"

    private val VIEW_COMPONENT = "yfiles/view-component"
    private val VIEW_EDITOR = "yfiles/view-editor"
    private val VIEW_FOLDING = "yfiles/view-folding"
    private val VIEW_TABLE = "yfiles/view-table"
    private val VIEW_GRAPHML = "yfiles/view-graphml"
    private val VIEW_LAYOUT_BRIDGE = "yfiles/view-layout-bridge"
    private val ALGORITHMS = "yfiles/algorithms"
    private val LAYOUT_TREE = "yfiles/layout-tree"
    private val LAYOUT_ORGANIC = "yfiles/layout-organic"
    private val LAYOUT_HIERARCHIC = "yfiles/layout-hierarchic"
    private val LAYOUT_ORTHOGONAL = "yfiles/layout-orthogonal"
    private val LAYOUT_ORTHOGONAL_COMPACT = "yfiles/layout-orthogonal-compact"
    private val LAYOUT_FAMILYTREE = "yfiles/layout-familytree"
    private val LAYOUT_MULTIPAGE = "yfiles/layout-multipage"
    private val LAYOUT_RADIAL = "yfiles/layout-radial"
    private val LAYOUT_SERIESPARALLEL = "yfiles/layout-seriesparallel"
    private val ROUTER_POLYLINE = "yfiles/router-polyline"
    private val ROUTER_OTHER = "yfiles/router-other"
}