import com.yworks.yfiles.api.generator.generateKotlinWrappers
import java.io.File
import java.net.URL
import java.nio.charset.Charset
import java.util.*
import kotlin.reflect.KProperty

buildscript {
    repositories {
        jcenter()
    }
}

task("build") {
    // val source = project.properties["apiPath"] ?: throw GradleException("Invalid 'apiPath' parameter value!")
    val apiPath = "http://docs.yworks.com/yfileshtml/assets/api.04860ba1.js"
    generateKotlinWrappers(loadPath(apiPath), projectDir.resolve("generated/src/main/kotlin"))
}

fun loadPath(path: String): String {
    val start = "var apiData="
    return URL(path).readText(Charset.forName("UTF-8")).substring(start.length)
}