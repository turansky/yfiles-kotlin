package com.github.turansky.yfiles

import com.github.turansky.yfiles.ContentMode.ALIASES
import com.github.turansky.yfiles.ContentMode.CLASS

private val ENUM_COMPANION_MAP = mapOf(
    "BipartitionAlgorithm" to "BipartitionMark",
    "DfsAlgorithm" to "DfsState"
)

internal class KotlinFileGenerator(
    private val types: Iterable<Type>,
    private val functionSignatures: Iterable<FunctionSignature>,
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
        generatedFile: GeneratedFile,
    ) {
        val content = generatedFile.content()
        val companionContent = generatedFile.companionContent()

        context[generatedFile.data.fileId, CLASS] = if (companionContent != null) {
            "$content\n\n\n$companionContent"
        } else {
            content
        }
    }

    private fun generate(
        context: GeneratorContext,
        signatures: List<FunctionSignature>,
    ) {
        val firstFqn = signatures.first().classId
        context[firstFqn, ALIASES] = signatures
            .asSequence()
            .sortedBy { it.classId }
            .map { it.toCode() }
            .joinToString(System.lineSeparator().repeat(2))
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
            get() = declaration.memberProperties

        protected val memberExtensionProperties: List<Property>
            get() = declaration.memberExtensionProperties

        protected val memberFunctions: List<Method>
            get() = declaration.memberMethods

        protected val memberExtensionFunctions: List<Method>
            get() = declaration.extensionMethods

        protected val memberEvents: List<Event>
            get() = when (declaration) {
                is ExtendedType -> declaration.events
                else -> emptyList()
            }

        protected val memberDeclarations by lazy {
            memberProperties.filterNot(::isHidden) + memberFunctions
        }

        protected open fun isHidden(property: Property) = false

        protected val externalAnnotation: String
            get() = exp(
                data.name != data.jsName,
                "@JsName(\"${data.jsName}\")"
            )

        protected val classDeclaration: String
            get() {
                val name = declaration.name
                return name + declaration.generics.declaration
            }

        protected val documentation
            get() = declaration.documentation

        protected open fun parentTypes(): List<String> {
            return declaration.implementedTypes()
        }

        protected fun parentString(): String {
            var parentTypes = parentTypes()
            if (parentTypes.singleOrNull() == IEVENT_DISPATCHER) parentTypes = listOf(IEVENT_DISPATCHER)
            return if (parentTypes.isEmpty()) "" else ": " + parentTypes.byComma()
        }

        open fun content(): String {
            return memberDeclarations
                .joinToString(System.lineSeparator().repeat(2)) { it.toCode() }
        }

        protected abstract val metadataClass: String

        protected open val factoryMethod: String = ""

        protected val companionObjectContent: String
            get() {
                val content = staticDeclarations.joinToString(System.lineSeparator().repeat(2)) { it.toCode() }
                if (content.isEmpty() && factoryMethod.isEmpty() && metadataClass.isEmpty()) {
                    return ""
                }
                return buildString {
                    appendLine()
                    append("companion object")
                    if (metadataClass.isNotEmpty()) {
                        val generic = data.name + declaration.generics.placeholder
                        append(" : $metadataClass<$generic>")
                    }
                    appendLine(" {")
                    if (content.isNotEmpty()) {
                        appendLine(content.indent())
                    }
                    if (factoryMethod.isNotEmpty()) {
                        appendLine()
                        appendLine(factoryMethod.indent())
                    }
                    append("}")
                }
            }

        abstract fun companionContent(): String?
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
        override fun isHidden(property: Property) =
            declaration.isHidden(property)

        private val enumCompanionName = ENUM_COMPANION_MAP[data.jsName]

        override val hasConstants: Boolean =
            enumCompanionName == null && !declaration.enumLike

        // TODO: check after fix
        //  https://youtrack.jetbrains.com/issue/KT-31126
        private fun constructors(): String {
            return declaration
                .secondaryConstructors
                .joinToString(System.lineSeparator()) { it.toCode() }
        }

        override fun parentTypes(): List<String> {
            return sequenceOf(declaration.extendedType())
                .filterNotNull()
                .plus(super.parentTypes())
                .toList()
        }

        private fun isObject(): Boolean {
            return !declaration.abstract &&
                    declaration.generics.isEmpty() &&
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

            // TODO: move in companion calculation?
            var companionContent = companionObjectContent
            if (declaration.enumLike) {
                check("\n}" in companionContent)
                companionContent = companionContent
                    .replace("\n}", declaration.constants.toContent() + "\n}")
            }

            val components = declaration.getComponents()
                ?.let { it + "\n\n" }
                ?: ""

            val dec = buildList {
                if (declaration.kotlinModifier.isNotEmpty()) {
                    add(declaration.kotlinModifier)
                }
                add("external class")
                add(classDeclaration)
                add(primaryConstructor)
                add(parentString())
            }

            return buildString {
                if (documentation.isNotEmpty()) {
                    appendLine(documentation)
                }
                if (externalAnnotation.isNotEmpty()) {
                    appendLine(externalAnnotation)
                }
                append(dec.joinToString(" ")).appendLine(" {")

                val constructors = constructors()
                if (constructors.isNotEmpty()) {
                    appendLine(constructors.indent())
                }

                val contents = super.content()
                if (contents.isNotEmpty()) {
                    appendLine(contents.indent())
                }

                if (components.isNotEmpty()) {
                    appendLine("//components")
                    appendLine(components.indent())
                }

                if (companionContent.isNotEmpty()) {
                    appendLine(companionContent.indent())
                }

                append("}")
            }
        }

        private fun primitiveContent(): String {
            val objectName = data.jsName
                .removePrefix("Y")
                .uppercase()
                .let { "__${it}__" }

            return """
                        $documentation
                        @JsName("${data.jsName}")
                        external object $objectName
                    """.trimIndent()
        }

        private fun objectContent(): String {
            val code = staticDeclarations
                .joinToString(System.lineSeparator().repeat(2)) { it.toCode() }

            return """
                        |$documentation
                        |$externalAnnotation
                        |external object $classDeclaration {
                        |${code.indent()}
                        |}
                        |${enumCompanionContent()}
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
                declaration.getExtensions()
            )

            val events = memberEvents
            if (events.isNotEmpty()) {
                content = content + events.joinToString(System.lineSeparator().repeat(2)) { it.toExtensionCode() }
            }

            return if (content.isNotEmpty()) {
                content.joinToString(System.lineSeparator().repeat(2))
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
                |sealed external class $name {
                |    companion object {
                |        ${declaration.constants.toContent()}
                |    }
                |}
            """.trimMargin()
        }
    }

    inner class InterfaceFile(private val declaration: Interface) : GeneratedFile(declaration) {
        override fun content(): String {
            var content = super.content()
            if (data.fqn == IENUMERABLE) {
                content = content
                    .replace("item: T", "item: @UnsafeVariance T")
                    .replace("items: T", "items: @UnsafeVariance T")
                    .replace("value: T", "value: @UnsafeVariance T")
                    .replace("elements: $IENUMERABLE<T>", "elements: $IENUMERABLE<@UnsafeVariance T>")
                    .replace("toList(): $LIST<T>", "toList(): $LIST<@UnsafeVariance T>")
                    .replace("Accumulator<T, T>", "Accumulator<T, @UnsafeVariance T>")
            }

            val interfaceDeclaration = classDeclaration
                .replace("IEnumerator<", "IEnumerator<out ")
                .replace("IEnumerable<", "IEnumerable<out ")
                .replace("IListEnumerable<", "IListEnumerable<out ")

            return buildString {
                if (documentation.isNotEmpty()) {
                    appendLine(documentation)
                }
                if (externalAnnotation.isNotEmpty()) {
                    appendLine(externalAnnotation)
                }
                appendLine("external interface $interfaceDeclaration ${parentString()} {")
                if (content.isNotEmpty()) {
                    appendLine(content.indent())
                }
                if (companionObjectContent.isNotEmpty()) {
                    appendLine()
                    appendLine(companionObjectContent.indent())
                }
                appendLine("}")
            }
        }

        private val defaultDeclarations = memberExtensionProperties +
                memberExtensionFunctions +
                memberEvents

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
                declaration.getExtensions(),
                declaration.getComponentExtensions()
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

            return buildString {
                appendLine(documentation)
                appendLine(externalAnnotation)
                appendLine("sealed external class $name {")

                declaration.constants
                    .joinToString(System.lineSeparator().repeat(2)) {
                        it.toCode()
                    }
                    .indent()
                    .run(::appendLine)

                appendLine("}")
            }
        }

        override val metadataClass: String
            get() = error("Enums don't have metadata classes")

        private fun flagsContent(): String {
            val name = data.name
            val members = declaration.constants + declaration.staticMethods
            return """
                        |$documentation
                        |$externalAnnotation
                        |external class $name private constructor(): $YFLAGS<$name> {
                        |  companion object {
                        |${members.joinToString(System.lineSeparator().repeat(2)) { it.toCode() }.indent(INDENT.repeat(2))}
                        |  }
                        |}
                    """.trimMargin()
        }

        override fun companionContent(): String? =
            null
    }
}

private fun List<Constant>.toContent(): String =
    asSequence()
        .map { it.toCode() }
        .joinToString("\n\n")
