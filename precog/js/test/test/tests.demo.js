var should = require('should'),
  	precog = require('../../src/precog.amd.js'),
  	demo   = {
  		analyticsService : "http://labcoat.precog.com/",
  		apiKey : "5CDA81E8-9817-438A-A340-F34E578E86F8"
  	};

function fail(done) {
	return function() {
		should.fail();
		done();	
	}
}

describe("demo", function() {
  this.timeout(5000);
	describe("method", function() {
		it("should count more than one element in the demo dataset", function(done) {
			(new precog.Api(demo))
				.query("count(//billing)")
				.then(
					function(data) {
            should.exist(data);
            should.ok(data instanceof Array);
						done();
					},
					fail(done)
				);
		});
	});
});
