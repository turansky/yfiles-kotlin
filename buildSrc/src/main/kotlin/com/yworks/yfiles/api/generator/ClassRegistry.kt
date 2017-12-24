package com.yworks.yfiles.api.generator

import org.gradle.api.GradleException

interface ClassRegistry {
    companion object {
        private var _instance: ClassRegistry = EmptyClassRegistry()

        var instance: ClassRegistry
            get() {
                return _instance
            }
            set(value) {
                _instance = value
            }
    }

    fun isInterface(className: String): Boolean
    fun isFinalClass(className: String): Boolean
    fun functionOverriden(className: String, functionName: String): Boolean
    fun propertyOverriden(className: String, functionName: String): Boolean
}

private class EmptyClassRegistry : ClassRegistry {
    override fun isInterface(className: String): Boolean {
        return false
    }

    override fun isFinalClass(className: String): Boolean {
        return false
    }

    override fun functionOverriden(className: String, functionName: String): Boolean {
        return false
    }

    override fun propertyOverriden(className: String, functionName: String): Boolean {
        return false
    }

}

class ClassRegistryImpl(types: List<JType>) : ClassRegistry {
    private val instances = types.associateBy({ it.fqn }, { it })

    private val functionsMap = types.associateBy(
            { it.fqn },
            { it.methods.map { it.name } }
    )

    private val propertiesMap = types.associateBy(
            { it.fqn },
            { it.properties.map { it.name } }
    )

    private val propertiesMap2 = types.associateBy(
            { it.fqn },
            { it.properties.associateBy({ it.name }, { it.getterSetter }) }
    )

    private fun getParents(className: String): List<String> {
        val instance = instances[className] ?: throw GradleException("Unknown instance type: $className")

        return mutableListOf<String>()
                .union(listOf(instance.extendedType()).filterNotNull())
                .union(instance.implementedTypes())
                .map { if (it.contains("<")) till(it, "<") else it }
                .toList()
    }

    private fun functionOverriden(className: String, functionName: String, checkCurrentClass: Boolean): Boolean {
        if (checkCurrentClass) {
            val funs = functionsMap[className] ?: throw GradleException("No functions found for type: $className")
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
            val props = propertiesMap[className] ?: throw GradleException("No properties found for type: $className")
            if (props.contains(propertyName)) {
                return true
            }
        }
        return getParents(className).any {
            propertyOverriden(it, propertyName, true)
        }
    }

    override fun isInterface(className: String): Boolean {
        return instances[className] is JInterface
    }

    override fun isFinalClass(className: String): Boolean {
        val instance = instances[className]
        return instance is JClass && instance.final
    }

    override fun functionOverriden(className: String, functionName: String): Boolean {
        return functionOverriden(className, functionName, false)
    }

    override fun propertyOverriden(className: String, functionName: String): Boolean {
        return propertyOverriden(className, functionName, false)
    }
}