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

    private fun functionOverriden(
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
            functionOverriden(it, functionName, true)
        }
    }

    private fun propertyOverriden(
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
            propertyOverriden(it, propertyName, true)
        }
    }

    private fun listenerOverriden(
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

    private fun getAllParents(className: String): Set<String> {
        val parents = getParents(className)
        if (parents.isEmpty()) {
            return emptySet()
        }

        return (parents + parents.flatMap { getAllParents(it) }).toSet()
    }

    init {
        types.forEach { type ->
            val allParents = getAllParents(type.classId)
            if (allParents.isEmpty()) {
                return@forEach
            }

            val m1 = type.properties.associate { it.name to it.nullable }

            val m2 = (allParents.map { instances.getValue(it) })
                .asSequence()
                .flatMap { it.properties.asSequence() }
                .associate { it.name to it.nullable }

            m1.forEach { (mn, nullable) ->
                val n2 = m2.get(mn)
                if (n2 != null && n2 != nullable) {
                    println("${type.classId}.$mn")
                }
            }
        }

    }
}