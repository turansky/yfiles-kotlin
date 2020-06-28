@file:Suppress("unused", "UNUSED_VARIABLE")

import yfiles.graph.observable

class Point3D {
    var x: Double by observable(0.0)
    var y: Double by observable(0.0)
    var z: Double by observable(0.0)
}

class User {
    var firstName: String by observable("Frodo")
    var lastName: String by observable("Baggins")
    var age: Int by observable(42)
}
