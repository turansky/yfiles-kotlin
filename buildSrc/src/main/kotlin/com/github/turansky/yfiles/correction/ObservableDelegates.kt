package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.KPROPERTY
import com.github.turansky.yfiles.READ_WRITE_PROPERTY

internal val OBSERVABLE = "yfiles.lang.Observable"

internal fun generateObservableDelegates(context: GeneratorContext) {
    // language=kotlin
    context[OBSERVABLE] =
        """
        fun <P : Any, T> P.observable(initialValue: T): $READ_WRITE_PROPERTY<P, T> {
            if (firePropertyChangedDeclared) {
                yfiles.styles.Templates.makeObservable(this)
            }
        
            return Observable(initialValue)
        }
        
        private class Observable<T>(
            initialValue: T
        ) : $READ_WRITE_PROPERTY<Any, T> {
            private var value: T = initialValue
        
            override fun getValue(
                thisRef: Any,
                property: $KPROPERTY<*>
            ): T =
                value
        
            override fun setValue(
                thisRef: Any,
                property: $KPROPERTY<*>,
                value: T
            ) {
                if (this.value != value) {
                    this.value = value
                    thisRef.firePropertyChanged(property.name)
                }
            }
        }
        
        private fun Any.firePropertyChanged(propertyName: String) {
            asDynamic().firePropertyChanged(propertyName)
        }
        
        private val Any.firePropertyChangedDeclared: Boolean
            get() = !!asDynamic().firePropertyChanged
        """.trimIndent()
}
