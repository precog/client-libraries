QUnit.config = {
//	autostart : false
};

var email    = "testjs@precog.com",
	password = "123abc";

function ensureAccount(callack) {
	Precog.createAccount(email, password, function(r) {
		Precog.describeAccount(email, password, r['accountId'], function(d) {
			Precog.$.Config.apiKey = d.apiKey;
			callack(r['accountId'], d.apiKey, d.rootPath);
		});
	});
}

function createDelayedAction(f) {
	return function() {
		setTimeout(f, 6000);
	};
}

ensureAccount(function(id, apiKey, rootPath) {

	// **********************
	// ***     ACCOUNT    ***
	// **********************
	asyncTest("describe account", function() {
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

	asyncTest("describe plan", function() {
		Precog.describePlan(email, password, id, function(result) {
			ok(result.type);
			start();
		});
	});

	// **********************
	// ***    SECURITY    ***
	// **********************

	asyncTest("create and delete key", function() {
		var grants = { "grants": [{ "type": "read", "path": rootPath+"foo/", "expirationDate": null, "ownerAccountId" : id }] };
		Precog.createKey(grants,
			function(result) {
				var ak = result['apiKey'];
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

	asyncTest("describe key", function() {
		var grants = { "grants": [{ "type": "read", "path": rootPath+"foo/", "expirationDate": null, "ownerAccountId" : id }] };
		Precog.createKey(grants,
			function(result) {
				var ak = result['apiKey'];
				Precog.describeKey(ak, function(details) {
					equal(details.apiKey, ak);
					ok(details.grants);
					start();
				});
			}
		);
	});

	asyncTest("create and describe new grant", function() {
		var grant = { "type": "read", "path": rootPath+"foo/", "ownerAccountId": id, "expirationDate": null };
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

	asyncTest("delete grant", function() {
		var grant = { "type": "read", "path": rootPath+"foo/", "ownerAccountId": id, "expirationDate": null };
		Precog.createNewGrant(grant, function(grantId) {
			Precog.deleteGrant(grantId, function(result) {
				ok(true);
				start();
			});
		});
	});
// TODO return format is inconsistent
	asyncTest("create child grant and list", function() {
		var grant1 = { "type": "read", "path": rootPath+"foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": rootPath+"foo/bar/", "ownerAccountId": id, "expirationDate": null };
		Precog.createNewGrant(grant1, function(g1) {
			Precog.createChildGrant(g1, grant2, function(g2) {
				Precog.listChildrenGrant(g1, function(result) {
					ok(result instanceof Array);
					equal(result.length, 1);
					equal(result[0].ownerAccountId, id);
					equal(result[0].grantId, g2['grantId']);
					start();
				});
			});
		});
	});

	asyncTest("retrieve grants", function() {
		var grant1 = { "type": "read", "path": rootPath+"foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": rootPath+"foo/bar/", "ownerAccountId": id, "expirationDate": null };
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
/*
	// TODO check test validity
	asyncTest("remove grant", function() {
		var grant1 = { "type": "read", "path": rootPath+"foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": rootPath+"foo/bar/", "ownerAccountId": id, "expirationDate": null };
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
*/

	/*
	  	Precog.addGrantToKey(apiKey, grant, success, failure, options);
	*/
	/*
	asyncTest("list keys", function() {
		Precog.listKeys(
			function(keys) {
				console.log(keys);
				ok(keys instanceof Array);
				start();
			}
		);
	});
	*/
/*
	asyncTest( "query with skip", function() {
		var path = rootPath+"test/js/skip";
		Precog.store(path, "A");
		Precog.store(path, "B",
			createDelayedAction(function() {
				var query = "/"+path;
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
		var path      = rootPath+"test/js/order",
			timeStamp = +new Date(),
			event1    = { num : 1, time : timeStamp },
			event2    = { num : 2, time : timeStamp };
		Precog.store(path, event1);
		Precog.store(path, event2,
			createDelayedAction(function() {
				var query = "data := /"+path+" data where data.time = "+timeStamp;
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
		Precog.store(rootPath+"test/js/store",
			{strTest: "string loaded", numTest: 42},
			function() {
				ok(true);
				start();
			}
		);
	});

	asyncTest( "ingest csv", function() {
		var path = rootPath+"test/js/csv2",
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
		var path = rootPath+"test/js/json",
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
		var path = rootPath+"test/js/json",
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
		var path = rootPath+"test/js/delete";
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
		var path      = rootPath+"test/js/query",
			timeStamp = +new Date(),
			event     = {strTest: "string loaded", numTest: 43, time:timeStamp};
		Precog.store(path,
			event,
			createDelayedAction(function() {
				var query = "data := /"+path+" data where data.time = "+timeStamp;
				Precog.query(query, function(result) {
					deepEqual(result, [event]);
					start();
				});
			})
		);
	});

	asyncTest( "query with limit", function() {
		var path   = rootPath+"test/js/store",
			store1 = {strTest: "string loaded", numTest: 42};
		Precog.store(path, store1,
			function(){
				Precog.store(path, store1,
					createDelayedAction(function(){
						var query = "/"+path;
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
*/
	/*
	asyncTest( "basePathPass", function() {
		var timeStamp = +new Date(),
			event = {strTest: "string loaded", numTest: 42, time:timeStamp};
		Precog.store(rootPath+"test/js/store",
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
/*
	asyncTest("retrieve metadata", function() {
		var store = { value : 1 },
			path  = rootPath+"test/js/metadata/retrieve";
		Precog.store(path, store,
			createDelayedAction(function() {
				Precog.retrieveMetadata(path, function(result) {
					ok(result['children'].length === 0);
					start();
				});
			})
		);
	});
*/
});
