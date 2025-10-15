package com.github.turansky.yfiles

internal class ClassRegistry(
    types: List<Type>,
) {
    companion object {
        lateinit var instance: ClassRegistry
    }

    private val instances = types.associateBy { it.classId }

    private val functionsMap = types.associateBy(
        { it.classId },
        { it.memberMethods.map { it.name } }
    )

    private val propertiesMap = types.associateBy(
        { it.classId },
        { it.memberProperties.map { it.name } }
    )

    private fun getParents(className: String): List<String> {
        val instance = instances.getValue(className)

        return sequenceOf(instance.extendedType())
            .filterNotNull()
            .plus(instance.implementedTypes())
            .filterNot { it == IEVENT_DISPATCHER }
            .map { it.substringBefore("<") }
            .toList()
    }

    private fun functionOverridden(
        className: String,
        functionName: String,
        checkCurrentClass: Boolean,
    ): Boolean {
        if (checkCurrentClass) {
            val funs = functionsMap.getValue(className)
            if (functionName in funs) {
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
        checkCurrentClass: Boolean,
    ): Boolean {
        if (checkCurrentClass) {
            val props = propertiesMap.getValue(className)
            if (propertyName in props) {
                return true
            }
        }
        return getParents(className).any {
            propertyOverridden(it, propertyName, true)
        }
    }

    fun functionOverridden(className: String, functionName: String): Boolean {
        return functionOverridden(className, functionName, false)
    }

    fun propertyOverridden(className: String, propertyName: String): Boolean {
        return propertyOverridden(className, propertyName, false)
    }
}
