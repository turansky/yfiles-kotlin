external interface IUser {
    @JsName("__name")
    val name: String
}

@JsName("Object")
external class User : IUser {
    override val name: String
}

@JsExport
@ExperimentalJsExport
fun externalFieldTest() {
    val user1: IUser = js("{ __name: 'Frodo' }")
    println(user1.name)
    println(user1.asDynamic().__name)

    val user2: User = js("{ __name: 'Frodo' }")
    println(user2.name)
    println(user2.asDynamic().__name)
}
