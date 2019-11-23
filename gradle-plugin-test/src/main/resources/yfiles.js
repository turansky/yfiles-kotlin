function YObject () {}

function Class () {}

Class.fixType = function (type, name) {
  type.className = name
  type.prototype.className = name
}

function BaseClass (...types) {
  const generic = types
    .map(type => type.name)
    .join('-')
  const className = `BaseClass[${generic}]`

  const YClass = function () {}
  Object.defineProperty(YClass.prototype, 'className', { get: () => className })
  return YClass
}

function IVisibilityTestable () {}

function IBoundsProvider () {}

export {
  YObject,
  Class,
  BaseClass,
  IVisibilityTestable,
  IBoundsProvider
}
