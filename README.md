<a href="https://travis-ci.org/turansky/yfiles-kotlin"><img src="https://travis-ci.org/turansky/yfiles-kotlin.svg?branch=master"></a>

# Kotlin/JS declarations generator for yFiles

## Generation
* Run `./gradlew build`
* Check source folders
  * yFiles for HTML - `yfiles-kotlin/src/main/kotlin`
  * VSDX Export - `vsdx-kotlin/src/main/kotlin`
  
## Description
| Library                  | [yFiles for HTML][11] | [VSDX Export][21] |
|--------------------------|-----------------------|-------------------|
| Documentation            |        [API][12]      |     [API][22]     |
| JS module                |         yfiles        |    yfiles/vsdx    |
| JS module format         |          ES6          |        ES6        |
| Nullability fixes        |         2500+         |         -         |
| Numberability*           |           ✔           |         ✔         |
| Strict `Class` generic   |           ✔           |         ✔         |
| Trait support**          |           ✔           |         ✔         |
| Operators                |           ✔           |         ✔         |
| Operator aliases         |           ✔           |         ✔         |

\* - `Int`, `Double` instead of `Number`<br>
\** - via extension methods

[11]: https://www.yworks.com/products/yfiles-for-html
[12]: http://docs.yworks.com/yfileshtml/
[21]: https://www.yworks.com/products/yfiles-for-html/vsdx-export
[22]: https://docs.yworks.com/vsdx-html/