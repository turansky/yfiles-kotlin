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

        val file = dir.resolve("${data.jsName}.kt")
        val header = generatedFile.header

        val content = generatedFile.content()
            .clear(data)
        file.writeText("$header\n$content")

        var companionContent = generatedFile.companionContent()
            ?: return

        companionContent = "@file:Suppress(\"NOTHING_TO_INLINE\")\n" +
                "package ${data.packageName}\n\n" +
                companionContent.clear(data)

        dir.resolve("${data.jsName}.ext.kt")
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

    private fun String.clear(data: AbstractGeneratorData): String {
        var content = replace(data.packageName + ".", "")
            .replace(Regex("(\\n\\s?){3,}"), "\n\n")
            .replace(Regex("(\\n\\s?){2,}}"), "\n}")

        val regex = Regex("yfiles\\.([a-z]+)\\.([A-Za-z0-9]+)")
        val importedClasses = regex
            .findAll(content)
            .map { it.value }
            .distinct()
            // TODO: remove after es6name use
            // WA for duplicated class names (Insets for example)
            .filterNot { it.endsWith("." + data.name) }
            .plus(
                STANDARD_IMPORTED_TYPES
                    .asSequence()
                    .filter { content.contains(it) }
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

        protected val typeparameters: List<TypeParameter>
            get() = declaration.typeparameters

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

        open fun isObject() = false

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

        open fun companionContent(): String? {
            if (isObject()) {
                return null
            }

            val className = data.name
            val yclass = "${className}.yclass"

            val generics = genericParameters()
            val classDeclaration = className + generics

            return """
                |inline fun Any?.is$className() = 
                |   ${yclass}.isInstance(this)
                |
                |inline fun $generics Any?.as$className(): $classDeclaration? =
                |   if ( is$className() ) {
                |       unsafeCast<$classDeclaration>()
                |   } else {
                |       null
                |   }
                |
                |inline fun $generics Any?.to$className(): $classDeclaration =
                |   as$className()!!
            """.trimMargin()
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

        override fun isObject(): Boolean {
            return declaration.constructors.isEmpty() &&
                    memberDeclarations.isEmpty() &&
                    !data.marker ||
                    data.primitive
        }

        override fun content(): String {
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

            return externalAnnotation +
                    "external ${type()} ${data.name}${genericParameters()} $constructor ${parentString()} {\n" +
                    constructors() + "\n\n" +
                    super.content() + "\n\n" +
                    companionObjectContent + "\n" +
                    "}"
        }

        private fun objectContent(): String {
            val code = if (data.primitive) {
                yclass()
            } else {
                staticDeclarations.map {
                    it.toCode()
                }.lines()
            }

            return """
                |external object ${data.jsName} {
                |$code
                |}
            """.trimMargin()
        }

        override fun companionContent(): String? {
            if (data.packageName == "yfiles.lang" || data.name.endsWith("Args")) {
                return null
            }

            var companionContent = super.companionContent()
                ?: return null

            val generics = genericParameters()
            val events = memberEvents
                .filter { !it.overriden }

            if (events.isNotEmpty()) {
                val classDeclaration = data.name + generics
                companionContent += "\n\n" + events
                    .lines { it.toExtensionCode(classDeclaration, typeparameters) }
            }

            return companionContent
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

            return externalAnnotation +
                    "external interface ${data.name}${genericParameters()}${parentString()} {\n" +
                    content + "\n\n" +
                    companionObjectContent + "\n" +
                    "}"
        }

        private val defaultDeclarations = memberProperties.filter { !it.abstract } +
                memberFunctions.filter { !it.abstract } +
                memberEvents.filter { !it.overriden }

        override fun companionContent(): String? {
            val content = requireNotNull(super.companionContent())

            if (defaultDeclarations.isEmpty()) {
                return content
            }

            val generics = genericParameters()
            val classDeclaration = data.name + generics
            val extensions = defaultDeclarations
                .lines {
                    when (it) {
                        is Property -> it.toExtensionCode(classDeclaration, typeparameters)
                        is Method -> it.toExtensionCode(classDeclaration, typeparameters)
                        is Event -> it.toExtensionCode(classDeclaration, typeparameters)
                        else -> throw IllegalStateException("Invalid default declaration")
                    }
                }

            return "$content\n\n$extensions"
        }
    }

    inner class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = declaration.constants
                .asSequence()
                .map { "    ${it.name}" }
                .joinToString(separator = ",\n", postfix = ";\n")

            return externalAnnotation +
                    "external enum class ${data.name} {\n" +
                    values + "\n" +
                    super.content() + "\n" +
                    "}"
        }

        override fun isObject() = true
    }
}