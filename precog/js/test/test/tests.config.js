var should = require('should'),
    precog = require('../../src/precog.amd.js');

describe('config', function() {
  describe('analyticsService', function() {
    it("should normalize the service path", function() {
      var api = new precog.Api({analyticsService: "http://example.com/"});
      api.config("analyticsService").should.equal("http://example.com");
      api.config("protocol").should.equal("http");
      api.config("port").should.equal(80);
      api.config("hostname").should.equal("example.com");
    });

    it("should update the service path", function() {
      var api = new precog.Api({analyticsService: "http://sample.com/"});
      api.config("analyticsService" , "https://example.com");
      api.config("analyticsService").should.equal("https://example.com");
      api.config("protocol").should.equal("https");
      api.config("port").should.equal(443);
      api.config("hostname").should.equal("example.com");
    });

    it("should change protocol case", function() {
      var api = new precog.Api({analyticsService: "http://example.com/"});
      api.config("protocol", "HTTPS");
      api.config("protocol").should.equal("https");
      api.config("port").should.equal(443);
      api.config("hostname").should.equal("example.com");
    });
  });
});