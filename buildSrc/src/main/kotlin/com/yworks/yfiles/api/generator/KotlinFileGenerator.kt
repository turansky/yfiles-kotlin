package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.KotlinTypes.UNIT
import com.yworks.yfiles.api.generator.YfilesModule.Companion.findModule
import com.yworks.yfiles.api.generator.YfilesModule.Companion.getQualifier
import java.io.File

private val PROGRAMMING_LANGUAGE = ProgrammingLanguage.KOTLIN

internal class KotlinFileGenerator(
    private val types: Iterable<Type>,
    private val functionSignatures: Iterable<FunctionSignature>
) : FileGenerator {
    override fun generate(directory: File) {
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
            .map { it.toCode(PROGRAMMING_LANGUAGE) }
            .joinToString(", ")
        val returns = functionSignature.returns?.type ?: UNIT

        val content = "typealias ${fqn.name}$generics = ($parameters) -> $returns"
            .replace(redundantPackageDeclaration, "")

        file.writeText("$header\n\n$content")
    }

    abstract inner class GeneratedFile(private val declaration: Type) {
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
            get() {
                val module = findModule(declaration.modules)
                val qualifier = getQualifier(fqn.packageName)
                return "@file:JsModule(\"$module\")\n" +
                        if (qualifier != null) {
                            "@file:JsQualifier(\"$qualifier\")\n"
                        } else {
                            ""
                        } +
                        "package ${fqn.packageName}\n"
            }

        protected open fun parentTypes(): List<String> {
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

        protected open fun isStatic(): Boolean {
            return false
        }

        protected fun companionContent(): String {
            val items = staticDeclarations.map {
                it.toCode(PROGRAMMING_LANGUAGE)
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
                it.toCode(PROGRAMMING_LANGUAGE)
            }

            if (items.isEmpty()) {
                return ""
            }

            return "@JsName(\"$className\")\n" +
                    "external object ${className}s {\n" +
                    items.joinToString("\n") +
                    "}"
        }

        open fun content(): String {
            return listOf<Declaration>()
                .union(memberProperties)
                .union(memberFunctions)
                .map { it.toCode(PROGRAMMING_LANGUAGE) }
                .union(listOf(Hacks.getAdditionalContent(declaration.fqn)))
                .joinToString("\n") + "\n"
        }
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
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
                it.toCode(PROGRAMMING_LANGUAGE)
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

    inner class InterfaceFile(declaration: Interface) : GeneratedFile(declaration) {
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

    inner class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = declaration.constants
                .map { "    ${it.name}" }
                .joinToString(",\n")
            return "external enum class ${fqn.name} {\n" +
                    values + "\n\n" +
                    super.content() + "\n" +
                    "}\n"
        }
    }
}