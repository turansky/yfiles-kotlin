package com.github.turansky.yfiles.correction

import com.github.turansky.yfiles.GeneratorContext
import com.github.turansky.yfiles.KPROPERTY
import com.github.turansky.yfiles.READ_ONLY_PROPERTY
import com.github.turansky.yfiles.READ_WRITE_PROPERTY

internal val OBSERVABLE = "yfiles.graph.Observable"
internal val MAKE_OBSERVABLE = "yfiles.styles.Templates.makeObservable"

internal fun generateObservableDelegates(context: GeneratorContext) {
    // language=kotlin
    context[OBSERVABLE] =
        """
        fun <T: Any> $TAG.deepObservable(value: T): IPropertyProvider<$TAG, T> {
            check(jsTypeOf(value) == "object")
            makeObservable()            
            return TagPropertyProvider(value)
        }    
        
        fun <T> $TAG.observable(initialValue: T): $READ_WRITE_PROPERTY<$TAG, T> {
            makeObservable()
            return Property(initialValue)
        }    
            
        fun <T> Any.observable(initialValue: T): $READ_WRITE_PROPERTY<Any, T> {
            return Property(initialValue)
        }
        
        interface IPropertyProvider<P:Any, T:Any> {
            operator fun provideDelegate(
                thisRef: P,
                property: $KPROPERTY<*>
            ): $READ_ONLY_PROPERTY<P, T>
        }
        
        private class TagPropertyProvider<T:Any>(
            private val value: T
        ): IPropertyProvider<$TAG, T> {
            override operator fun provideDelegate(
                thisRef: $TAG,
                property: $KPROPERTY<*>
            ): $READ_ONLY_PROPERTY<$TAG, T> {
                val propertyName = property.name
                value.firePropertyChanged = { 
                    thisRef.firePropertyChanged(propertyName)
                }
                
                return Constant(value)
            }
        }
        
        private class Constant<P:Any, T:Any>(
            private val value: T
        ) : $READ_ONLY_PROPERTY<P, T> {
            override fun getValue(
                thisRef: P,
                property: $KPROPERTY<*>
            ): T =
                value
        }
        
        private class Property<P:Any, T>(
            initialValue: T
        ) : $READ_WRITE_PROPERTY<P, T> {
            private var value: T = initialValue
        
            override fun getValue(
                thisRef: P,
                property: $KPROPERTY<*>
            ): T =
                value
        
            override fun setValue(
                thisRef: P,
                property: $KPROPERTY<*>,
                value: T
            ) {
                if (this.value != value) {
                    this.value = value
                    thisRef.firePropertyChanged(property.name)
                }
            }
        }
         
        private inline fun $TAG.makeObservable() {
            if (firePropertyChangedDeclared) {
                $MAKE_OBSERVABLE(this)
            }
        }
         
        private inline var Any.firePropertyChanged: (propertyName: String) -> Unit
            get() = asDynamic().firePropertyChanged
            set(noinline value) {
                asDynamic().firePropertyChanged = value
            }
        
        private inline val Any.firePropertyChangedDeclared: Boolean
            get() = !!asDynamic().firePropertyChanged
        """.trimIndent()
}
