import web.prompts.alert
import yfiles.lang.IClassMetadata
import yfiles.lang.YObject
import yfiles.lang.classMetadata

class CustomObject : YObject {
    fun hallo() {
        alert("Hallo from CustomObject!")
    }

    companion object : IClassMetadata<CustomObject> by classMetadata()
}

class OtherCustomObject : YObject {
    fun hallo() {
        alert("Hallo from OtherCustomObject!")
    }
}
