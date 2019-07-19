package com.github.turansky.yfiles

import java.io.File

private const val MODULE = "@file:JsModule(\"yfiles\")"

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
            }

            generate(directory, generatedFile)
        }

        functionSignatures
            .groupBy { it.classId.substringBeforeLast(".") }
            .forEach { _, items -> generate(directory, items) }
    }

    private fun generate(
        directory: File,
        generatedFile: GeneratedFile
    ) {
        val data = generatedFile.data
        val dir = directory.resolve(data.path)
        dir.mkdirs()

        val fileName = if (generatedFile.useJsName) {
            data.jsName
        } else {
            data.name
        }

        val file = dir.resolve("$fileName.kt")
        val header = generatedFile.header

        val content = generatedFile.content()
            .clear(data)
        file.writeText("$header\n$content")

        var companionContent = generatedFile.companionContent()
            ?: return

        companionContent = "package ${data.packageName}\n\n" +
                companionContent.clear(data)

        if (generatedFile !is EnumFile) {
            companionContent = "@file:Suppress(\"NOTHING_TO_INLINE\")\n\n" +
                    companionContent
        }

        dir.resolve("$fileName.ext.kt")
            .writeText(companionContent)
    }

    private fun generate(
        directory: File,
        signatures: List<FunctionSignature>
    ) {
        val firstData = GeneratorData(signatures.first().classId)
        val dir = directory.resolve(firstData.path)
        dir.mkdirs()

        val file = dir.resolve("Aliases.kt")
        val header = "package ${firstData.packageName}"

        val content = signatures
            .asSequence()
            .sortedBy { it.classId }
            .map { it.toCode() }
            .joinToString("\n\n")
            .clear(firstData)

        file.writeText("$header\n\n$content")
    }

    private fun String.clear(data: GeneratorData): String {
        var content = replace(data.packageName + ".", "")
            .replace(Regex("(\\n\\s?){3,}"), "\n\n")
            .replace(Regex("(\\n\\s?){2,}}"), "\n}")

        val regex = Regex("yfiles\\.([a-z]+)\\.([A-Za-z0-9]+)")

        val code = content
            .split("\n")
            .asSequence()
            .filterNot { it.startsWith(" *") }
            .joinToString("\n")

        val importedClasses = regex
            .findAll(code)
            .map { it.value }
            .distinct()
            // TODO: remove after es6name use
            // WA for duplicated class names (Insets for example)
            .filterNot { it.endsWith("." + data.name) }
            .plus(
                STANDARD_IMPORTED_TYPES
                    .asSequence()
                    .filter { code.contains(it) }
            )
            .sorted()
            .toList()

        if (importedClasses.isEmpty()) {
            return content
        }

        val imports = importedClasses
            .lines { "import $it" }

        for (className in importedClasses) {
            val name = className.substringAfterLast(".")
            content = content.replace(className, name)
        }

        return "$imports\n$content"
    }

    abstract inner class GeneratedFile(private val declaration: Type) {
        val data = es6GeneratorData(declaration)
        open val useJsName = true

        private val typeparameters: List<TypeParameter>
            get() = declaration.typeparameters

        protected val properties: List<Property>
            get() = declaration.properties
                .sorted()

        protected val staticConstants: List<Constant>
            get() = declaration.constants
                .sorted()

        protected val staticProperties: List<Property>
            get() = declaration.staticProperties
                .sorted()

        protected val staticFunctions: List<Method>
            get() = declaration.staticMethods
                .sorted()

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
                .sorted()

        protected val memberEvents: List<Event>
            get() = if (declaration is ExtendedType) {
                declaration.events
            } else {
                emptyList()
            }

        protected val memberDeclarations by lazy { calculateMemberDeclarations() }

        protected open fun calculateMemberDeclarations(): List<JsonWrapper> {
            return memberProperties + memberFunctions + memberEvents
        }

        protected val externalAnnotation: String
            get() = exp(
                data.name != data.jsName,
                "@JsName(\"${data.jsName}\")\n"
            )

        protected open val suppress: String
            get() = ""

        val header: String
            get() {
                return MODULE + "\n" +
                        suppress +
                        "package ${data.packageName}\n"
            }

        protected val classDeclaration
            get() = declaration.classDeclaration

        protected val documentation
            get() = declaration.documentation

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

        protected fun getGeneric(): String {
            var generic = data.name
            if (generic == JS_OBJECT) {
                generic = ANY
            }

            if (typeparameters.isNotEmpty()) {
                generic += "<" + (1..typeparameters.size).map { "*" }.joinToString(",") + ">"
            }
            return generic
        }

        protected fun yclass() =
            """
                    |@JsName("\${"$"}class")
                    |val yclass: yfiles.lang.Class<${getGeneric()}>
                """.trimMargin()

        protected fun typealiasDeclaration(): String? =
            if (data.name != data.jsName) {
                val generics = genericParameters()
                "typealias ${data.jsName}$generics = ${data.name}$generics"
            } else {
                null
            }

        open fun content(): String {
            return memberDeclarations
                .lines { it.toCode() }
        }

        protected open val companionObjectContent =
            """
                |companion object {
                |${yclass()}
                |
                |${staticDeclarations.lines { it.toCode() }}
                |}
            """.trimMargin()

        abstract fun companionContent(): String?
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
        override val useJsName = data.primitive

        private fun type(): String {
            val modificator = if (memberFunctions.any { it.abstract } || memberProperties.any { it.abstract }) {
                "abstract"
            } else {
                declaration.kotlinModificator
            }

            return modificator + " class"
        }

        // TODO: check after fix
        //  https://youtrack.jetbrains.com/issue/KT-31126
        private fun constructors(): String {
            val constructors = declaration.constructors

            if (constructors.size <= 1) {
                return ""
            }

            return constructors
                .dropLast(1)
                .asSequence()
                .distinct()
                .lines { it.toCode() }
        }

        override fun parentTypes(): List<String> {
            val extendedType = declaration.extendedType()
                ?: return super.parentTypes()

            return sequenceOf(extendedType)
                .plus(super.parentTypes())
                .toList()
        }

        private fun isObject(): Boolean {
            return declaration.constructors.isEmpty() &&
                    memberDeclarations.isEmpty() &&
                    !data.marker
        }

        override fun content(): String {
            if (data.primitive) {
                return primitiveContent()
            }

            if (isObject()) {
                return objectContent()
            }

            val lastConstructor = declaration.constructors
                .lastOrNull()

            val constructor = if (lastConstructor != null) {
                lastConstructor.toCode()
                    .removePrefix(" constructor")
            } else {
                ""
            }

            return documentation +
                    externalAnnotation +
                    "external ${type()} $classDeclaration $constructor ${parentString()} {\n" +
                    constructors() + "\n\n" +
                    super.content() + "\n\n" +
                    companionObjectContent + "\n" +
                    "}"
        }

        private fun primitiveContent(): String {
            return documentation +
                    """
                        |external object ${data.jsName} {
                        |${yclass()}
                        |}
                    """.trimMargin()
        }

        private fun objectContent(): String {
            val code = staticDeclarations
                .lines { it.toCode() }

            return documentation +
                    externalAnnotation +
                    """
                        |external object $classDeclaration {
                        |$code
                        |}
                    """.trimMargin()
        }

        override fun companionContent(): String? {
            if (isObject() || data.primitive || data.packageName == "yfiles.lang" || data.name.endsWith("Args")) {
                return null
            }

            var content = ""

            val events = memberEvents
                .filter { !it.overriden }

            if (events.isNotEmpty()) {
                content = events
                    .lines { it.toExtensionCode() } +
                        "\n\n" +
                        content
            }

            typealiasDeclaration()?.also {
                content = it + "\n\n" + content
            }

            return content.takeIf { it.isNotEmpty() }
        }
    }

    inner class InterfaceFile(declaration: Interface) : GeneratedFile(declaration) {
        override fun calculateMemberDeclarations(): List<JsonWrapper> {
            return memberProperties.filter { it.abstract } +
                    memberFunctions.filter { it.abstract } +
                    memberEvents
        }

        override val suppress: String
            get() = "@file:Suppress(\"NESTED_CLASS_IN_EXTERNAL_INTERFACE\")\n"

        override fun content(): String {
            val content = super.content()
                .replace("abstract ", "")

            return documentation +
                    externalAnnotation +
                    "external interface $classDeclaration ${parentString()} {\n" +
                    content + "\n\n" +
                    companionObjectContent + "\n" +
                    "}"
        }

        private val defaultDeclarations = memberProperties.filter { !it.abstract } +
                memberFunctions.filter { !it.abstract } +
                memberEvents.filter { !it.overriden }

        override fun companionContent(): String? {
            val content = interfaceCastExtensions(
                className = data.name,
                generics = genericParameters()
            )

            if (defaultDeclarations.isEmpty()) {
                return content
            }

            val extensions = defaultDeclarations
                .lines { it.toExtensionCode() }

            return "$extensions\n\n$content"
        }
    }

    inner class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = declaration.constants
                .asSequence()
                .map { it.toEnumValue() }
                .joinToString(separator = ",\n\n", postfix = ";\n")

            return documentation +
                    externalAnnotation +
                    "external enum class ${data.name} {\n" +
                    values + "\n" +
                    super.content() + "\n" +
                    "}"
        }

        override fun companionContent(): String? =
            typealiasDeclaration()
    }
}