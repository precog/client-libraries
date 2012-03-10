===================================
Charts Query API
===================================

.. contents:: :depth: 2

---------------
Overview
---------------

The Charts Query API is intended to be a utility library to load/handle/transform/enhance datasets. A dataset is considered to be an array of datapoints where a datapoint is usually a JavsSript object (key/value pairs).

The Charts Query API is included in both the "reportgrid-query.js" and the "reportgrid-charts.js" files and you can use its functionalities by adding one (or both) to your HTML page. For inclusion details see either the documentation for Charts API or ReportGrid Query API.

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


--------------------
Data Loading Methods
--------------------

The following methods are used to inject new datasets in the current stack. Note that the datasets will be appended to the stack and will not clear what was in there before.

data
===========================
``.data(Array values)``

Appends the passed values to the current stack.

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














--------------------
Dataset Methods
--------------------

The following methods perform some kind of transformation at the dataset level. In practice the transformation is repeated for each dataset in the stack.

addIndex
===========================
``addIndex(?String name, ?Int start)``

Adds a new field to each datapoint with an associated index. The default name (if not set for ``name``) is `ìndex`` and the default starting value is ``1``.

Note that indexes are reset for each dataset in the stack. So if you plan to have a unique value for each datapoint you should `stackMerge`_ your stack first.

::
	
	ReportGrid.query
		.load(asyncLoader)
		.addIndex() // add ``index : 1`` to the first datapoint, ``index : 2`` to the second and so on.
		.addIndex("index0", 0) // add a new ``index0`` field that starts from 0.

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

console
===========================
``console()``

Display the current state of the stack in the console when available.

filter
===========================
``filter(Function filterFunction)``

Each datapoint in the datasets is passed to the ``filterFunction``. If that function returns ``true`` the value is preserved in the stack, otherwise it will be removed.

::
	
	ReportGrid.query
		.load(asyncLoader)
		.filter(function(dp) {
			return dp.age > 21 && dp.gender == "female";
		})

filterValue
===========================
``filterValue(String fieldName, mixed filterValue)``

Filter the datapoints based on the value of ``fieldName``. ``filterValue`` can be either a function taking the current value for ``fieldName`` as the argument or a static value.

::
	
	ReportGrid.query
		.load(asyncLoader)
		.filterValue("age", function(v) { return v > 21})
		.filterValue("gender", "female")

filterValues
===========================
``filterValues(Object filterObject)``

Works like `filterValue`_ but on multiple fields at once.

::
	
	ReportGrid.query
		.load(asyncLoader)
		.filterValues({
			age : function(v) { return v > 21},
			gender : "female"
		})

limit
===========================
``limit(?Int offset, Int count)``

Removes from the dataset the elements before ``offset`` (default is 0) and after ``offset + limit``.

In this example only the first 5 datapoints are preserved:

::
	
	ReportGrid.query
		.load(asyncLoader)
		.limit(5)

In this example only the 5 datapoints after the first 10 are preserved:

::
	
	ReportGrid.query
		.load(asyncLoader)
		.limit(10, 5)

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

Map can be very handy to tranform primitive values (strings, numbers ...) into datasets of JavaScript objects.

::
	
	ReportGrid.query
		.data(["Franco", "John"])
		.map(function(name) {
			return { name : name };
		})

mapValue
===========================
``mapValue(String name, mixed f)``

Similar to `setValue`_ but the function that generates the values take the current value of the field as argument.

::
	
	ReportGrid.query
		.data([{ value : 8 }])
		.mapValue("value", function(v) { return v * v })

mapValues
===========================
``mapValues(Object o)``

Applies a transformation function to each field specified in the argument object. See also `setValue`_, `setValues`_ and `mapValue`_.

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

reverse
===========================
``reverse()``

Reverses the sequence of the datapoints in each dataset in the stack.

setValue
===========================
``setValue(String name, mixed f)``

Adds or changes the value of the field ``name``. The second argument can be either a function that takes the entire datapoint as argument or a static value.

::
	
	ReportGrid.query
		.data([{ width : 10, height : 20 }])
		.setValue("area", function(dp){ return dp.width * dp.height; })
		.setValue("geom", "rectangle")

setValues
===========================
``setValues(Object o)``

Works much as `setValue`_ but instead of working on a single key/value pair it works on a set of key/values pairs. The pairs are passed in one JavaScript object.

::
	
	ReportGrid.query
		.data([{ width : 10, height : 20 }])
		.setValues({
			area : function(dp){ return dp.width * dp.height; }),
			geom : "rectangle"
		})

sort
===========================
``sort(Function sortFunction)``

Reorders the datapoints in a dataset according to ``sortFunction``. The function must return an integer value used for comparison.

::
	
	ReportGrid.query
		.load(asyncLoader)
		.sort(function(a, b) {
			if(a.gender != b.gender)
				return a.gender == 'male' ? -1 : 1;
			return a.age - b.age;
		})

sortValue
===========================
``sortValue(String fieldName, ?Boolean ascending)``

Reorders the dataset according to the values of the property ``fieldName``. The second argument states if the order should be ascending (default) or not.

::
	
	ReportGrid.query
		.load(asyncLoader)
		.sortValue("gender")
		.sortValue("age")

Note that in the example above the result might be different than using `sort`_ or `sortValues`_.

sortValues
===========================
``sortValues(Object objectSort)``

Works much like as `sortValue`_ but applying more than one comparison at once. Note that the values for ``objectSort`` are boolean values that determine the direction of the sorting for each property.

::
	
	ReportGrid.query
		.load(asyncLoader)
		.sortValues({
			gender : true,
			age : trur
		})

split
===========================
``split(mixed splitArgument)``

Splits the datasets in the stack into multiple datasets according to ``splitArgument``. ``splitArgument`` can be either a field name (split by value) or a function that takes one datapoint at the time and assign it to a bucket identified by the return value.

::
	
	ReportGrid.query
		.load(asyncLoader) // after the load, the stack contains one dataset
		.split("gender")   // the split creates one dataset for each value of "gender"

unique
===========================
``unique(?Function uniqueFunction)``

Removes duplicates from the datasets. If ``uniqueFunction`` is passed than it will be used to determine if two datapoins are equal, otherwise each datapoint will be structurally compared analyzing the value of each field recursively.

This operation is computationally expensive so use it with care, particularly if ``uniqueFunction`` is not provided.

::
	
	ReportGrid.query
		.data([{name:"Franco"},{name:"John"},{name:"Franco"}])
		.unique() // the result is [{name:"Franco"},{name:"Franco"}]








--------------------
Execution Method
--------------------

The only execution method is ``execute``. Note that if you are using the Query Charts API to generate data for a chart, you don't need to call this method because it is handled automatically by the visualization. Executing the method manually will generate an execution error.

execute
===========================
``execute(Array callback)``

Performs the query chain and sends the result to the ``callback`` function.

::
	
	ReportGrid.query
		.load(asyncLoad)
		.execute(function(dataset) {
			console.log("loaded " + dataset.length + " items");
		})












--------------------
Stack Methods
--------------------

The following methods act on the stack as a whole.

stackCross
===========================
``stackCross()``

Performs a cross operation an all the datapoints of all the datasets currently in the stack.

::
	
	ReportGrid.query
		.data([{ name : "Franco" }, { name : "John" }]) // first dataset
		.data([{ group : "A" }, { group : "B" }]) // second dataset
		.stackCross()
		// produces: [
		//   { name : "Franco", group : "A" },
		//   { name : "Franco", group : "B" },
		//   { name : "John", group : "A" },
		//   { name : "John", group : "B" }
		// ]

stackClear
===========================
``stackClear()``

Removes all the datasets from the stack.

stackDiscard
===========================
``stackDiscard(?howmany : Int)``

Removes the last ``howmany`` (default is 1) datasets from the stack.

stackKeep
===========================
``stackKeep(?howmany : Int)``

Removes the datasets in the stack after ``howmany`` (default is 1).

stackMerge
===========================
``stackMerge()``

Merges multiples datasets in the stack in one dataset.

stackReverse
===========================
``stackReverse()``

Reverses the order of the datasets in the stack.

stackRetrieve
===========================
``stackRetrieve(?String name)``

Retrieves and appends tha data stored through ``stackStore`` at the end of the current stack.

stackRotate
===========================
``stackRotate(?Function matchingFunction)``

Rotates the datasets in the stack. The rotation is performed on the position of each datapoint in the datasets if the ``matchingFunction`` is not provided. The ``matchingFunction`` takes two datapoints from two different datasets, the result must be a boolean that states if the 2 datapoints should be moved to the same dataset.

stackSortValue
===========================
``stackSortValue(String fieldName, Bool ascending)``

Sums all the values of fieldName for each datapoint in the dataset and use that value to compare the datasets in the stack.

stackStore
===========================
``stackStore(?String name)``

Puts the current stack into a reserved meomory space for later retrieval with ``stackRetrieve``. You can optionally associate a ``name`` to the stored data.















--------------------
Low Level Methods
--------------------

The methods below are used a lot internally and are exposed because can cover usages that are not possible using the methods decribed above. These methods require probably a deeper knowledge of JavaScript and more code writing.

asyncAll
===========================
``asyncAll(Function asyncTransformer)``

Transforms asynchronously each dataset. The ``asyncTransformer`` is a function that takes a handler function that takes an array of datapoints as argument.

asyncEach
===========================
``asyncEach(Function asyncTransformer)``

Transforms asynchronously each datapoint in a dataset. The ``asyncTransformer`` is a function that takes a handler function that takes one datapoint as argument.

fold
===========================
``fold(mixed start, Function reduceFunction : Dynamic -> Dynamic -> Array<Dynamic> -> Dynamic)``

The ``fold`` can be used to reduce a dataset of values to a new dataset or to add cumulative values to the datapoints. It takes two arguments, the first one can be either a static value or a function:

``startFunction(?Array dataset, ?Array newDataset) mixed``
The function takes the current dataset and a new empty dataset as argument. It must return a value that is used as a base value for the ``reduceFunction``.

If a static value is provided, that value will be used as base.

``reduceFunction(mixed base, mixed datapoint, ?Array newDataset) mixed``

The ``reduceFunction`` is invoked once for each datapoint in the dataset. The first argument is the base value, the second is the current datapoint and the third is the new dataset that will replace the original dataset in the stack. The function must return a new value that replaces the value of base on further interactions of ``reduceFunction``.

transform
===========================
``.transform(Function transformer)``

The ``transformer`` function takes an entire dataset as argument (Array of datapoints) and must return a new dataset.

stackAsync
===========================
``stackAsync(Function asyncTransformer)``

Much like ``stackTransform`` but instead of returning the new stack, the ``asyncTransformer`` will use the ``handler`` function passed as argument to send the data to the stack. It is useful if the stack transformation happens asynchronously.

stackSort
===========================
``stackSort(Function sortFunction)``

Reorders the sequence of the datasets in the stack.

stackTransform
===========================
``stackTransform(Function transformer)``

The "transformer" function takes the entire stack (array of array of datapoints) and should return a new transformed stack.