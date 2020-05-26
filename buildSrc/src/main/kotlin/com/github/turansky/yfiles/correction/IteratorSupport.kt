package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.ContentMode.ITERATOR
import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.ICURSOR
import com.github.turansky.yfiles.IENUMERABLE
import com.github.turansky.yfiles.YOBJECT

internal fun addIteratorSupport(context: GeneratorContext) {
    // language=kotlin
    context[IENUMERABLE, ITERATOR] = """
        operator fun <T> IEnumerable<T>.iterator(): Iterator<T> =
            EnumeratorIterator(getEnumerator())
            
        fun <T> IEnumerable<T>.asSequence(): Sequence<T> =
            iterator().asSequence()
        
        private class EnumeratorIterator<T>(
            private val source: IEnumerator<T>
        ) : Iterator<T> {
        
            private var validCurrent = source.moveNext()
        
            override fun hasNext(): Boolean =
                validCurrent
        
            override fun next(): T {
                val result = source.current
                validCurrent = source.moveNext()
                return result
            }
        }        
    """.trimIndent()

    // language=kotlin
    context[ICURSOR, ITERATOR] = """
        operator fun <T : $YOBJECT> ICursor<T>.iterator(): Iterator<T> =
            CursorIterator(this)
            
        fun <T : $YOBJECT> ICursor<T>.asSequence(): Sequence<T> =
            iterator().asSequence()
        
        private class CursorIterator<T : $YOBJECT>(
            private val source: ICursor<T>
        ) : Iterator<T> {
            
            override fun hasNext(): Boolean =
                source.ok
        
            override fun next(): T {
                val result = source.current
                source.next()
                return result
            }
        }        
    """.trimIndent()
}
