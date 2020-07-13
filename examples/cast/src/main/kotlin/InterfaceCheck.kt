@file:Suppress("NESTED_CLASS_IN_EXTERNAL_INTERFACE")

@JsExport
@ExperimentalJsExport
fun interfaceTest() {
    val o = js("{ firstName: 'Frodo', lastName: 'Baggins' }")
    println(ObjectClass.keys(o))
    println(ObjectInterface.keys(o))
}

@JsName("Object")
external class ObjectClass {
    companion object {
        fun keys(o: Any): Array<String>
    }
}

@JsName("Object")
external interface ObjectInterface {
    companion object {
        fun keys(o: Any): Array<String>
    }
}
