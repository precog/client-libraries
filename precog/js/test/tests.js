function createDelayedAction(f) {
	return function() {
		setTimeout(f, 6000);
	};
}

// **********************
// ***     ACCOUNT    ***
// **********************

var email    = "testjs@precog.com",
	password = "123abc";

function ensureAccount(callack) {
	Precog.createAccount(email, password, function(r) { callack(r['accountId']); });
}

asyncTest("describe account", function() {
	ensureAccount(function(id) {
		Precog.describeAccount(email, password, id, function(result) {
			equal(result.accountId, id);
			equal(result.email, email);
			ok(result.accountCreationDate);
			ok(result.apiKey);
			ok(result.rootPath);
			ok(result.plan);
			start();
		});
	});
});

asyncTest("list accounts", function() {
	ensureAccount(function(id) {
		Precog.listAccounts(email, password, function(result) {
			ok(result instanceof Array, "expected an array of objects");
			start();
		});
	});
});

asyncTest("describe plan", function() {
	ensureAccount(function(id) {
		Precog.describePlan(email, password, id, function(result) {
			ok(result.type);
			start();
		});
	});
});

/*
	// TESTABLE WITH NO ADMIN ACCOUNT?
	Precog.addGrantToAccount(email, password, accountId, grantId, success, failure, options)
*/



// **********************
// ***    SECURITY    ***
// **********************

asyncTest("create and delete key", function() {
	ensureAccount(function(id) {
		var grants = { "grants": [{ "type": "read", "path": "/foo/", "expirationDate": null, "ownerAccountId" : id }] };
		Precog.createKey(grants,
			function(ak) {
				ok(ak);
				equal(ak.split("-").length, 5);
				Precog.deleteKey(
					ak,
					function() {
						ok(true);
						Precog.describeKey(
							ak,
							function(details) {
								ok(false); // should not follow this path
								start();
							},
							function(e) {
								ok(true);
								start();
							}
						);
					}
				);
			}
		);
	});
});

asyncTest("describe key", function() {
	ensureAccount(function(id) {
		var grants = { "grants": [{ "type": "read", "path": "/foo/", "expirationDate": null, "ownerAccountId" : id }] };
		Precog.createKey(grants,
			function(ak) {
				Precog.describeKey(ak, function(details) {
					equal(details.apiKey, ak);
					ok(details.grants);
					start();
				});
			}
		);
	});
});

asyncTest("create and describe new grant", function() {
	ensureAccount(function(id) {
		var grant = { "type": "read", "path": "/foo/", "ownerAccountId": id, "expirationDate": null };
		Precog.createNewGrant(grant, function(grantId) {
			ok(grantId);
			Precog.describeGrant(grantId, function(result) {
				equal(result.grantId, grantId);
				equal(result.ownerAccountId, id);
				equal(result.path, grant.path);
				equal(result.type, grant.type);
				equal(result.expirationDate, null);
				start();
			});
		});
	});
});

asyncTest("delete grant", function() {
	ensureAccount(function(id) {
		var grant = { "type": "read", "path": "/foo/", "ownerAccountId": id, "expirationDate": null };
		Precog.createNewGrant(grant, function(grantId) {
			Precog.deleteGrant(grantId, function(result) {
				ok(true);
				start();
			});
		});
	});
});

asyncTest("create child grant and list", function() {
	ensureAccount(function(id) {
		var grant1 = { "type": "read", "path": "/foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": "/foo/bar/", "ownerAccountId": id, "expirationDate": null };
		Precog.createNewGrant(grant1, function(g1) {
			Precog.createChildGrant(g1, grant2, function(g2) {
				Precog.listChildrenGrant(g1, function(result) {
					ok(result instanceof Array);
					equal(result.length, 1);
					equal(result[0].ownerAccountId, id);
					equal(result[0].issuer, g1);
					equal(result[0].grantId, g2);
					start();
				});
			});
		});
	});
});

asyncTest("retrieve grants", function() {
	ensureAccount(function(id) {
		var grant1 = { "type": "read", "path": "/foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": "/foo/bar/", "ownerAccountId": id, "expirationDate": null };
		Precog.createNewGrant(grant1, function(g1) {
			Precog.createChildGrant(g1, grant2, function(g2) {
				Precog.retrieveGrants(Precog.$.Config.apiKey, function(result) {
					ok(result instanceof Array);
					ok(result.length > 0);
					start();
				});
			});
		});
	});
});

asyncTest("remove grant", function() {
	ensureAccount(function(id) {
		var grant1 = { "type": "read", "path": "/foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": "/foo/bar/", "ownerAccountId": id, "expirationDate": null };
		Precog.createNewGrant(grant1, function(g1) {
			Precog.createChildGrant(g1, grant2, function(g2) {
				Precog.removeGrant(Precog.$.Config.apiKey, g2, function(r) {
					Precog.listChildrenGrant(g1, function(result) {
						equal(result.length, 0);
						start();
					});
				});
			});
		});
	});
});


/*
	// TESTABLE WITH NO ADMIN ACCOUNT?
  	Precog.addGrantToKey(apiKey, grant, success, failure, options)
*/
/*
asyncTest("list keys", function() {
	ensureAccount(function(id) {
		Precog.listKeys(
			function(keys) {
				console.log(keys);
				ok(keys instanceof Array);
				start();
			}
		);
	});
});
*/
asyncTest( "query with skip", function() {
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
						null,
						{limit: 1, skip: 1}
					);
				},
				null,
				{limit: 1, skip: 0}
			);
		})
	);
});

asyncTest( "query with order field", function() {
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
						null,
						{ sortOn : "num", sortOrder : "desc"}
					);
				},
				null,
				{ sortOn : "num", sortOrder : "asc"}
			);
		})
	);
});

// **********************
// ***     INGEST     ***
// **********************

asyncTest( "store event", function() {
	Precog.store("/unit_test/beta/test/js/store",
		{strTest: "string loaded", numTest: 42},
		function() {
			ok(true);
			start();
		}
	);
});

asyncTest( "ingest csv", function() {
	var path = "/unit_test/beta/test/js/csv2",
		now  = +new Date();
	Precog.ingest(path,
		'"timestamp","index"\n'+now+',1\n'+now+',2\n'+now+',3',
		"csv",
		createDelayedAction(function() {
			Precog.query("ds := /"+path+" ds where ds.timestamp = "+now, function(result) {
				equal(result.length, 3);
				start();
			});
		})
	);
});

asyncTest( "ingest sync json", function() {
	var path = "/unit_test/beta/test/js/json",
		now  = +new Date();
	Precog.ingest(path,
		"{ \"timestamp\" : "+now+", \"index\" : 1 }\n{ \"timestamp\" : "+now+", \"index\" : 2 }\n{ \"timestamp\" : "+now+", \"index\" : 3 }",
		"json",
		createDelayedAction(function(result) {
			console.log(result);
			Precog.query("ds := /"+path+" ds where ds.timestamp = "+now, function(result) {
				equal(result.length, 3);
				start();
			});
		})
	);
});

asyncTest( "ingest async json", function() {
	var path = "/unit_test/beta/test/js/json",
		now  = +new Date();
	Precog.ingest(path,
		"{ \"timestamp\" : "+now+", \"index\" : 1 }\n{ \"timestamp\" : "+now+", \"index\" : 2 }\n{ \"timestamp\" : "+now+", \"index\" : 3 }",
		"json",
		createDelayedAction(function() {
			Precog.query("ds := /"+path+" ds where ds.timestamp = "+now, function(result) {
				equal(result.length, 3);
				start();
			});
		}),
		null,
		{ async : true }
	);
});

asyncTest( "delete path", function() {
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
				})
			);
		})
	);
});

// **********************
// ***      QUERY     ***
// **********************

asyncTest( "query", function() {
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
		})
	);
});

asyncTest( "query with limit", function() {
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
						null,
						{limit: 1}
					);
				})
			);
		}
	);
});

/*
asyncTest( "basePathPass", function() {
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
				null,
				{basePath: "unit_test/beta/test/"}
			);
		})
	);
});
*/

// **********************
// ***    METADATA    ***
// **********************

asyncTest("retrieve metadata", function() {
	var store = { value : 1 },
		path  = "/unit_test/beta/test/js/metadata/retrieve";
	Precog.store(path, store,
		createDelayedAction(function() {
			Precog.retrieveMetadata(path, function(result) {
				ok(result['children'].length === 0);
				start();
			});
		})
	);
});
