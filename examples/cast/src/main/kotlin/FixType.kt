@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.lang.YObject

open class BaseA : YObject {
    val x = 13
    val y = 42
}

open class A : BaseA() {
    val xx = 13
    val yy = 42
}

class SuperA : A() {
    val xxx = 13
    val yyy = 42
}

open class BaseC : YObject {
    val x = 13
    val y = 42

    companion object
}

open class C : BaseC() {
    val xx = 13
    val yy = 42

    companion object
}

class SuperC : C() {
    val xxx = 13
    val yyy = 42

    companion object
}

@JsExport
@ExperimentalJsExport
fun fixType() {
    val a = SuperA()
    val c = SuperC()
}
