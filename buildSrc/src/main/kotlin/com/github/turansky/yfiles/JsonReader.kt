package com.github.turansky.yfiles

import org.json.JSONObject
import java.io.File
import java.nio.charset.StandardCharsets.UTF_8

internal fun File.readJson(): JSONObject =
    readText(UTF_8)
        .run { substring(indexOf("{")) }
        .run { JSONObject(this) }

internal fun File.readApiJson(action: JSONObject.() -> Unit): JSONObject =
    readJson()
        .toString()
        .fixSystemPackage()
        .fixClassDeclaration()
        .run { JSONObject(this) }
        .apply(action)
        .toString()
        .run { JSONObject(this) }

private fun String.fixSystemPackage(): String =
    replace("\"yfiles.system.", "\"yfiles.lang.")
        .replace("\"system.", "\"yfiles.lang.")

private fun String.fixClassDeclaration(): String =
    replace(""""id":"yfiles.lang.Class"""", """"id":"$YCLASS","es6name":"Class"""")
        .replace(""""name":"Class"""", """"name":"YClass"""")
        .replace(""""yfiles.lang.Class"""", """"$YCLASS"""")
        .replace(""""Array<yfiles.lang.Class>"""", """"Array<$YCLASS>"""")
        .replace(""""yfiles.collections.Map<yfiles.lang.Class,$JS_OBJECT>"""", """"yfiles.collections.Map<$YCLASS,$JS_OBJECT>"""")
