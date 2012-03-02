===================================
The ReportGrid Charts Query API
===================================

.. contents:: :depth: 2

---------------
Overview
---------------

The Charts Query API is intended to be a utility library to load/handle/transform/enhance datasets. A dataset is considere to be an array of datapoints where a datapoint is usually a JavsSript object (key/value pairs).
Usually the Charts Query API is used to transform a dataset into one that better fits the requirements of our Charts library.

All of the data manipulation start with a reference to ``ReportGrid.query``. For example

::
	
	ReportGrid.query.data([{ num : 1}, { num : 2 }])

or

::
	
	ReportGrid.query.load(ayncloader)

Note that the query is not actually performed until the method ``execute`` is invoked. So a complete example that prints the dataset into the console would read like this:

::
	
	ReportGrid.query
		.load(asyncLoader)
		.execute(function(dataset) { console.log(dataset)})

Note: If you are using the Charts Query API with our Charts API you don't need to call ``execute`` because that method is invoked internally by the visualization.

During the execution of a query a "stack" of datasets is kept internally. This allows more flexible combinations of commands and operations. The methods that explicitely work on the whole stack are prefixed with ``stack``. Even if internally there might be multiple datasets the handler of execute will always receive an array of datapoints (the stack is flattened before ``execute``).

Note that all the methods of ReportGrid.query return a new instance of ReportGrid.query; that means that the most natural way to work with queries is to chain methods:

::

	var query = ReportGrid.query
		.load(asyncLoader)
		.sortValue("name")
		.filterValue("gender", "female")
		.execute(log);

The example above is not the same as:

::
	
	var query = ReportGrid.query;
	query.load(asyncLoader)
	query.sortValue("name")
	query.filterValue("gender", "female")
	query.execute(log);

The second case is creating many instances of a Query object that are not really ever used. Running the above script is equivalent to ``ReportGrid.query.execute(log)`` and the end result is obviously an empty dataset.


---------------
Methods
---------------

load
============================
``.load(Function loader)``

The ``loader`` function is responsible of loading some data asynchrnously and to invoke its argument callback once the data is ready. The loaded data is appended to the current stack.

::
	
	ReportGrid.query
		.load(function(callback) {
			// simulate async loading
			setTimeout(function() {
				callback([{ name : "Franco" }, { name : "John" }]);
			}, 250);
		})

data
===========================
``.data(Array values)``

Appends the passed values to the current stack.

map
===========================
``.map(Function handler)``

Transforms each datapoint in the dataset according to the ``handler`` function. The ``handler`` function takes as argument one datapoint and optionally a ``index`` value (integer starting from zero that resets for each dataset in the stack).

::
	
	ReportGrid.query
		.data([{gender : "male"}, {gender : "female"}])
		.map(function(dp) {
			return { ismale : dp.gender == "male" };
		})

Map can be very handy to tranform primitive values (strings, numbers ...) into an array of JavaScript objects.

::
	
	ReportGrid.query
		.data(["Franco", "John"])
		.map(function(name) {
			return { name : name };
		})

audit
===========================
``.audit(Function handler)``

Performs the ``handler`` action on each datapoint for the dataset. It can be used to monitor the datapoints at a determined points of the query tranformation chain. Note that the return values from ``handler`` is ignored.

::
	
	ReportGrid.query
		.data([{gender : "male"}, {gender : "female"}])
		.audit(function(dataset) {
			console.log(dataset); // logs the values before they are transformed by map
		})
		.map(function(dp) {
			return { ismale : dp.gender == "male" };
		})

renameFields
===========================
``.renameFields(Object fields)``

Maps the field names to new values. Only the mapped fields will be preserved, all the rest will be discarded.

::
	
	ReportGrid.query
		.data([{sex : "male", years : 25, origin : "Italy" }, {sex : "female", years : 26, origin : "Portugal" }])
		.renameFields({
			sex : "gender",
			years : "age"
			// note that origin is discarded with this operation
		})

transform
===========================
``.transform(Function transformer)``

The ``transformer`` function takes an entire dataset as argument (Array of datapoints) and must return a new dataset.

stackCross
===========================
``stackCross()``

???

stackTransform
===========================
``stackTransform(t : StackTransformer)``

???

stackAsync
===========================
``stackAsync(f : AsyncStack)``

???

asyncAll
===========================
``asyncAll(f : Async)``

???

asyncEach
===========================
``asyncEach(f : Dynamic -> (Array<Dynamic> -> Void) -> Void)``

???

setValue
===========================
``setValue(name : String, f : Dynamic)``

???

setValues
===========================
``setValues(o : Dynamic)``

???

mapValue
===========================
``mapValue(name : String, f : Dynamic)``

???

mapValues
===========================
``mapValues(o : Dynamic)``

???

addIndex
===========================
``addIndex(?name : String, ?start : Int)``

???

filter
===========================
``filter(f : Dynamic -> Bool)``

???

filterValues
===========================
``filterValues(f : Dynamic)``

???

filterValue
===========================
``filterValue(name : String, f : Dynamic)``

???

sort
===========================
``sort(f : Dynamic -> Dynamic -> Int)``

???

sortValue
===========================
``sortValue(field : String, ?ascending : Bool)``

???

sortValues
===========================
``sortValues(o : Dynamic)``

???

limit
===========================
``limit(?offset : Int, count : Int)``

???

reverse
===========================
``reverse()``

???

unique
===========================
``unique(?f : Dynamic -> Dynamic -> Bool)``

???

fold
===========================
``fold(startf : Array<Dynamic> -> Array<Dynamic> -> Dynamic, reducef : Dynamic -> Dynamic -> Array<Dynamic> -> Dynamic)``

???

stackMerge
===========================
``stackMerge()``

???

stackDiscard
===========================
``stackDiscard(?howmany : Int)``

???

stackKeep
===========================
``stackKeep(?howmany : Int)``

???

split
===========================
``split(f : Dynamic -> String)``

???

stackRotate
===========================
``stackRotate(?matchingf : Dynamic -> Dynamic -> Bool)``

???

stackReverse
===========================
``stackReverse()``

???

stackStore
===========================
``stackStore(?name : String)``

???

stackRetrieve
===========================
``stackRetrieve(?name : String)``

???

stackClear
===========================
``stackClear()``

???

execute
===========================
``execute(handler : Array<Dynamic> -> Void)``

???




---------------
Use Cases
---------------


- jquery integration