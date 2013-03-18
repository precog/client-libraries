"use strict";

var precog = require("../../src/precog.amd.js");

console.log(precog);

exports.fulfilled = function() {
	console.log(new precog.Deferred());
	return new precog.Deferred().fulfill.apply(null, arguments);
};

exports.rejected = function() {
	return new precog.Deferred().reject.apply(null, arguments);
};

exports.pending = function () {
    var deferred = new precog.Deferred();

    return {
        promise : deferred.promise,
        fulfill : deferred.fulfill,
        reject  : deferred.reject
    };
};