package com.github.turansky.yfiles

import java.io.File

internal class KotlinFileGenerator(
    moduleName: String,
    private val types: Iterable<Type>,
    private val functionSignatures: Iterable<FunctionSignature>
) : FileGenerator {
    private val moduleAnnotation = "@file:JsModule(\"$moduleName\")"

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

    abstract inner class GeneratedFile(private val declaration: Type) {
        val data = es6GeneratorData(declaration)
        open val useJsName = false

        private val properties: List<Property>
            get() = declaration.properties

        private val staticConstants: List<Constant>
            get() = declaration.constants

        private val staticProperties: List<Property>
            get() = declaration.staticProperties

        private val staticFunctions: List<Method>
            get() = declaration.staticMethods

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
                data.name != data.jsName && !data.isYObject,
                "@JsName(\"${data.jsName}\")\n"
            )

        protected open val suppress: String
            get() = ""

        val header: String
            get() {
                return moduleAnnotation + "\n" +
                        suppress +
                        "package ${data.packageName}\n"
            }

        protected val classDeclaration: String
            get() {
                val name = if (data.isYObject) {
                    data.jsName
                } else {
                    declaration.name
                }

                return name + declaration.generics.declaration
            }

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

        protected val generics: Generics
            get() = declaration.generics

        protected fun getGeneric(): String {
            var generic = data.name
            if (generic == JS_OBJECT) {
                generic = ANY
            }

            return generic + declaration.generics.placeholder
        }

        protected fun typealiasDeclaration(): String? =
            if (data.name != data.jsName) {
                val generics = declaration.generics.asParameters()
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
                |companion object: yfiles.lang.ClassMetadata<${getGeneric()}> {
                |${staticDeclarations.lines { it.toCode() }}
                |}
            """.trimMargin()

        abstract fun companionContent(): String?
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
        override val useJsName = data.primitive || data.isYObject

        // TODO: check after fix
        //  https://youtrack.jetbrains.com/issue/KT-31126
        private fun constructors(): String {
            return declaration
                .secondaryConstructors
                .lines { it.toCode() }
        }

        override fun parentTypes(): List<String> {
            if (data.isYObject) {
                return emptyList()
            }

            val extendedType = declaration.extendedType()
                ?: YOBJECT_CLASS_ALIAS

            return sequenceOf(extendedType)
                .plus(super.parentTypes())
                .toList()
        }

        private fun isObject(): Boolean {
            return declaration.generics.isEmpty() &&
                    declaration.primaryConstructor == null &&
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

            val primaryConstructor = declaration
                .primaryConstructor
                ?.run { toPrimaryCode() }
                ?: ""

            return documentation +
                    externalAnnotation +
                    "external ${declaration.kotlinModificator} class $classDeclaration $primaryConstructor ${parentString()} {\n" +
                    constructors() + "\n\n" +
                    super.content() + "\n\n" +
                    companionObjectContent + "\n" +
                    "}"
        }

        private fun primitiveContent(): String {
            return documentation +
                    "external object ${data.jsName}: yfiles.lang.ClassMetadata<${getGeneric()}>"
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
            if (isObject() || data.primitive || data.isYObject) {
                return null
            }

            var content = listOfNotNull(
                typealiasDeclaration(),
                declaration.toConstructorMethodCode(),
                invokeExtension(declaration.name, declaration.generics)
            )

            val events = memberEvents
                .filter { !it.overriden }

            if (events.isNotEmpty()) {
                content = content + events.lines { it.toExtensionCode() }
            }

            return if (content.isNotEmpty()) {
                content.joinToString("\n\n")
            } else {
                null
            }
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

            val interfaceDeclaration = classDeclaration
                .replace("IEnumerator<", "IEnumerator<out ")
                .replace("IEnumerable<", "IEnumerable<out ")
                .replace("IListEnumerable<", "IListEnumerable<out ")

            return documentation +
                    externalAnnotation +
                    "external interface $interfaceDeclaration ${parentString()} {\n" +
                    content + "\n\n" +
                    companionObjectContent + "\n" +
                    "}"
        }

        private val defaultDeclarations = memberProperties.filter { !it.abstract } +
                memberFunctions.filter { !it.abstract } +
                memberEvents.filter { !it.overriden }

        override fun companionContent(): String? {
            val content = typealiasDeclaration()

            if (defaultDeclarations.isEmpty()) {
                return content
            }

            val extensions = defaultDeclarations
                .lines { it.toExtensionCode() }

            return if (content != null) {
                "$extensions\n\n$content"
            } else {
                extensions
            }
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