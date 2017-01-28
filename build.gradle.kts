import org.gradle.api.GradleException
import java.io.File
import java.nio.charset.Charset

task("build") {
    val source = project.properties["apiFile"] ?: throw GradleException("Invalid 'apiFile' parameter value!")
    val file = file(source) ?: throw GradleException("No file located in '$source'")
    generateKotlinWrappers(file)
}

fun generateKotlinWrappers(sourceFile: File) {
    val lines = sourceFile.readLines(Charset.forName("UTF-8")).iterator()
    val declarations = mutableListOf<Declaration>()
    while (lines.hasNext()) {
        val declaration = DeclarationReader.read(lines)
        if (declaration is ClassDec) {
            println(declaration.data.name)
        }
        declarations.add(declaration)
    }

    println("Declarations count: ${declarations.size}")
    val fileGenerator = FileGenerator(declarations)
    fileGenerator.generate(projectDir.resolve("generated/src/main/kotlin"))
}

object DeclarationReader {
    fun read(lineIterator: Iterator<String>): Declaration {
        if (lineIterator.next() != "/**") {
            throw IllegalStateException("Invalid comment start!")
        }

        val lines = mutableListOf<String>()
        while (true) {
            val line = lineIterator.next()
            if (line == " */") {
                return Declaration.parse(lineIterator.next(), lines)
            }

            lines.add(line.substring(3))
        }
    }
}

open class Declaration(val data: Data) {
    companion object {

        val NAMESPACE = "@namespace"
        val CLASS = "@class "
        val INTERFACE = "@interface "
        val CONSTRUCTOR = "@constructor"

        val CONST = "@const"
        val STATIC = "@static"
        val FINAL = "@final"
        val ABSTRACT = "@abstract"
        val PROTECTED = "@protected"

        val IMPLEMENTS = "@implements"
        val EXTENDS = "@extends"

        val PARAM = "@param"
        val RETURNS = "@returns"
        val TYPE = "@type"

        val GETS_OR_SETS = "Gets or sets"
        val NULL_VALUE = "null;"

        fun parse(source: String, lines: List<String>): Declaration {
            val data = Data.parse(source)

            if (lines.contains(NAMESPACE)) {
                return Namespace(data)
            }

            if (lines.any { it.startsWith(CLASS) } && !lines.contains(CONSTRUCTOR)) {
                return ClassDec(data, lines)
            }

            if (lines.any { it.startsWith(INTERFACE) }) {
                return Interface(data, lines)
            }

            if (lines.contains(CONSTRUCTOR)) {
                return Constructor(data, lines)
            }

            if (lines.contains(CONST)) {
                return Const(data, lines)
            }

            if (data.nullValue) {
                return Property(data, lines)
            }

            return Function(data, lines)
        }
    }
}

class Data(val name: String, val value: String) {
    companion object {
        fun parse(source: String): Data {
            val items = source.split("=")
            if (items.size == 1) {
                val name = source.substring(0, source.length - 1)
                return Data(name, Declaration.NULL_VALUE)
            }

            if (items.size != 2) {
                throw GradleException("Invalid declaration: '$source'")
            }

            return Data(items[0], items[1])
        }
    }

    val nullValue: Boolean
        get() = value == Declaration.NULL_VALUE
}

class ClassDec(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Interface(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Constructor(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Const(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Property(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Function(data: Data, private val lines: List<String>) : Declaration(data) {

}

class Namespace(data: Data) : Declaration(data)

class Undefined(data: Data) : Declaration(data)

class FileGenerator(private val declarations: List<Declaration>) {

    private val classNames: Set<String>

    init {
        classNames = declarations.filter({ it is ClassDec || it is Constructor })
                .map { it.data.name }
                .toSet()

        println(classNames.joinToString(separator = "\n"))
        println("Classes: ${classNames.size}")
    }

    fun generate(directory: File) {
        directory.mkdirs()
        directory.deleteRecursively()

        for (className in classNames) {
            val names = className.split(".")
            val name = names.last()
            val packageNames = names.subList(0, names.size - 1)
            val relativePath = packageNames.joinToString(separator = "/")
            val dir = directory.resolve(relativePath)
            dir.mkdirs()

            val file = dir.resolve("$name.kt")
            file.writeText(
                    "package ${packageNames.joinToString(separator = ".")}\n" +
                            "\n" +
                            "class $name {\n" +
                            "}"
            )
        }
    }
}