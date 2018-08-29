var exec = require('cordova/exec');

exports.wifiLocation = function (success, error) {
    exec(success, error, 'WifiPlugin', 'wifiLocation', undefined);
};

exports.wifiCollection = function (success, error) {
    exec(success, error, 'WifiPlugin', 'wifiCollection', undefined);
};