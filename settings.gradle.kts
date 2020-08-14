rootProject.name = "yfiles-kotlin"

include("libraries:yfiles-kotlin")
include("libraries:vsdx-kotlin")

includeBuild("gradle-plugin")
include("gradle-plugin-test")

include("examples:simple-app")
include("examples:data-classes")
include("examples:cast")
include("examples:configurable-properties")
