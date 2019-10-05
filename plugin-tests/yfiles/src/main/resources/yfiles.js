class YObject {}

class Class {
    static fixType(type, name) {
        type["fixedClassName"] = name;
        type.prototype.fixedClassName = name;
    }
}

function BaseClass(...types) {
    return {}
}

class IVisibilityTestable {}

export {
    YObject,
    Class,
    BaseClass,
    IVisibilityTestable
}