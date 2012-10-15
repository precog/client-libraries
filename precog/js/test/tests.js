QUnit.config = {
//	autostart : false
};

var email    = "test-js2@precog.com",
	password = "1234abc";

function ensureAccount(callack) {
	Precog.cache.disable();
	Precog.createAccount(email, password, function(r) {
		Precog.describeAccount(email, password, r['accountId'], function(d) {
			Precog.$.Config.apiKey = d.apiKey;
			Precog.$.Config.basePath = d.rootPath;

//		Precog.$.Config.apiKey = "A1C62105-691B-4D77-9372-36A693E5D905";
//		Precog.$.Config.basePath = "/0000000024";

//console.log(Precog.$.Config);
			callack(r['accountId'], d.apiKey, d.rootPath);
		});
	});
}

function createDelayedAction(f) {
	return function(r) {
		setTimeout(function() { f(r); }, 6000);
	};
}


//TODO refactor into a single function that takes an optional argument
function isApiKeyInArray(arr, val) { 
	for (i = 0; i < arr.length; i++) 
		if (val == arr[i]["apiKey"]){
			return true; 
		} 
			return false; 
}

function isGrantIdInArray(arr, val) { 
	for (i = 0; i < arr.length; i++) 
		if (val == arr[i]["grantId"]){
			return true; 
		} 
			return false; 
}

function isInArray(arr, val) { 
	for (i = 0; i < arr.length; i++) 
		if (val == arr[i]){
			console.log(val);
			return true; 
		} 
			return false; 
}

ensureAccount(function(id, apiKey, rootPath) {

	// **********************
	// ***     ACCOUNT    ***
	// **********************

	asyncTest("create account", function() {
		var random = Math.floor((Math.random()*1000000)+1); 
		Precog.createAccount("test-js"+random+"@precog.com", password, function(result) {
			equal(result.accountId.length, 10);
			ok(result.accountId);
			start();
		});
	});
	
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

	asyncTest("add grant to account", function() {
		var random = Math.floor((Math.random()*1000000)+1); 
		Precog.createAccount("test-js"+random+"@precog.com", password, function(result) {
			Precog.describeAccount("test-js"+random+"@precog.com", password, result.accountId,
				function(description){
					var accountId = description.accountId;
					var grants = { "grants": [{ "type": "read", "path": rootPath+"foo/", "expirationDate": null, "ownerAccountId" : id }] };
					Precog.createKey(grants,
						function(result) {
							console.log(result);
							var ak = result['apiKey'];
							console.log(ak);
							Precog.describeKey(ak,
								function(description){
									console.log(description);
									console.log(description.grants[0].grantId);
									var grantId = description.grants[0].grantId;
									Precog.addGrantToAccount(email, password, accountId, grantId,
										function() {
											Precog.retrieveGrants(ak, 
												function(grantArray){
													ok(isGrantIdInArray(grantArray, grantId));
													start();
												})
											
									});
								})
								
							}
					);
				})

			
		});
//	});

/*
		Precog.addGrantToAccount(email, password, accountId, grantId function(result) {
			equal(result.accountId, id);
			equal(result.email, email);
			ok(result.accountCreationDate);
			ok(result.apiKey);
			ok(result.rootPath);
			ok(result.plan);
			start();
		});
	*/
	});

	asyncTest("describe plan", function() {
		Precog.describePlan(email, password, id, function(result) {
			ok(result.type);
			start();
		});
	});

	asyncTest("change plan", function() {
		var random = Math.floor((Math.random()*1000000)+1); 
		Precog.changePlan(email, password, id, "bronze"+random, function(result) {
			Precog.describePlan(email, password, id, function(result){
				equal(result.type, "bronze"+random);
				start();
			})
			
		});
	});

	asyncTest("delete plan", function() {
		Precog.deletePlan(email, password, id, function(result) {
			Precog.describePlan(email, password, id, function(result){
				equal(result.type, "Free");
				start();
			})
			
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

	asyncTest("list keys", function() {
		var grants = { "grants": [{ "type": "read", "path": rootPath+"foo/", "expirationDate": null, "ownerAccountId" : id }] };
		Precog.createKey(grants,
			function(result) {
				var ak = result['apiKey'];
				console.log(ak);
				Precog.listKeys(function(details) {
					ok(isApiKeyInArray(details, ak));
					start();
				});
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
		Precog.createGrant(grant, function(g) {
			ok(g.grantId);
			Precog.describeGrant(g.grantId, function(result) {
				equal(result.grantId, g.grantId);
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
		Precog.createGrant(grant, function(g) {
			Precog.deleteGrant(g.grantId, function(result) {
				ok(true);
				start();
			});
		});
	});

	asyncTest("create child grant and list", function() {
		var grant1 = { "type": "read", "path": rootPath+"foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": rootPath+"foo/bar/", "ownerAccountId": id, "expirationDate": null };
		Precog.createGrant(grant1, function(g1) {
			Precog.createGrantChild(g1.grantId, grant2, function(g2) {
				Precog.listGrantChildren(g1.grantId, function(result) {
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
		Precog.createGrant(grant1, function(g1) {
			Precog.createGrantChild(g1.grantId, grant2, function(g2) {
				Precog.retrieveGrants(Precog.$.Config.apiKey, function(result) {
					ok(result instanceof Array);
					ok(result.length > 0);
					start();
				});
			});
		});
	});

	
	asyncTest("remove grant", function() {
		var grant1 = { "type": "read", "path": rootPath+"foo/",     "ownerAccountId": id, "expirationDate": null },
			grant2 = { "type": "read", "path": rootPath+"foo/bar/", "ownerAccountId": id, "expirationDate": null };
			console.log("in remove grant");
		Precog.createGrant(grant1, function(g1) {
			console.log(g1.grantId);
			Precog.createGrantChild(g1.grantId, grant2, function(g2) {
				console.log(g2);
				Precog.removeGrant(Precog.$.Config.apiKey, g2, function(r) {
					Precog.listGrantChildren(g1, function(result) {
						equal(result.length, 0);
						start();
					});
				});
			});
		});
	});


	/*
	  	Precog.addGrantToKey(apiKey, grant, success, failure, options);
	*/

	asyncTest("add grant to key", function() {
		var grants1 = { "grants": [{ "type": "read", "path": rootPath+"foo/", "expirationDate": null, "ownerAccountId" : id }] };
			grants2 = { "grants": [{ "type": "read", "path": rootPath+"foo/bar/", "ownerAccountId": id, "expirationDate": null }] };
			Precog.createKey(grants1,
				function(result) {
					var ak = result['apiKey'];
					Precog.createKey(grants2,
						function(details) {
						var ak2 = (details['apiKey']);
							Precog.describeKey(ak,
								function(description){
									var grantId = description.grants[0].grantId;
									var wrapper = { "grantId": grantId };
								Precog.addGrantToKey(ak2, wrapper,
									function() {
										Precog.describeKey(ak2,
											function(result){
												var grantArray = result.grants;
												console.log(grantArray);
												//console.log(result.grants);
												ok(isGrantIdInArray(grantArray, grantId));
												start();
											}
										)	
									}
								);
								}
							)	
						}	
					);
				}
			);
	});
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

	// **********************
	// ***     INGEST     ***
	// **********************
     
	asyncTest( "store event", function() {
		Precog.store("/test/js/store",
			{strTest: "string loaded", numTest: 42},
			function() {
				ok(true);
				start();
			}
		);
	});

	asyncTest( "ingest csv", function() {
		var path = "/test/js/csv2",
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
		var path = "/test/js/json",
			now  = +new Date();
/*

		Precog.store(path, { name : "franco" },
			createDelayedAction(function(result) {
				console.log("A", result);
				Precog.query("/"+path, function(result) {
					console.log("B", result);
					equal(result.length, 3);
					start();
				});
			})
		);
*/
		
		Precog.ingest(path,
			"{ \"timestamp\" : \""+now+"\", \"index\" : 1 }\n{ \"timestamp\" : \""+now+"\", \"index\" : 2 }\n{ \"timestamp\" : \""+now+"\", \"index\" : 3 }",
			"json",
			createDelayedAction(function() {
			//	console.log(result);
				Precog.query("ds := /"+path+" ds where ds.timestamp = \""+now+"\"", function(result) {
					equal(result.length, 3);
					console.log(result);
					start();
				});
			})
		);
//
	});
	
	asyncTest( "ingest async json", function() {
		var path = "/test/js/json",
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
		var path = "/test/js/delete";
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
			
//			/*
	// **********************
	// ***      QUERY     ***
	// **********************

	asyncTest( "query", function() {
		var path      = "/test/js/query",
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
		var path   = "/test/js/store",
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


	asyncTest( "basePathPass", function() {
		var timeStamp = +new Date(),
			event = {strTest: "string loaded", numTest: 42, time:timeStamp};
		Precog.store("test/js/store",
			event,
			createDelayedAction(function() {
				var query = "data := //js/store data where data.time = "+timeStamp;
				Precog.query(query,
					function(result) {
						console.log(result);
						console.log([event]);
						deepEqual(result, [event]);
						start();
					},
					null,
					{basePath: rootPath+"/test"}
				); 
			})
		);
	});

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
	// ***    METADATA    ***
	// **********************

	asyncTest("retrieve metadata", function() {
		var store = { value : 1 },
			path  = "/test/js/metadata/retrieve";
		Precog.store(path, store,
			createDelayedAction(function() {
				Precog.retrieveMetadata(path, function(result) {
					ok(result['children'].length === 0);
					start();
				});
			})
		);
	});

	asyncTest("children", function() {
		var store = { value : 1 },
		    random = Math.floor((Math.random()*1000000)+1), 
			childPath = "/test/js/metadata/child"+random,
			parentPath = "/test/js/metadata";
		Precog.store(childPath, store,
			createDelayedAction(function() {
				Precog.children(parentPath, function(result) {
					ok(isInArray(result, "/child"+random+"/"));
					start();
				});
			})
		);
	});

});
