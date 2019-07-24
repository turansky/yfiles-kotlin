package com.github.turansky.yfiles

internal class ClassRegistry(
    types: List<Type>
) {
    companion object {
        lateinit var instance: ClassRegistry
    }

    private val instances = types.associateBy({ it.classId }, { it })

    private val functionsMap = types.associateBy(
        { it.classId },
        { it.methods.map { it.name } }
    )

    private val propertiesMap = types.associateBy(
        { it.classId },
        { it.properties.map { it.name } }
    )

    private val listenerMap = types
        .asSequence()
        .filterIsInstance<ExtendedType>()
        .associateBy(
            { it.classId },
            { it.events.flatMap { it.listenerNames } }
        )

    private fun getParents(className: String): List<String> {
        val instance = instances.getValue(className)

        return sequenceOf(instance.extendedType())
            .filterNotNull()
            .plus(instance.implementedTypes())
            .map { it.substringBefore("<") }
            .toList()
    }

    private fun functionOverridden(
        className: String,
        functionName: String,
        checkCurrentClass: Boolean
    ): Boolean {
        if (checkCurrentClass) {
            val funs = functionsMap.getValue(className)
            if (funs.contains(functionName)) {
                return true
            }
        }
        return getParents(className).any {
            functionOverridden(it, functionName, true)
        }
    }

    private fun propertyOverridden(
        className: String,
        propertyName: String,
        checkCurrentClass: Boolean
    ): Boolean {
        if (checkCurrentClass) {
            val props = propertiesMap.getValue(className)
            if (props.contains(propertyName)) {
                return true
            }
        }
        return getParents(className).any {
            propertyOverridden(it, propertyName, true)
        }
    }

    private fun listenerOverridden(
        className: String,
        listenerName: String,
        checkCurrentClass: Boolean
    ): Boolean {
        if (checkCurrentClass) {
            val listeners = listenerMap.getValue(className)
            if (listeners.contains(listenerName)) {
                return true
            }
        }
        return getParents(className).any {
            listenerOverridden(it, listenerName, true)
        }
    }

    fun functionOverridden(className: String, functionName: String): Boolean {
        return functionOverridden(className, functionName, false)
    }

    fun propertyOverridden(className: String, propertyName: String): Boolean {
        return propertyOverridden(className, propertyName, false)
    }

    fun listenerOverridden(className: String, listenerName: String): Boolean {
        return listenerOverridden(className, listenerName, false)
    }
}