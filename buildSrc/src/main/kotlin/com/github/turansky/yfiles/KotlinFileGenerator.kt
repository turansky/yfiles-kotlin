package com.github.turansky.yfiles

import com.github.turansky.yfiles.ContentMode.*

private const val SUPPRESS_TYPE_VARIANCE_CONFLICT = "@Suppress(\"TYPE_VARIANCE_CONFLICT\", \"TYPE_VARIANCE_CONFLICT_IN_EXPANDED_TYPE\")\n"

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
        context[data.fileId, EXTENSIONS] = generatedFile.companionContent() ?: return
    }

    private fun generate(
        context: GeneratorContext,
        signatures: List<FunctionSignature>
    ) {
        val firstFqn = signatures.first().classId
        context[firstFqn, ALIASES] = signatures
            .asSequence()
            .sortedBy { it.classId }
            .map { it.toCode() }
            .joinToString("\n\n")
    }

    abstract inner class GeneratedFile(private val declaration: Type) {
        val data = es6GeneratorData(declaration)

        protected open val hasConstants = true

        private val staticConstants: List<Constant>
            get() = if (hasConstants) {
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
            get() = declaration.memberProperties.filter { !it.generated }

        protected val memberExtensionProperties: List<Property>
            get() = declaration.memberProperties.filter { it.generated }

        protected val memberFunctions: List<Method>
            get() = declaration.memberMethods

        protected val memberExtensionFunctions: List<Method>
            get() = declaration.extensionMethods

        protected val memberEvents: List<Event>
            get() = when (declaration) {
                is ExtendedType -> declaration.events
                else -> emptyList()
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
            if (data.isYObject || data.isYBase) {
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

        open fun content(): String {
            return memberDeclarations
                .lines { it.toCode() }
        }

        protected abstract val metadataClass: String

        protected open val factoryMethod: String = ""

        protected val companionObjectContent: String
            get() {
                val typeDeclaration: String = when {
                    data.isYBase -> ""
                    else -> {
                        val name = if (data.isYObject) data.jsName else data.name
                        val generic = name + declaration.generics.placeholder
                        ": $metadataClass<$generic>"
                    }
                }
                return """
                    |companion object $typeDeclaration {
                    |${staticDeclarations.lines { it.toCode() } + factoryMethod}
                    |}
                """.trimMargin()
            }

        abstract fun companionContent(): String?
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
        private val enumCompanionName = ENUM_COMPANION_MAP[data.jsName]

        override val hasConstants: Boolean =
            enumCompanionName == null && !declaration.enumLike

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

            val enumContent = if (declaration.enumLike) {
                declaration.constants.toContent()
            } else {
                ""
            }

            return documentation +
                    externalAnnotation +
                    "external ${declaration.kotlinModifier} class $classDeclaration $primaryConstructor ${parentString()} {\n" +
                    constructors() + "\n\n" +
                    enumContent +
                    super.content() + "\n\n" +
                    companionObjectContent + "\n" +
                    "}" +
                    enumCompanionContent()
        }

        private fun primitiveContent(): String {
            val objectName = data.jsName
                .removePrefix("Y")
                .toUpperCase()
                .let { "__${it}__" }

            return documentation +
                    """
                        @JsName("${data.jsName}")
                        external object $objectName
                    """.trimIndent()
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

        override val metadataClass: String
            get() = CLASS_METADATA

        override fun companionContent(): String? {
            if (isObject() || data.primitive) {
                return null
            }

            var content = listOfNotNull(
                declaration.toFactoryMethodCode(),
                invokeExtension(
                    className = declaration.name,
                    generics = declaration.generics,
                    final = declaration.final
                ),
                memberExtensionProperties
                    .takeIf { it.isNotEmpty() }
                    ?.run { lines { it.toExtensionCode() } },
                memberExtensionFunctions
                    .takeIf { it.isNotEmpty() }
                    ?.run { lines { it.toExtensionCode() } },
                declaration.getComponents()
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
            return memberProperties +
                    memberFunctions +
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
                    exp(data.fqn == IENUMERABLE, SUPPRESS_TYPE_VARIANCE_CONFLICT) +
                    "external interface $interfaceDeclaration ${parentString()} {\n" +
                    content + "\n\n" +
                    companionObjectContent + "\n" +
                    "}"
        }

        private val defaultDeclarations = memberExtensionProperties +
                memberExtensionFunctions +
                memberEvents.filter { !it.overriden }

        override val metadataClass: String
            get() = INTERFACE_METADATA

        override val factoryMethod: String
            get() = declaration.qiiMethod?.toQiiCode() ?: ""

        override fun companionContent(): String? =
            listOfNotNull(
                invokeExtension(
                    className = declaration.name,
                    generics = declaration.generics
                ),
                defaultDeclarations
                    .takeIf { it.isNotEmpty() }
                    ?.lines { it.toExtensionCode() },
                declaration.getComponents()
            ).takeIf { it.isNotEmpty() }
                ?.joinToString("\n\n")
    }

    inner class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
        override fun content(): String =
            if (declaration.flags) {
                flagsContent()
            } else {
                enumContent()
            }

        private fun enumContent(): String {
            val name = data.name

            return documentation +
                    externalAnnotation +
                    """
                        |external enum class $name: $YENUM<$name> {
                        |${declaration.constants.toContent()}
                        |
                        |   companion object: $ENUM_METADATA<$name>
                        |}
                    """.trimMargin()
        }


        private fun flagsContent(): String {
            val name = data.name

            val members = declaration.constants + declaration.staticMethods

            return documentation +
                    externalAnnotation +
                    """
                        |external class $name 
                        |    private constructor(): $YFLAGS<$name> {
                        |    companion object {
                        |    ${members.lines { it.toCode() }}
                        |    }
                        |}
                    """.trimMargin()
        }

        override val metadataClass: String
            get() = TODO()

        override fun companionContent(): String? =
            null
    }
}

private fun List<Constant>.toContent(): String =
    asSequence()
        .map { it.toEnumValue() }
        .joinToString(separator = ",\n\n", postfix = ";\n")
