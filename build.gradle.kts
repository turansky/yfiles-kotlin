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
    val map = mutableMapOf<Declaration, Doc>()
    var i = 0
    while (lines.hasNext()) {
        val doc = DocReader.read(lines)
        val declaration = DeclarationReader.read(lines)
        map.put(declaration, doc)
    }

    println("Declarations count: ${map.size}")
}

object DocReader {
    fun read(lineIterator: Iterator<String>): Doc {
        if (lineIterator.next() != "/**") {
            throw IllegalStateException("Invalid comment start!")
        }

        val lines = mutableListOf<String>()
        while (true) {
            val line = lineIterator.next()
            if (line == " */") {
                return Doc(lines)
            }

            lines.add(line.substring(3))
        }
    }
}

object DeclarationReader {
    fun read(lineIterator: Iterator<String>): Declaration {
        return Declaration(lineIterator.next())
    }
}

class Doc(private val lines: List<String>) {

}

class Declaration(private val data: String) {

}