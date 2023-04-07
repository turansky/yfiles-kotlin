external class Object {
    companion object {
        fun getOwnPropertyDescriptor(obj: Any, prop: String): PropertyDescriptor
    }
}

external interface PropertyDescriptor {
    val configurable: Boolean
}
