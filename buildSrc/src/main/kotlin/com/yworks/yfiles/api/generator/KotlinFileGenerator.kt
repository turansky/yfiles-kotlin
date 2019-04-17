package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.KotlinTypes.UNIT
import com.yworks.yfiles.api.generator.YModule.Companion.findModule
import com.yworks.yfiles.api.generator.YModule.Companion.getQualifier
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

        val companionContent = generatedFile.companionContent()
            ?: return

        dir.resolve("${fqn.name}Companion.kt")
            .writeText(companionContent)
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
            "<${typeparameters.byComma { it.name }}>"
        } else {
            ""
        }
        val parameters = functionSignature.parameters
            .byComma { it.toCode(PROGRAMMING_LANGUAGE) }
        val returns = functionSignature.returns?.type ?: UNIT

        val content = "typealias ${fqn.name}$generics = ($parameters) -> $returns"
            .replace(redundantPackageDeclaration, "")

        file.writeText("$header\n\n$content")
    }

    abstract inner class GeneratedFile(private val declaration: Type) {
        protected val className = declaration.fqn
        val fqn: FQN = FQN(className)

        protected val properties: List<Property>
            get() = declaration.properties
                .sortedBy { it.name }

        protected val staticConstants: List<Constant>
            get() = declaration.constants
                .sortedBy { it.name }

        protected val staticProperties: List<Property>
            get() = declaration.staticProperties
                .sortedBy { it.name }

        protected val staticFunctions: List<Method>
            get() = declaration.staticMethods
                .sortedBy { it.name }

        protected val staticDeclarations: List<Declaration>
            get() {
                return sequenceOf<Declaration>()
                    .plus(staticConstants)
                    .plus(staticProperties)
                    .plus(staticFunctions)
                    .toList()
            }

        protected val memberProperties: List<Property>
            get() = properties.filter { !it.static }

        protected val memberFunctions: List<Method>
            get() = declaration.methods
                .sortedBy { it.name }

        protected val memberEvents: List<Event>
            get() = if (declaration is ExtendedType) {
                declaration.events
            } else {
                emptyList()
            }

        protected val memberDeclarations = sequenceOf<Declaration>()
            .plus(memberProperties)
            .plus(memberFunctions)
            .plus(memberEvents)
            .toList()

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
            return ": " + parentTypes.byComma()
        }

        fun genericParameters(): String {
            return declaration.genericParameters()
        }

        open fun isObject() = false

        open fun content(): String {
            return memberDeclarations
                .lines { it.toCode(PROGRAMMING_LANGUAGE) }
        }

        protected fun staticContent(): String {
            if (isObject()) {
                return ""
            }

            val items = staticDeclarations.map {
                it.toCode(PROGRAMMING_LANGUAGE)
            }

            return """
                |@JsName("$className")
                |external object ${className}Static {
                |    ${items.lines()}
                |
                |    @JsName("\${"$"}class")
                |    internal val yclass: ${fixPackage("yfiles.lang.Class")}
                |}
            """.trimMargin()
        }

        fun companionContent(): String? {
            if (isObject()) {
                return null
            }

            return "package ${fqn.packageName}"
        }
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
        private fun type(): String {
            val modificator = if (memberFunctions.any { it.abstract } || memberProperties.any { it.abstract }) {
                "abstract"
            } else {
                declaration.kotlinModificator
            }

            return modificator + " class"
        }

        private fun constructors(): String {
            return declaration
                .constructors
                .asSequence()
                .distinct()
                .lines { it.toCode(PROGRAMMING_LANGUAGE) }
        }

        override fun parentTypes(): List<String> {
            val extendedType = declaration.extendedType()
                ?: return super.parentTypes()

            return sequenceOf(extendedType)
                .plus(super.parentTypes())
                .toList()
        }

        override fun isObject(): Boolean {
            return declaration.constructors.isEmpty() &&
                    memberDeclarations.isEmpty() &&
                    !isMarkerClass(className)
        }

        override fun content(): String {
            if (isObject()) {
                return objectContent()
            }

            return "external ${type()} ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    constructors() +
                    super.content() + "\n" +
                    "}\n\n\n" +
                    staticContent()
        }

        private fun objectContent(): String {
            val items = staticDeclarations.map {
                it.toCode(PROGRAMMING_LANGUAGE)
            }

            return """
                |external object ${className} {
                |    ${items.lines()}
                |}
            """.trimMargin()
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
                    staticContent()
        }
    }

    inner class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = declaration.constants
                .asSequence()
                .map { "    ${it.name}" }
                .joinToString(separator = ",\n", postfix = ";\n")

            return "external enum class ${fqn.name} {\n" +
                    values + "\n" +
                    super.content() + "\n" +
                    "}\n"
        }

        override fun isObject() = true
    }
}