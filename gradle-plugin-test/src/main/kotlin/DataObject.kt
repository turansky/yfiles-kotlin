import yfiles.lang.ConfigurableProperties
import yfiles.lang.YObject

@ConfigurableProperties
class DataObject(
    val key: String,
    val name: String
) : YObject()
