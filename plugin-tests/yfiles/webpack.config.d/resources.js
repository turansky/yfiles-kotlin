(function () {
    const path = require('path');

    config.resolve.modules.unshift(
        path.resolve(__dirname, "../../../../plugin-tests/yfiles/src/main/resources")
    );
})();
