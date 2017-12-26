package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.Types.UNIT
import java.io.File

internal class FileGenerator(private val types: Iterable<Type>,
                             private val functionSignatures: Iterable<FunctionSignature>) {
    fun generate(directory: File) {
        directory.mkdirs()
        directory.deleteRecursively()

        types.forEach {
            val generatedFile = when (it) {
                is Class -> ClassFile(it)
                is Interface -> InterfaceFile(it)
                is Enum -> EnumFile(it)
                else -> throw IllegalStateException("Undefined type for generation: " + it)
            }

            generate(directory, generatedFile)
        }

        functionSignatures.forEach {
            generate(directory, it)
        }
    }

    private fun generate(directory: File, generatedFile: GeneratedFile) {
        val fqn = generatedFile.fqn
        val dir = directory.resolve(fqn.path)
        dir.mkdirs()

        val redundantPackageDeclaration = fqn.packageName + "."

        val file = dir.resolve("${fqn.name}.kt")
        val header = generatedFile.header
        val content = generatedFile.content()
                .replace(redundantPackageDeclaration, "")
        file.writeText("$header\n\n$content")
    }

    private fun generate(directory: File, functionSignature: FunctionSignature) {
        val fqn = FQN(functionSignature.fqn)
        val dir = directory.resolve(fqn.path)
        dir.mkdirs()

        val packageName = fqn.packageName
        val redundantPackageDeclaration = packageName + "."

        val file = dir.resolve("${fqn.name}.kt")
        val header = "package $packageName"

        val typeparameters = functionSignature.typeparameters
        val generics = if (typeparameters.isNotEmpty()) {
            "<${typeparameters.map { it.name }.joinToString(", ")}>"
        } else {
            ""
        }
        val parameters = functionSignature.parameters
                .joinToString(", ")
        val returns = functionSignature.returns?.type ?: UNIT

        val content = "typealias ${fqn.name}$generics = ($parameters) -> $returns"
                .replace(redundantPackageDeclaration, "")

        file.writeText("$header\n\n$content")
    }
}

private class FQN(val fqn: String) {
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

private abstract class GeneratedFile(private val declaration: Type) {
    val className = declaration.fqn
    val fqn: FQN = FQN(className)

    val properties: List<Property>
        get() = declaration.properties
                .sortedBy { it.name }

    val staticConstants: List<Constant>
        get() = declaration.constants
                .sortedBy { it.name }

    val staticProperties: List<Property>
        get() = declaration.staticProperties
                .sortedBy { it.name }

    val staticFunctions: List<Method>
        get() = declaration.staticMethods
                .sortedBy { it.name }

    val staticDeclarations: List<Declaration>
        get() {
            return mutableListOf<Declaration>()
                    .union(staticConstants)
                    .union(staticProperties)
                    .union(staticFunctions)
                    .toList()
        }

    val memberProperties: List<Property>
        get() = properties.filter { !it.static }

    val memberFunctions: List<Method>
        get() = declaration.methods
                .sortedBy { it.name }

    val header: String
        get() = "package ${fqn.packageName}"

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
            return ""
        }

        val result = items.joinToString("\n") + "\n"
        if (isStatic()) {
            return result
        }

        return "    companion object {\n" +
                result +
                "    }\n"
    }

    protected fun utilContent(): String {
        val items = staticDeclarations.map {
            it.toString()
        }

        if (items.isEmpty()) {
            return ""
        }

        return "external object ${className}Util {\n" +
                items.joinToString("\n") +
                "}"
    }

    open fun content(): String {
        return listOf<Declaration>()
                .union(memberProperties)
                .union(memberFunctions)
                .union(listOf(Hacks.getAdditionalContent(declaration.fqn)))
                .joinToString("\n") + "\n"
    }
}

private class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
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
                ?: return super.parentTypes()

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

private class InterfaceFile(declaration: Interface) : GeneratedFile(declaration) {
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
                content + "\n" +
                "}\n\n" +
                utilContent()
    }
}

private class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
    override fun content(): String {
        val values = declaration.constants
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