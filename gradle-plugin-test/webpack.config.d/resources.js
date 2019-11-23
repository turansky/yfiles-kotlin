;(function () {
  const path = require('path')

  config.resolve.modules.unshift(
    path.resolve(__dirname, '../../../../gradle-plugin-test/src/main/resources')
  )
})()
