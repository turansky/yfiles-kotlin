package com.github.turansky.yfiles.ide.highlighter.markers

import com.intellij.openapi.util.IconLoader
import javax.swing.Icon

internal object Icons {
    object Gutter {
        val BASE_CLASS: Icon = IconLoader.getIcon("/icons/base-class.svg")
        val CLASS_FIX_TYPE: Icon = IconLoader.getIcon("/icons/class-fix-type.svg")
    }
}
