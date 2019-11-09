[![Build Status](https://travis-ci.org/turansky/yfiles-kotlin.svg?branch=master)](https://travis-ci.org/turansky/yfiles-kotlin)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/turansky/yfiles/com.github.turansky.yfiles.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://plugins.gradle.org/plugin/com.github.turansky.yfiles)
[![Kotlin](https://img.shields.io/badge/kotlin-1.3.50-blue.svg)](http://kotlinlang.org)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# yFiles Gradle Plugin for Kotlin/JS

## Goal
- Safe [interface implementing](http://docs.yworks.com/yfileshtml/#/dguide/framework_basic_interfaces#framework_implementing_interfaces)
- OOB `YObject` support

## Base Class

#### Example
```Kotlin
// Generated JS: 
// VisualTemplateBase.prototype = Object.create(BaseClass(IVisualTemplate)) 
abstract class VisualTemplateBase: IVisualTemplate

// Generated JS: 
// ArrowBase.prototype = Object.create(BaseClass(IArrow, IVisualCreator, IBoundsProvider))
abstract class ArrowBase: IArrow, IVisualCreator, IBoundsProvider
```

#### Requirements
- Only yFiles interfaces extended
- No extended class
- No constructors
- No properties
- No methods

## Custom `YObject`

#### Example
```Kotlin
// Generated JS: 
// Class.fixType(SelectionProvider) 
class SelectionProvider : YObject {
    fun isSelected(item: IModelItem): Boolean = /* ... */
}

// Generated JS: 
// Class.fixType(HighlightProvider)
class HighlightProvider : YObject {
    fun isHighlighted(item: IModelItem): Boolean = /* ... */
}
```

#### Requirements
- No extended interfaces