[![Build Status](https://travis-ci.org/turansky/yfiles-kotlin.svg?branch=master)](https://travis-ci.org/turansky/yfiles-kotlin)
[![Gradle Plugin Portal](https://img.shields.io/maven-metadata/v/https/plugins.gradle.org/m2/com/github/turansky/yfiles/com.github.turansky.yfiles.gradle.plugin/maven-metadata.xml.svg?label=gradle)](https://plugins.gradle.org/plugin/com.github.turansky.yfiles)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# yFiles Gradle Plugin for Kotlin/JS

## Base Class

#### Example
```Kotlin
abstract class VisualTemplateBase: IVisualTemplate

abstract class ArrowBase: IArrow, IVisualCreator, IBoundsProvider
```

#### Requirements

- Only yFiles interfaces extended
- No extended class
- No constructors
- No properties
- No methods