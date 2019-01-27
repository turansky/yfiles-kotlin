package com.yworks.yfiles.api.generator

import com.yworks.yfiles.api.generator.JavaTypes.VOID
import com.yworks.yfiles.api.generator.YfilesModule.Companion.getNamespace
import java.io.File

private val PROGRAMMING_LANGUAGE = ProgrammingLanguage.JAVA

internal class JavaFileGenerator(
    private val types: Iterable<Type>,
    private val functionSignatures: Iterable<FunctionSignature>
) : FileGenerator {
    override fun generate(directory: File) {
        directory.mkdirs()
        directory.deleteRecursively()

        val generatedFiles = types.map {
            when (it) {
                is Class -> ClassFile(it)
                is Interface -> InterfaceFile(it)
                is Enum -> EnumFile(it)
                else -> throw IllegalStateException("Undefined type for generation: " + it)
            }
        }

        val interfaceRegistry = InterfaceRegistry(
            generatedFiles
                .asSequence()
                .filterIsInstance<InterfaceFile>()
                .filter { !it.likeAbstractClass }
                .map { it.className }
                .toSet()
        )

        generatedFiles
            .forEach { it.registry = interfaceRegistry }

        generatedFiles.forEach {
            generate(directory, it)
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

        val file = dir.resolve("${fqn.name}.java")
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

        val file = dir.resolve("${fqn.name}.java")
        val header = "package $packageName;"

        val typeparameters = functionSignature.typeparameters
        val generics = if (typeparameters.isNotEmpty()) {
            "<${typeparameters.byComma { it.name }}>"
        } else {
            ""
        }
        val parameters = functionSignature.parameters
            .byComma { it.toCode(PROGRAMMING_LANGUAGE) }
        val returns = functionSignature.returns?.type ?: VOID

        val content = ("@jsinterop.annotations.JsFunction\n" +
                "public interface ${fqn.name}$generics {\n" +
                "   $returns execute($parameters);\n" +
                "}")
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

        val memberProperties: List<Property>
            get() = properties
                .asSequence()
                .filter { !it.static }
                // WA: temp, for compilation only
                .filterNot { it.nameOfClass == "NavigationInputMode" && it.name == "graphComponent" }
                .toList()

        val memberFunctions: List<Method>
            get() = declaration.methods
                .sortedBy { it.name }

        val header: String
            get() {
                return "package ${fqn.packageName};\n"
            }

        lateinit var registry: InterfaceRegistry

        protected open fun extendsTypes(): List<String> {
            return emptyList()
        }

        protected open fun implementsTypes(): List<String> {
            return emptyList()
        }

        protected fun parentString(): String {
            return toString("extends", extendsTypes()) +
                    toString("implements", implementsTypes())
        }

        private fun toString(keyword: String, types: List<String>): String {
            if (types.isEmpty()) {
                return ""
            }

            return " " + keyword + " " + types.byComma()
        }

        fun genericParameters(): String {
            return declaration.genericParameters()
        }

        protected open val addStaticDeclarations: Boolean = true

        open fun content(): String {
            val declarations = if (addStaticDeclarations) {
                sequenceOf(
                    staticConstants,
                    staticProperties,
                    staticFunctions
                ).flatten()
            } else {
                emptySequence()
            }

            return declarations
                .plus(memberProperties)
                .plus(memberFunctions)
                .lines { it.toCode(PROGRAMMING_LANGUAGE) }
        }
    }

    inner class ClassFile(private val declaration: Class) : GeneratedFile(declaration) {
        private fun type(): String {
            val modificator = if (memberFunctions.any { it.abstract } || memberProperties.any { it.abstract }) {
                "abstract"
            } else {
                declaration.javaModificator
            }

            return "public $modificator class"
        }

        private fun constructors(): String {
            return declaration
                .constructors
                .asSequence()
                .distinct()
                .lines { it.toCode(PROGRAMMING_LANGUAGE) }
        }

        override fun extendsTypes(): List<String> {
            val extendedType = declaration.extendedType()
            return if (extendedType != null) {
                listOf(extendedType)
            } else {
                declaration.implementedTypes()
                    .filterNot(registry::contains)
            }
        }

        override fun implementsTypes(): List<String> {
            return declaration.implementedTypes()
                .filter(registry::contains)
        }

        override fun content(): String {
            val namespace = getNamespace(fqn.packageName)
            return "@jsinterop.annotations.JsType(isNative=true, namespace=\"$namespace\")\n" +
                    "${type()} ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    // TODO: support
                    // constructors() +
                    super.content() + "\n" +
                    "}"
        }
    }

    inner class InterfaceFile(private val declaration: Interface) : GeneratedFile(declaration) {
        val likeAbstractClass: Boolean by lazy { MixinHacks.defineLikeAbstractClass(className, memberFunctions, memberProperties) }

        override val addStaticDeclarations: Boolean = false

        override fun content(): String {
            var content = super.content()
            if (!likeAbstractClass) {
                content = content.replace("public abstract ", "")
                    .replace("native ", "")
            }

            val namespace = getNamespace(fqn.packageName)
            val type = if (likeAbstractClass) "abstract class" else "interface"
            return "@jsinterop.annotations.JsType(isNative=true, namespace=\"$namespace\")\n" +
                    "public $type ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    content + "\n" +
                    "}"
        }

        override fun extendsTypes(): List<String> {
            if (likeAbstractClass) {
                return declaration.implementedTypes()
                    .asSequence()
                    .filterNot(registry::contains)
                    .toList()
            }

            return declaration.implementedTypes()
        }

        override fun implementsTypes(): List<String> {
            if (likeAbstractClass) {
                return declaration.implementedTypes()
                    .asSequence()
                    .filter(registry::contains)
                    .toList()
            }

            return super.implementsTypes()
        }
    }

    class InterfaceRegistry(private val items: Set<String>) {
        fun contains(type: String): Boolean {
            val fqn = type.split("<")[0]
            return items.contains(fqn)
        }
    }

    // TODO: check if fields can be final
    inner class EnumFile(private val declaration: Enum) : GeneratedFile(declaration) {
        override fun content(): String {
            val values = declaration.constants
                .lines { "    public static ${fqn.name} ${it.name};" }

            val namespace = getNamespace(fqn.packageName)
            return "@jsinterop.annotations.JsType(isNative=true, namespace=\"$namespace\")\n" +
                    "public final class ${fqn.name} {\n" +
                    values + "\n\n" +
                    "private ${fqn.name}() {}\n" +
                    "}\n"
        }
    }
}