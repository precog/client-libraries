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
console.log("a");
			setTimeout(function(){
console.log("b");
				Precog.delete(path,
					function(){
						setTimeout(function(){
console.log("c");
							Precog.query("count(/"+path+")",
								function(result){
console.log("c2");
									ok(result[0] === 0);
									start();
								});
						}, 500);
					},
					function(){
console.log("d");
						ok(false);
						start();
					})
				}, 1000);
		},
		function(){
console.log("e");
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