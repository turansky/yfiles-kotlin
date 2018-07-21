package com.yworks.yfiles.api.generator

import java.io.File

internal interface FileGenerator {
    fun generate(directory: File)
}