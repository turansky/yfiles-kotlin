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

        val memberEvents: List<Event>
            get() = if (declaration is ExtendedType) {
                declaration.events
            } else {
                emptyList()
            }

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

            var overlays = """
                $JS_OVERLAY
                $PUBLIC_STATIC boolean is(Object o) {
                    return jsClass.isInstance(o);
                }

                $JS_OVERLAY
                $PUBLIC_STATIC ${fqn.name} as(Object o) {
                    return is(o) ? $JS.cast(o) : null;
                }
            """

            overlays += if (addStaticDeclarations) {
                """
                @jsinterop.annotations.JsProperty(name="${'$'}class")
                $PUBLIC_STATIC $YFILES_CLASS jsClass;
                """
            } else {
                """
                $JS_OVERLAY
                $PUBLIC_STATIC $YFILES_CLASS jsClass = null;
                """
            }

            var result = declarations
                .plus(memberProperties)
                .plus(memberFunctions)
                .plus(memberEvents)
                .lines { it.toCode(PROGRAMMING_LANGUAGE) }

            // WA: For Class
            if (className != YFILES_CLASS) {
                result += "\n\n\n$overlays"
            }

            return result
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
            return jsType(fqn) +
                    "${type()} ${fqn.name}${genericParameters()}${parentString()} {\n" +
                    // TODO: support
                    // constructors() +
                    super.content() + "\n" +
                    "}"
        }
    }

    inner class InterfaceFile(private val declaration: Interface) : GeneratedFile(declaration) {
        val likeAbstractClass: Boolean by lazy { MixinHacks.defineLikeAbstractClass(className, memberFunctions, memberProperties) }

        override val addStaticDeclarations: Boolean = likeAbstractClass

        override fun content(): String {
            var content = super.content()
            if (!likeAbstractClass) {
                content = content
                    .replace("public ", "")
                    .replace("abstract ", "")
                    .replace("native ", "")
            }

            val type = if (likeAbstractClass) "abstract class" else "interface"
            return jsType(fqn) +
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
                .lines { "    $PUBLIC_STATIC ${fqn.name} ${it.name};" }

            return jsType(fqn) +
                    "public final class ${fqn.name} {\n" +
                    values + "\n\n" +
                    "private ${fqn.name}() {}\n" +
                    "}\n"
        }
    }

    private fun jsType(fqn: FQN) = "@jsinterop.annotations.JsType(isNative=true, namespace=\"${getNamespace(fqn.packageName)}\")\n"

    private val YFILES_CLASS = fixPackage("yfiles.lang.Class")
    private val JS_OVERLAY = "@jsinterop.annotations.JsOverlay"
    private val JS = "jsinterop.base.Js"
    private val PUBLIC_STATIC = "public static"
}
