<a href="https://travis-ci.org/turansky/yfiles-kotlin"><img src="https://travis-ci.org/turansky/yfiles-kotlin.svg?branch=master"></a>

# Kotlin/JS declarations generator for yFiles

## Generation
* Run `./gradlew build`
* Check source folders
  * yFiles for HTML - `yfiles-kotlin/src/main/kotlin`
  * VSDX Export - `vsdx-kotlin/src/main/kotlin`
  
## Description
| Library                  | yFiles for HTML | VSDX Export |
|--------------------------|-----------------|-------------|
| JS module                |      yfiles     | yfiles/vsdx |
| JS module format         |       ES6       |     ES6     |
| Nullability fixes        |      2500+      |      -      |
| Numberability*           |        ✔        |      ✔      |
| Strict `Class` generic   |        ✔        |      ✔      |
| Trait support**          |        ✔        |      ✔      |
| Operators                |        ✔        |      ✔      |
| Operator aliases         |        ✔        |      ✔      |

* \* - `Int`, `Double` instead of `Number`
* \** - via extension methods

## Links
* [yFiles for HTML](https://www.yworks.com/products/yfiles-for-html)
  * [Documentation](http://docs.yworks.com/yfileshtml/)
* [VSDX Export](https://www.yworks.com/products/yfiles-for-html/vsdx-export)
  * [Documentation](https://docs.yworks.com/vsdx-html/)
