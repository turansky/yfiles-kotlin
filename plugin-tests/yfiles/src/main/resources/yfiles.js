function YObject() {}

function Class() {}

Class.fixType = function (type, name) {
    type.className = name;
    type.prototype.className = name;
};

function BaseClass(...types) {
    const YClass = function () {};
    YClass.className = types.join("-");
    return YClass
}

function IVisibilityTestable() {}

function IBoundsProvider() {}

export {
    YObject,
    Class,
    BaseClass,
    IVisibilityTestable,
    IBoundsProvider
}