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

asyncTest( "deletePass", function() {
	expect(1);
	var path = "/unit_test/beta/test/js/delete";
	Precog.store(path,
		{strTest: "string loaded", numTest: 42},
		function(){
			setTimeout(function(){
				Precog.deletePath(path,
					function(){
						setTimeout(function(){
							Precog.query("count(/"+path+")",
								function(result){
									ok(result[0] === 0);
									start();
								});
						}, 500);
					},
					function(){
						ok(false);
						start();
					}
				);
			}, 1000);
		},
		function(){
			ok(false);
			start();
		}
	);
});

asyncTest( "query", function() {
	expect(1);
	var timeStamp = +new Date(),
		event = {strTest: "string loaded", numTest: 42, time:timeStamp};
	Precog.store("/unit_test/beta/test/js/query",
		event,
		function(){
			var query = "data := //unit_test/beta/test/js/query data where data.time = "+timeStamp;
console.log(query);
			setTimeout(function() {
				Precog.query(query, function(result) {
console.log(result, event);
					deepEqual(result, [event]);
					start();
				});
			}, 5000);
		},
		function(){
			ok(false);
			start();
		}
	);
});

asyncTest( "limitPass", function() {
	expect(1);

	var store1 = {strTest: "string loaded", numTest: 42};

	Precog.store("/unit_test/beta/test/js/store", store1,
		function(){
			Precog.store("/unit_test/beta/test/js/store", store1,
				function(){
					var query = "//unit_test/beta/test/js/store";
					setTimeout(
						function(){
							Precog.query(query,
								function(result){
									ok(result.length === 1);
									start();
								},
								function(){
									ok(false);
									start();
								},
								{limit: 1}
							);
						}, 5000
					);
				}
			);
		},
		function(){
			ok(false);
			start();
		}
	);
});

asyncTest( "basePathPass", function() {
	expect(1);
	var timeStamp = +new Date(),
		event = {strTest: "string loaded", numTest: 42, time:timeStamp};
	Precog.store("/unit_test/beta/test/js/store",
		event,
		function(){
			var query = "data := //js/store data where data.time = "+timeStamp;
			setTimeout(function() {
				Precog.query(query,
					function(result) {
					deepEqual(result, [event]);
					start();
				},
						function(){
							ok(false);
							start();
						},
							{basePath: "unit_test/beta/test/"}
				);
			}, 3000);
		},
		function(){
			ok(false);
			start();
		}
	);
});

asyncTest( "skipPass", function() {
	expect(1);
	Precog.store("/unit_test/beta/test/js/skip", "A");
	Precog.store("/unit_test/beta/test/js/skip", "B",
		function(){
			var query = "//unit_test/beta/test/js/skip";
			setTimeout(function() {
				Precog.query(query,
					function(result1) {
						Precog.query(query,
							function(result2){
								ok(result1 != result2);
								start();
							},
								function(){
								ok(false);
								start();
								},
									{limit: 1, skip: 1}

						);
					},
					function(){
						ok(false);
						start();
					},
					{limit: 1, skip: 0}
				);
			}, 3000);
		},
		function(){
			ok(false);
			start();
		}
	);
});

asyncTest( "orderPass", function() {
	expect(1);
	var timeStamp = +new Date(),
		event = {strTest: "string loaded", numTest: 42, time:timeStamp};
	Precog.store("/unit_test/beta/test/js/store",
		event,
		function(){
			var query = "data := //unit_test/beta/test/js/store count(data where data.time = "+timeStamp+")";
			setTimeout(function() {
				Precog.query(query,
					function(result) {
					ok(result ===0);
					start();
				},
				function(){
					ok(false);
					start();
				},
				{order: "ascending"}
				);
			}, 3000);
		},
		function(){
			ok(false);
			start();
		}
	);
});

