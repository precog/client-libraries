var should = require('should'),
  	precog = require('../../src/precog.amd.js'),
  	testAccount = {
  		analyticsService : "http://nebula.precog.com/",
  		apiKey : "45A49AB5-908A-4BE3-AE6F-3D1F9D05D205",
  		basePath : "/0000000023"
  	},
  	root = "/test/jsamd/"+guid()+"/";

function fail(done) {
	return function() {
		should.fail();
		done();	
	}
}

function guid() {
	function s4() {
	  return Math.floor((1 + Math.random()) * 0x10000)
	             .toString(16)
	             .substring(1);
	};
	return s4() + s4() + '-' + s4() + '-' + s4() + '-' + s4() + '-' + s4() + s4() + s4();
}

describe("Api", function() {
  this.timeout(5000);
	describe("ingest", function(done) {
    it("should ingest one JS object", function(done) {

		  var path = root + "ingestobject",
          api = new precog.Api(testAccount);
  		api.ingest(path, { name : "Nathan", age : 103 })
        .then(function(r) {
          r.ingested.should.equal(1);
          done();
        },
          fail(done)
        );
    });

    it("should ingest several JS objects (Array)", function(done) {

      var path = root + "ingestarray",
          api = new precog.Api(testAccount);
      api.ingest(path, [{ name : "Nathan", age : 103 }, { name : "Franco", age : 21 }])
        .then(function(r) {
          r.ingested.should.equal(2);
          done();
        },
          fail(done)
        );
    });

    it("should ingest json string", function(done) {

      var path = root + "ingestjson",
          api = new precog.Api(testAccount);
      api.ingest(path,
          JSON.stringify({ name : "Nathan", age : 103 })+
          "\n"+
          JSON.stringify({ name : "Nathan", age : 205 })
        )
        .then(function(r) {
          r.ingested.should.equal(2);
          done();
        },
          fail(done)
        );
    });
	});
});
