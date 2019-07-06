package com.github.turansky.yfiles

internal class ClassRegistry(
    types: List<Type>
) {
    companion object {
        lateinit var instance: ClassRegistry
    }

    private val instances = types.associateBy({ it.fqn }, { it })

    private val functionsMap = types.associateBy(
        { it.fqn },
        { it.methods.map { it.name } }
    )

    private val propertiesMap = types.associateBy(
        { it.fqn },
        { it.properties.map { it.name } }
    )

    private val listenerMap = types
        .asSequence()
        .filterIsInstance<ExtendedType>()
        .associateBy(
            { it.fqn },
            { it.events.flatMap { it.listenerNames } }
        )

    private fun getParents(className: String): List<String> {
        val instance = instances.getValue(className)

        return sequenceOf(instance.extendedType())
            .filterNotNull()
            .plus(instance.implementedTypes())
            .map { if (it.contains("<")) till(it, "<") else it }
            .toList()
    }

    private fun functionOverriden(className: String, functionName: String, checkCurrentClass: Boolean): Boolean {
        if (checkCurrentClass) {
            val funs = functionsMap.getValue(className)
            if (funs.contains(functionName)) {
                return true
            }
        }
        return getParents(className).any {
            functionOverriden(it, functionName, true)
        }
    }

    private fun propertyOverriden(className: String, propertyName: String, checkCurrentClass: Boolean): Boolean {
        if (checkCurrentClass) {
            val props = propertiesMap.getValue(className)
            if (props.contains(propertyName)) {
                return true
            }
        }
        return getParents(className).any {
            propertyOverriden(it, propertyName, true)
        }
    }

    private fun listenerOverriden(className: String, listenerName: String, checkCurrentClass: Boolean): Boolean {
        if (checkCurrentClass) {
            val listeners = listenerMap.getValue(className)
            if (listeners.contains(listenerName)) {
                return true
            }
        }
        return getParents(className).any {
            listenerOverriden(it, listenerName, true)
        }
    }

    fun functionOverriden(className: String, functionName: String): Boolean {
        return functionOverriden(className, functionName, false)
    }

    fun propertyOverriden(className: String, propertyName: String): Boolean {
        return propertyOverriden(className, propertyName, false)
    }

    fun listenerOverriden(className: String, listenerName: String): Boolean {
        return listenerOverriden(className, listenerName, false)
    }
}