[![Build Status](https://travis-ci.org/turansky/yfiles-kotlin.svg?branch=master)](https://travis-ci.org/turansky/yfiles-kotlin)
[![GitHub license](https://img.shields.io/badge/license-Apache%20License%202.0-blue.svg?style=flat)](https://www.apache.org/licenses/LICENSE-2.0)

# Kotlin/JS declarations generator for yFiles
## [Gradle Plugin](gradle-plugin/README.md)

## Generation
* Run `./gradlew build`
* Check source folders
  * yFiles for HTML - `yfiles-kotlin/src/main/kotlin`
  * VSDX Export - `vsdx-kotlin/src/main/kotlin`
  
## Description
| JS library                  | [yFiles for HTML][11] | [VSDX Export][21] |
| :---                        |         :---:         |      :---:        |
| Documentation               |        [API][12]      |     [API][22]     |
| Module                      |        `yfiles`       |   `yfiles/vsdx`   |
| Version                     |         `2.2.1`       |      `1.0.0`      |
| Module format               |         `ES6`         |       `ES6`       |
| **Kotlin/JS Declarations**  |  **`yfiles-kotlin`**  | **`vsdx-kotlin`** |
| Nullability fixes           |         2500+         |         -         |
| Numberability*              |           ✔           |         ✔         |
| Strict [Class][31] generic  |           ✔           |         ✔         |
| Trait support**             |           ✔           |         ✔         |
| Operators                   |           ✔           |         ✔         |
| Operator aliases            |           ✔           |         ✔         |

\* - `Int`, `Double` instead of `Number`<br>
\** - via extension methods

## KDoc
#### Supported blocks
* Summary
* Remarks
  * Only for properties/methods
  * Started with "The default "
* Preconditions
* Postconditions
* Parameters
* Returns
* Default value
* Throws
* Links
  * Developer Guide
  * Demo

#### Online Documentation
Fast navigation to online documentation supported for classes.

#### Related issues
* [KT-32815](https://youtrack.jetbrains.com/issue/KT-32815) - Broken links with double anchor `#`
* [IDEA-219818](https://youtrack.jetbrains.com/issue/IDEA-219818) - Broken links with double anchor `#`
* [KT-32640](https://youtrack.jetbrains.com/issue/KT-32640) - Broken markdown links in `@see` block
* [KT-32720](https://youtrack.jetbrains.com/issue/KT-32720) - `@see` is recommended?

[11]: https://www.yworks.com/products/yfiles-for-html
[12]: http://docs.yworks.com/yfileshtml/

[21]: https://www.yworks.com/products/yfiles-for-html/vsdx-export
[22]: https://docs.yworks.com/vsdx-html/

[31]: https://docs.yworks.com/yfileshtml/#/api/Class