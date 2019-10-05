function YObject() {}

function Class() {}

Class.fixType = function (type, name) {
    type["fixedClassName"] = name;
    type.prototype.fixedClassName = name;
};

function BaseClass(...types) {
    return {}
}

function IVisibilityTestable() {}

export {
    YObject,
    Class,
    BaseClass,
    IVisibilityTestable
}