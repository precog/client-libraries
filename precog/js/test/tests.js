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

asyncTest( "store event", function() {
	expect(1);
	Precog.store("/unit_test/beta/test/js/store",
		{strTest: "string loaded", numTest: 42},
		callbackIsOk,
		callbackIsError
	);
});

asyncTest( "delete path", function() {
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

asyncTest( "query with limit", function() {
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

asyncTest("retrieve metadata", function() {
	expect(1);
	var store = { value : 1 },
		path  = "/unit_test/beta/test/js/metadata/retrieve";
	Precog.store(path, store,
		createDelayedAction(function() {
			Precog.retrieveMetadata(path, function(result) {
				ok(result['children'].length === 0);
				start();
			}, callbackIsError);
		}),
		callbackIsError
	);
});

// **********************
// ***     ACCOUNT    ***
// **********************
var email     = "testjs@precog.com",
	password  = "123abc",
	ms        = 4000;
function removeAccount(id, callack) {
	Precog.deleteAccount(email, password, id, function() { setTimeout(callack, ms); });
}

function createAccount(callack) {
	Precog.createAccount(email, password, function(r) { setTimeout(function() { callack(r['accountId']); }, ms); });
}
/*
asyncTest("describe account", function() {
	expect(1);
	createAccount(function(id) {
		Precog.describeAccount(email, password, id, function(result) {
			console.log(result);
			ok(false);
			start();
		});
	});
});
*/

/*
asyncTest("create and retrieve key", function() {
	expect(2);
	var grants = { "grants": [{ "type": "read", "path": "/foo/", "expirationDate": null }] };
	Precog.createKey(grants,
		function(result) {
			console.log(result);
		},
		callbackIsError
	);
});
*/

/*
  Precog.createKey(grants, success, failure, options)
  Precog.describeKey(apiKey, success, failure, options)

  Precog.listKeys(success, failure, options)
  Precog.deleteKey(apiKey, success, failure, options)

  Precog.createNewGrant(grant, success, failure, options)
  Precog.describeGrant(grantId, success, failure, options)
  Precog.deleteGrant(grantId, success, failure, options)
  Precog.listChildrenGrant(grantId, success, failure, options)
  Precog.createChildGrant(grantId, child, success, failure, options)

  Precog.retrieveGrants(apiKey, success, failure, options)
  Precog.addGrantToKey(apiKey, grant, success, failure, options)
  Precog.removeGrant(apiKey, grantId, success, failure, options)
*/


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
asyncTest( "query with skip", function() {
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

asyncTest( "query with order field", function() {
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

