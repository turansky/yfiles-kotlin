package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.WEBGL2_RENDERING_CONTEXT

internal fun generateWebGL2Classes(context: GeneratorContext) {
    // language=kotlin
    context[WEBGL2_RENDERING_CONTEXT] =
        """
            external class WebGL2RenderingContext
                internal constructor()
        """.trimIndent()
}
