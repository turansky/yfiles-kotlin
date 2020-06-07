package com.github.turansky.yfiles.correction

private val CURRENT_MODE: CorrectionMode = CorrectionMode.NORMAL

internal enum class CorrectionMode {
    NORMAL,
    PROGRESSIVE;

    companion object {
        fun isNormal(): Boolean =
            CURRENT_MODE == NORMAL

        fun isProgressive(): Boolean =
            CURRENT_MODE == PROGRESSIVE

        fun <T : Any> getValue(nv: T, pv: T): T =
            when (CURRENT_MODE) {
                NORMAL -> nv
                PROGRESSIVE -> pv
            }

        fun test(mode: CorrectionMode?): Boolean =
            mode == null || mode == CURRENT_MODE
    }
}
