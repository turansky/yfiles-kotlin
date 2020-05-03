package com.github.turansky.yfiles

import com.github.turansky.yfiles.ContentMode.*

private val ENUM_COMPANION_MAP = mapOf(
    "BipartitionAlgorithm" to "BipartitionMark",
    "DfsAlgorithm" to "DfsState"
)

internal class KotlinFileGenerator(
    private val types: Iterable<Type>,
    private val functionSignatures: Iterable<FunctionSignature>
) : FileGenerator {
    override fun generate(context: GeneratorContext) {
        context.clean()

        types.forEach {
            val generatedFile = when (it) {
                is Class -> ClassFile(it)
                is Interface -> InterfaceFile(it)
                is Enum -> EnumFile(it)
            }

            generate(context, generatedFile)
        }

        functionSignatures
            .groupBy { it.classId.substringBeforeLast(".") }
            .forEach { (_, items) -> generate(context, items) }
    }

    private fun generate(
        context: GeneratorContext,
        generatedFile: GeneratedFile
    ) {
        val data = generatedFile.data
        val mode = if (generatedFile is InterfaceFile) INTERFACE else CLASS

        context[data.fileId, mode] = generatedFile.content()
            .clear(data)

        val companionContent = generatedFile.companionContent()
            ?.clear(data)
            ?: return

        context[data.fileId, EXTENSIONS] = companionContent
    }

    private fun generate(
        context: GeneratorContext,
        signatures: List<FunctionSignature>
    ) {
        val firstData = GeneratorData(signatures.first().classId)

        context[firstData.fqn, ALIASES] = signatures
            .asSequence()
            .sortedBy { it.classId }
            .map { it.toCode() }
            .joinToString("\n\n")
            .clear(firstData)
    }

    abstract inner class GeneratedFile(private val declaration: Type) {
        val data = es6GeneratorData(declaration)

        protected val enumCompanionName = ENUM_COMPANION_MAP[data.jsName]

        private val properties: List<Property>
            get() = declaration.properties

        private val staticConstants: List<Constant>
            get() = if (enumCompanionName == null) {
                declaration.constants
            } else {
                emptyList()
            }

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

        protected val memberExtensionFunctions: List<Method>
            get() = declaration.extensionMethods

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
            if (data.isYObject) {
                return ""
            }

            var parentTypes = parentTypes()
            when {
                parentTypes.isEmpty() ->
                    parentTypes = listOf(YOBJECT)

                parentTypes.singleOrNull() == IEVENT_DISPATCHER ->
                    parentTypes = listOf(YOBJECT, IEVENT_DISPATCHER)
            }
            return ": " + parentTypes.byComma()
        }

        protected fun getGeneric(): String {
            var generic = data.name
            if (generic == JS_OBJECT) {
                generic = ANY
            }

            return generic + declaration.generics.placeholder
        }

        protected fun typealiasDeclaration(): String? =
            if (data.name != data.jsName && !data.isYObject && !data.isYBase) {
                val generics = declaration.generics.asAliasParameters()
                "typealias ${data.jsName}$generics = ${data.name}$generics"
            } else {
                null
            }

        open fun content(): String {
            return memberDeclarations
                .lines { it.toCode() }
        }

        protected open val metadataClass: String
            get() = "yfiles.lang.ClassMetadata"

        protected val companionObjectContent: String
            get() = """
                |companion object: $metadataClass<${getGeneric()}> {
                |${staticDeclarations.lines { it.toCode() }}
                |}
            """.trimMargin()

        abstract fun companionContent(): String?
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
        // TODO: check after fix
        //  https://youtrack.jetbrains.com/issue/KT-31126
        private fun constructors(): String {
            return declaration
                .secondaryConstructors
                .lines { it.toCode() }
        }

        override fun parentTypes(): List<String> {
            return sequenceOf(declaration.extendedType())
                .filterNotNull()
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
                    "}" +
                    enumCompanionContent()
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
                        |}${enumCompanionContent()}
                    """.trimMargin()
        }

        override fun companionContent(): String? {
            if (isObject() || data.primitive) {
                return null
            }

            var content = listOfNotNull(
                typealiasDeclaration(),
                declaration.toFactoryMethodCode(),
                invokeExtension(
                    className = declaration.name,
                    generics = declaration.generics,
                    final = declaration.final
                ),
                memberExtensionFunctions
                    .takeIf { it.isNotEmpty() }
                    ?.run { lines { it.toExtensionCode() } }
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

        private fun enumCompanionContent(): String {
            val name = enumCompanionName
                ?: return ""

            return """
                |
                |@JsName("${data.jsName}")
                |external enum class $name {
                |${declaration.constants.toContent()}
                |}
            """.trimMargin()
        }
    }

    inner class InterfaceFile(private val declaration: Interface) : GeneratedFile(declaration) {
        override fun calculateMemberDeclarations(): List<JsonWrapper> {
            return memberProperties.filter { it.abstract } +
                    memberFunctions.filter { it.abstract } +
                    memberEvents
        }

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
                memberExtensionFunctions +
                memberEvents.filter { !it.overriden }

        override val metadataClass: String
            get() = "yfiles.lang.InterfaceMetadata"

        override fun companionContent(): String? =
            listOfNotNull(
                invokeExtension(
                    className = declaration.name,
                    generics = declaration.generics
                ),
                defaultDeclarations.run {
                    if (isNotEmpty()) lines { it.toExtensionCode() } else null
                },
                typealiasDeclaration()
            ).takeIf { it.isNotEmpty() }
                ?.joinToString("\n\n")
    }

    inner class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
        override fun content(): String {
            val name = data.name

            val interfaces = "$YENUM<$name>" + exp(declaration.flags, ",yfiles.lang.Flags<$name>")

            return documentation +
                    externalAnnotation +
                    """
                        |external enum class $name: $interfaces {
                        |${declaration.constants.toContent()}
                        |
                        |   companion object: $metadataClass<$name> {
                        |   ${declaration.staticMethods.lines { it.toCode() }}
                        |   }
                        |}
                    """.trimMargin()
        }

        override fun companionContent(): String? =
            typealiasDeclaration()
    }
}

private fun List<Constant>.toContent(): String =
    asSequence()
        .map { it.toEnumValue() }
        .joinToString(separator = ",\n\n", postfix = ";\n")
