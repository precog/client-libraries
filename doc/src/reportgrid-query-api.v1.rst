==========================
ReportGrid Query API
==========================

.. contents:: :depth: 2

---------------
Overview
---------------

The ReportGrid Query API allows to perform queries on the ReportGrid Analytics Database.

The minimal setup requires the inclusion of the ``reportgrid-core.js`` and ``reportgrid-query.js`` files. If you want to embed the visualizations too you will have to include the ``reportgrid-charts.js`` as described in the Charts API documentation.

::
	
	<!DOCTYPE html>
	<html>
	  <head>
	    <title>MySite.com</title>
	    <script type="text/javascript"
	     src="http://api.reportgrid.com/js/reportgrid-core.js?tokenId={TOKEN_ID}"></script>
	    <script type="text/javascript"
	     src="http://api.reportgrid.com/js/reportgrid-query.js"></script>
	    <script type="text/javascript">
	document.addEventListener("DOMContentLoaded", function(){
		// do something here with the ReportGrid Query API
	}, false);
	    </script>
	  </head>
	  <body>
	  </body>
	</html>

Note that to use the ReportGrid Query API you need an account token id (replace {TOKEN_ID}  with its value).

All the ReportGrid Query API are associated with the ReportGrid.query object. Note that you can combine freely the features from this API with the one described in the Charts Query API. Actually combining the two makes a powerful way of dealing with your data. For example suppose that you want to count the number of occurrances of all the events at some path, using this API alone you will have to make something like this:

::
	
	var path = "/";
	ReportGrid.query
		.count({
			path : path,
			event : "impression"
		})
		.count({
			path : path,
			event : "click"
		})
		.count({
			path : path,
			event : "conversion"
		})

Using this API combined with the Charts Query API you can do instead:

::
	
	var path = "/";
	ReportGrid.query
		.data(["impression", "click", "conversion"])
		.map(function(e) { return { event : e, path : path }; })
		.count()

The result will be the same. What happens is that the ``count`` method uses as parameters for the query the datapoints currently in the stack. If you pass some extra arguments to the function, those arguments will be merged with the datapoints.

The feature described above is very important and can contribute to simplify very complicated queries.

If the token you are using has the ``explore`` option activated the above query can be simplified even more:

::
	
	var path = "/";
	ReportGrid.query
		.events({ path : path })
		.count()

Note how the result of the ``events`` query becomes the argument for the ``count`` query. What happens behind the scenes is that the ``events`` query loads a dataset of datapoints in the following format:

::

	[{ path : "/", event : "impression" }, ...]

For each datapoint in the dataset a new ``count`` query is performed, the stack cleared and the result appended.

--------------------
Methods
--------------------

All of the methods describe below have the same signature. They take two arguments (both optionals) where the first set the parameters for the query and the second is an array of strings of the fields that you want to keep preserved from the parameters into the result. The parameters of the first argument are different for each query (see description belows); they are always optional because the parameters can be inherited from a previous dataset in the stack.

The second argument is used to preserve information in the query chain. Consider the following example:

::
	
	var path = "/";
	ReportGrid.query
		.count({ path : path, event : "impression" })

The result should be something like:

::

	[{ event : "impression", count : 173 }]

As you can see the event is automatically preserved but the path is lost. To preserve the path value in the result just change your query to this:

::
	
	var path = "/";
	ReportGrid.query
		.count({ path : path, event : "impression" }, ["path"])

To be able to use the following queries you must ensure that the token has the ``read`` permission. Some methods also require the ``explore`` permission.
Each method description contains the parameters required for the query and the format of the datapoints loaded by the function. In the return formats, the fields prefixed with ``$`` have variable names that depend on the query context.

--------------------
Explore Methods
--------------------

events
============================
parameters: ``{ path : String }``

datapoint format: ``{ event : String, path : String }``

Requires that the token has the ``explore`` permission.

Returns all the of event names at the specified path.

paths
============================
parameters: ``{ parent : String }``

datapoint format: ``{ parent : String, path : String }``

Requires that the token has the ``explore`` permission.

Returns all the children paths for the specified ``parent`` path.

properties
============================
parameters: ``{ path : String, event : String }``

datapoint format: ``{ event : String, path : String, property : String }``

Requires that the token has the ``explore`` permission.

Returns all the property names for the specified ``event``.






--------------------
Non-Series Methods
--------------------

The scope of the following queries can be optionally restricted by passing a ``start`` and an ``end`` parameters. Both can take a string that represents a date (ex: "yestarday", "3 months ago", "2012-01-10 05:30:25" ...), a timestamp value (numeric) or a Date instance.
It is also possible to restrict the scope of a query to a certain tag; a tag is commonly used for geographical locations but can be used for any hierarchical value. tags must be enabled at the time of tracking to be usable.
All the fields suffixed with a "+" can be repeated more than once in each datapoint.

count
============================
parameters: ``{ path : String, event : String, property : String, value : mixed, start : Mixed, end : mixed, where : Object, tag : String }``

datapoint format for events: ``{ event : String, count : Int, $whereField+ : mixed, ?tag : String }``

datapoint format for values: ``{ property : String, count : Int, ?tag : String }``

Count the number of occurances for the specified event. The count can be performed also on the number of occurrances of a specified ``property``/``value`` pair.

histogram
============================
parameters: ``{ path : String, event : String, property : String, top : Int, bottom : Int, start : Mixed, end : mixed, tag : String, where : Object }``

datapoint format: ``{ count : Int, $property : mixed, $whereField+ : mixed, ?tag : String }``

Returns a histogram of counts for the specified property.

intersect
============================
parameters: ``{ path : String, event : String, start : Mixed, end : mixed, ?properties : Array, tag : String }``

datapoint format: ``{ count : Int, $property+ : mixed, ?tag : String }``

In query arguments the ``properties``object must have the following format: { property : String, ?top : Int, ?bottom : Int }
``top`` and ``bottom`` are alternative and the default value is ``top``: 10.

Intersects the values of each specified property and computes the count of each intersection occurrance.


propertiesHistogram
============================
parameters: ``{ path : String, event : String, property : String, top : Int, bottom : Int, start : Mixed, end : mixed, tag : String }``

datapoint format: ``{ count : Int, $whereField+ : mixed, ?tag : String }``

Similar to the `histogram`_ query it performs the counting on properties that contain value objects.
For example consider the event

::
	
	{ viewed : { keywords : {analytics:true,reports:true} } }

The following query will return a histrogram of the counts of each field in keyowrds:

::
	Reportgrid.query
		.propertiesHistogram({ path : '/', event : 'viewed', property : 'keywords' })

summary
============================
parameters: ``{ path : String, event : String, property : String, String type }``

datapoint format for standard deviation: ``{ standardDeviation : Float }``

datapoint format for mean: ``{ mean : Float }``

Returns one datapoint that stores the absolute mean or standard deviation value.

values
============================
parameters: ``{ path : String, event : String, property : String, start : Mixed, end : mixed }``

datapoint format: ``{ event : String, path : String, property : String, value : Dynamic }``

Returns all the unique values for the specified property.







--------------------
Series Methods
--------------------

Time series methods alway happen between in a defined time range. If the ``start`` and ``end`` are omitted some default values are always assumed. Also the ``periodcity`` is always assumend if not state explicitly. Both ``start`` and ``end`` can take a string that represents a date (ex: "yestarday", "3 months ago", "2012-01-10 05:30:25" ...), a timestamp value (numeric) or a Date instance.
The ``periodicity`` can assume one of the following values: minute, hour, day, week, month or year.
If a ``groupby`` parameter is passed the datapoints will not have a ``time:$periodicity`` field but will have a ``periodicity`` field containing an integer values whose value range varies with the periodicity itself (0 to 59 for minutes, 0 to 23 for hours and so on). The ``groupby`` value must be a valid ``periodicity``value.
Optionally a ``timezone`` value can be passed. The time zone is expressed as a string describing the time shift: "+1", "-0.5" ...
It is also possible to restrict the scope of a query to a certain tag; a tag is commonly used for geographical locations but can be used for any hierarchical value. tags must be enabled at the time of tracking to be usable.
All the fields suffixed with a "+" can be repeated more than once in each datapoint.

intersectSeries
============================
parameters: ``{ path : String, event : String, periodicity : String, start : Mixed, end : mixed, ?properties : Array, tag : String, timezone : mixed, groupby : String }``

datapoint format: ``{ count : Int, $property+ : mixed, ?tag : String }``

datapoint format with timezone: ``{ count : Int, $property+ : mixed, "time:$periodicity" : Int, timezone : String, ?tag : String }``

datapoint format with groupby: ``{ count : Int, $property+ : mixed, $periodicity : Int, groupby : String, ?tag : String }``

In query arguments the ``properties``object must have the following format: { property : String, ?top : Int, ?bottom : Int }
``top`` and ``bottom`` are alternative and the default value is ``top``: 10.

Interesects a set of properties over time. See `intersect`_ for a description of the results of an intersection.

series
============================
parameters: ``{ path : String, event : String, property : String, value : mixed, periodicity : String, start : Mixed, end : mixed, where : Object, tag : String, timezone : mixed, groupby : String }``

with event:

datapoint format: ``{ event : String, count : Int, $whereField+ : mixed, ?tag : String }``

datapoint format with timezone: ``{ event : String, count : Int, $whereField+ : mixed, "time:$periodicity" : Int, timezone : String, ?tag : String }``

datapoint format with groupby: ``{ event : String, count : Int, $whereField+ : mixed, $periodicity : Int, groupby : String, ?tag : String }``


with value:

datapoint format: ``{ property : String, count : Int, $whereField+ : mixed, ?tag : String }``

datapoint format with timezone: ``{ property : String, count : Int, $whereField+ : mixed, "time:$periodicity" : Int, timezone : String, ?tag : String }``

datapoint format with groupby: ``{ property : String, count : Int, $whereField+ : mixed, $periodicity : Int, groupby : String, ?tag : String }``

Return a series of counts over time.

summarySeries
============================
parameters: ``{ path : String, event : String, property : String, String type, tag : String, timezone : mixed, groupby : String }``

datapoint format for standard deviation: ``{ standardDeviation : Float, "time:$periodicity" : Int, ?tag : String }``

datapoint format for mean: ``{ mean : Float, "time:$periodicity" : Int, ?tag : String }``


with timezone:

datapoint format for standard deviation: ``{ standardDeviation : Float, "time:$periodicity" : Int, timezone : String, ?tag : String }``

datapoint format for mean: ``{ mean : Float, "time:$periodicity" : Int, timezone : String, ?tag : String }``


with groupby:

datapoint format for standard deviation: ``{ standardDeviation : Float, $periodicity : Int, groupby : String, ?tag : String }``

datapoint format for mean: ``{ mean : Float, $periodicity : Int, groupby : String, ?tag : String }``

Return a series of values over time for the summary ``type`` specified (``mean`` or ``standardDeviation``).