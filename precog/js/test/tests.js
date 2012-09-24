function callbackIsOk() {
	ok(true);
	start();
}

function callbackIsError() {
	ok(false);
	start();
}

function createCallBackIsError(repetitions) {
	return function() {
		for(var i = 0; i < repetitions; i++) ok(false);
		start();
	};
}

function createDelayedAction(f) {
	return function() {
		setTimeout(f, 6000);
	};
}

asyncTest( "storePass", function() {
	expect(1);
	Precog.store("/unit_test/beta/test/js/store",
		{strTest: "string loaded", numTest: 42},
		callbackIsOk,
		callbackIsError
	);
});

asyncTest( "deletePass", function() {
	expect(1);
	var path = "/unit_test/beta/test/js/delete";
	Precog.store(path,
		{strTest: "string loaded", numTest: 42},
		createDelayedAction(function(){
			Precog.deletePath(path,
				createDelayedAction(function(){
					Precog.query("count(/"+path+")",
						function(result){
							ok(result[0] === 0);
							start();
						}
					);
				}),
				callbackIsError
			);
		}),
		callbackIsError
	);
});

asyncTest( "query", function() {
	expect(1);
	var timeStamp = +new Date(),
		event = {strTest: "string loaded", numTest: 43, time:timeStamp};
	Precog.store("/unit_test/beta/test/js/query",
		event,
		createDelayedAction(function() {
			var query = "data := //unit_test/beta/test/js/query data where data.time = "+timeStamp;
			Precog.query(query, function(result) {
				deepEqual(result, [event]);
				start();
			});
		}),
		callbackIsError
	);
});

asyncTest( "limitPass", function() {
	expect(1);

	var store1 = {strTest: "string loaded", numTest: 42};

	Precog.store("/unit_test/beta/test/js/store", store1,
		function(){
			Precog.store("/unit_test/beta/test/js/store", store1,
				createDelayedAction(function(){
					var query = "//unit_test/beta/test/js/store";
					Precog.query(query,
						function(result){
							ok(result.length === 1);
							start();
						},
						callbackIsError,
						{limit: 1}
					);
				})
			);
		},
		callbackIsError
	);
});
/*
asyncTest( "basePathPass", function() {
	expect(1);
	var timeStamp = +new Date(),
		event = {strTest: "string loaded", numTest: 42, time:timeStamp};
	Precog.store("/unit_test/beta/test/js/store",
		event,
		createDelayedAction(function() {
			var query = "data := //js/store data where data.time = "+timeStamp;
			Precog.query(query,
				function(result) {
					deepEqual(result, [event]);
					start();
				},
				callbackIsError,
				{basePath: "unit_test/beta/test/"}
			);
		}),
		callbackIsError
	);
});
*/
asyncTest( "skipPass", function() {
	expect(1);
	Precog.store("/unit_test/beta/test/js/skip", "A");
	Precog.store("/unit_test/beta/test/js/skip", "B",
		createDelayedAction(function() {
			var query = "//unit_test/beta/test/js/skip";
			Precog.query(query,
				function(result1) {
					Precog.query(query,
						function(result2){
							ok(result1 != result2);
							start();
						},
						callbackIsError,
						{limit: 1, skip: 1}
					);
				},
				callbackIsError,
				{limit: 1, skip: 0}
			);
		}),
		callbackIsError
	);
});

asyncTest( "orderPass", function() {
	expect(2);
	var timeStamp = +new Date(),
		event1 = { num : 1, time : timeStamp },
		event2 = { num : 2, time : timeStamp };
	Precog.store("/unit_test/beta/test/js/order", event1);
	Precog.store("/unit_test/beta/test/js/order", event2,
		createDelayedAction(function() {
			var query = "data := //unit_test/beta/test/js/order data where data.time = "+timeStamp;
			Precog.query(
				query,
				function(result) {
					ok(result[0] && result[0].num === 1);
					Precog.query(
						query,
						function(result) {
							ok(result[0] && result[0].num === 2);
							start();
						},
						createCallBackIsError(2),
						{ sortOn : "num", sortOrder : "desc"}
					);
				},
				createCallBackIsError(2),
				{ sortOn : "num", sortOrder : "asc"}
			);
		}),
		createCallBackIsError(2)
	);
});

