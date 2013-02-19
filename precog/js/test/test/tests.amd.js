var assert = require('assert');
var precog = require('../../src/precog.amd.js');

exports['test config'] = function(){
    var api = new precog.Api({analyticsService: "http://example.com"});
    assert.equal("http://example.com", api.config("analyticsService"));
    api.config("analyticsService" , "https://example.com");
    assert.equal("https://example.com", api.config("analyticsService"));
    assert.equal("https", api.config("protocol"));
};
