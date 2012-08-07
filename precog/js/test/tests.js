asyncTest( "storePass", function() {
	expect(1);
	Precog.store("/unit_test/beta/test/js/store",
		{strTest: "string loaded", numTest: 42},
		function(){
			ok(true);
			start();
		},
		function(){
			ok(false);
			start();
		}
	);
});
//TODO error code is not sufficient information

asyncTest( "storeFail", function() {
	expect(1);
	Precog.store("?",
		{strTest: "string loaded", numTest: 42},
		function(){
			ok(false);
			start();
		},
		function(e){
			console.log(e);
			ok(true);
			start();
		}
	);
});

asyncTest( "queryPass", function() {
	expect(1);
	var timeStamp = +new Date(),
		event = {strTest: "string loaded", numTest: 42, time:timeStamp};
	Precog.store("/unit_test/beta/test/js/store",
		event, 
		function(){
			var query = "data := //unit_test/beta/test/js/store data where data.time = "+timeStamp;
			setTimeout(function() {
				Precog.query(query, function(result) {
					deepEqual(result, [event]);
					start();
				})
			}, 3000);
		},
		function(){
			ok(false);
			start();
		}
	);
});