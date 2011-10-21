===================================
The ReportGrid JS Visualization API
===================================

.. contents:: :depth: 2

---------------
Overview
---------------

ReportGrid JavaScript visualizations are HTML5 widgets that you can embed in your web pages. First of all you have to be sure to include the two required javascript files.

The files are reportgrid-core.js and reportgrid-viz.js. The first requires a parameter for your token id while the latter is a simple inclusion.

::
	
	<!DOCTYPE html>
	<html>
	  <head>
	    <title>MySite.com</title>
	    <script type="text/javascript"
	     src="http://api.reportgrid.com/js/reportgrid-core.js?tokenId={YOUR_TOKEN_ID}"></script>
	    <script type="text/javascript"
	     src="http://api.reportgrid.com/js/reportgrid-viz.js"></script>
	    <script type="text/javascript">
	document.addEventListener("DOMContentLoaded", function(){
	  ReportGrid.lineChart("#test", { path : "/acme/", event : "impression", property : "browser" });
	}, false);
	    </script>
	  </head>
	  <body>
	  <div id="chart"></div>
	  </body>
	</html> 

The charts are not styled by default and you will have to provide your color schemes and styles using a standard CSS file. If you want you can use our default stylesheet including the following line of code inside your <head> element.

Note that in this document whenever you encounter a string enclosed in curly brackets ``{value}``, it means that the entire string must be replaced with a customer value (e.g. ``gradient-{value}`` means that ``gradient-0.75`` and ``gradient-1.25`` are both valid values). 

::
	
	<link rel="stylesheet" type="text/css" href="http://api.reportgrid.com/css/rg.css"/>

You can change the color schemes of the visualization loading one of our optional `css palettes`_. The palettes CSS files are located here http://api.reportgrid.com/css/colors/

Every visualization is a method of the object ``ReportGrid`` and every visualization takes an element placeholder as the first argument. That argument can be a DOM element or a CSS selector (string); usually you will want to use the ID selector of an existing DIV in your page. In the example above the "#chart" selector refers to the <div id="chart"></div> node in the DOM.

The second argument is always an object that contains all the info required to make the visualization render.

There are two way to configure that object, a `Simplified Query Model`_ and a `Complete Query Model`_; they both share a common ``options`` field that works the same in both context.

-----------------------
Simplified Query Model
-----------------------

In the simplified query model you have the options to quickly build a big set of visualizations in a really easy way.
The simplified query model always tries to fill the blanks for you. 
You can have one of the following combinations of parameters:

 * path: the query will retrieve all the count of all of the events at the path
 * path + event/events: the query will retrieve the count of all the events at the path
 * path + event + property: the query will retrieve the count of all the values for the specified property

If path is not specified the root path "/" is always assumed.

The queries can be restricted to a certain time span by specifying the ``start`` and ``end`` parameters. Those parameters must always be specified in pair. When a time series is produced, the system will always fill those parameters automatically when they are not passed.

For a better insight on how the data are queried and retrieved, take a look at the `Complete Query System`_

**options:**

event : string
	The event name.
events : string OR array string
	One or more (array of values) event names.
end : timestamp
	The end of the time span
	The ``start`` and ``end`` parameter must be specified as a timestamp (number), Date or a parsable date string.
path : string
	The path where the events are stored. The path must always begin with a slash ``/``. The ending slash is optional.
	Note that any path is a valid value even if no events are stored there. In case the path does not exist because there are no events stored yet, the server will response with an empty set of data.
periodicity : string
	Periodicity can be any of the following values: ``minute``, ``hour``, ``day``, ``week``, ``month``, ``year`` or ``eternity``
	The granularity is chosen automatically based on the start/end range if not specified and based on the type of visualization.
	So if you pick a ``piechart`` the ``periodicity`` will be automatically set to ``eternity`` but not for a ``linechart``.
	Note that if you pick a small granularity with an extended time span you can get a really huge amount of data back that besides slowing down your visualizations will probably not convey any meaningfull visualization.
property : string
	The property name whose values you want to visualize.
start : timestamp
	The beginning of the time span.
	The ``start`` and ``end`` paramater must be specified as a timestamp (number), Date or a parsable date string.

-----------------------
Visualization Methods
-----------------------

ReportGrid.barChart
===================

``ReportGrid.barChart(selector el, parameters object) void``

Conforming the queried data, for each tick in the X axis might exist one or more bar. The bars can belong to the same Y axis or to different ones. If they belong to the same Y axis the data can be segmented to produce several bars. The bars segmented on the same Y axis can be stacked or not.

**options:**

``barpadding`` : float
	Padding distance in pixel between groups of bars for the same X tick.
``barpaddingaxis`` : float
	Padding distance in pixel between bars when grouped on different Y axis.
``barpaddingdatapoint`` : float
	Padding distance in pixel between bars when lined horizontally.
``effect`` : string ("noeffect", "gradient", "gradient-{value}")
	The effect to apply to the bars.
	 * ``noeffect`` :
		simply uses a solid fill color
	 * ``gradient`` :
	 	applies a gradient
	 * ``gradient-{value}`` :
	 	applies a gradient from the color in the stylesheet darkened or lightned by the value. If the value is 1.0 there will be no gradient at all, use bigger or lower values to make the gradient lighter or darker.
``stacked`` : bool
	Determines if the bars are stacked on top of each other or lined horizontally.

.. include:: visualization-api-v1-options-cartesian.txt
.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt

.. include:: visualization-api-v1-options-label.txt
.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.funnelChart
======================

``ReportGrid.funnelChart(selector el, parameters object) void``

The funnel chart is an extremely insightful visualization to quickly grasp ROI-like information. The funnel starts big for a certain variable and reduces for some subordinate value.

**options:**

``arrowsize`` : float
	The size of the funnel arrow side in pixels.
``click`` : function(object datapoint_, object stats_) void
	A handler function that is executed when the user click or touches a datapoint_.
``effect`` : string ("noeffect", "gradient", "gradient-{value}")
	The effect to apply to the funnel sections.
	* ``noeffect`` simply uses a solid fill color
	* ``gradient`` applies a gradient
	* ``gradient-{value}`` : applies a gradient from the color in the stylesheet darkened or lightned by the value. If the value is 1.0 there will be no gradient at all, use bigger or lower values to make the gradient lighter or darker.
``flatness`` : float
	A value to accentuate or reduce the 3D effect of the chart. The default value is 1. A value near to 0.0 will make the funnel appear almost completely flat.
``label`` : object labeloptions
	Conveys information over `labelling the funnel sections`_.
``segmentpadding`` : float
	Distance in pixel (white space) between the sections of the funnel chart.
``sort`` : function(object a, object b) int
	A custom function to order the datapoints_ before rendering them.

.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt

.. _`labelling the funnel sections`:

.. include:: visualization-api-v1-options-label.txt

``arrow`` : function(object datapoint_, object stats_) string
	A function to generate a custom label to put over the section arrows. If the returned value is ``null`` the arrow will not be displayed.




ReportGrid.geo
===================

``ReportGrid.geo(selector el, parameters object) void``

The geo visualization can be used to produce choropleth charts or point/area diagrams. Each geo visualization can overlay more than one geographic layer associated or not to the datapoints retrieved by the query. This gives you the option to load geographic features to convey data information and/or to decorate the visualization.
Note that the data contained in the geographic features (if any) is merged in the datapoint_ when the visualization is rendered. That metadata will be contained in the ``#data`` field. In the same way also the centroids in pixel of the geometries is injected in the datapoint_ in the ``#centroid`` field.

**options:**

label : object label options
	Configuration object for text labels. The values passed to this object are used as defaults for the map object(s).
map : object/array map options
	A single map definition or an array of `Map Options`_.

Map Options
-----------

**options:**

classname : string
	An optional class name to associate with the geometries container. The ``classname`` may be used for styling purposes.
click : function(object datapoint_, object stats_) void
	A handler function that is executed when the user click or touches a datapoint_.
color : string OR function(datapoint_, stats_) string
	This parameter determins how the geometries are colored. The default value is ``css`` but the field accept any of the following:
	 * ``"css"`` or ``"css-{int}"`` :
	 	It uses the colors in the stylesheet to colour the geometries. The colors in the css palette are automatically detected and the color scale is divided proportionally to associate the values in the axis with each color in the palette. You can reduce the number of values used by specifying an integer number after the dash: ``"css-5"`` will only use the first 5 colors in the associated CSS stylesheet.
	 * ``function()`` :
		a custom function whose return value must be a valid CSS color string.
	 * ``"i-{color1},{color2},..."`` or ``"interpolated-{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built on the interpolation between those colors.
	 * ``"s-{color1},{color2},..."`` or ``"sequence-{color1},{color2},..."`` or ``"{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built to match exactly the colors in the list.
	 * ``"f-{color}"`` or ``"fixed-{color}"`` or ``"{color}"`` :
		Use this option to colour all the geometries with the same color.
label : object label options
	Configuration object for text labels.
mode : string
	The projection mode for the ``azimuthal`` projection. The value can be either ``orthographic`` or ``stereographic``.
origin : array of float
	Moves the origin of the projection (available for the ``albers`` and ``azimuthal`` projections).
parallels : array of float
	Redefines the parallels in the ``albers`` projection.
projection : string
	The kind of projection to use to render the geographic features. The available values are: ``mercator``, ``albers``, ``albersusa`` and ``azimuthal``
property : null OR string
	The property field in the datapoint_ associated to the feature ID in the geometric layer. The default value is ``#location``. If the property is set to ``null`` the geographic layer will be rendered but not associated to the data.
radius : float OR function(datapoint_, stats_) float
	If the geometry contains Point geometries they are rendered as ``svg:circle`` elements whose radius can be a fixed value or calculated using a custom function.
scale : float
	A scale factor to reduce or enlarge the geographic visualization.
template : string
	To simplify geo consumption some premade templates and geographic files are made available by the ReportGrid API.
	The available templates are:
	* ``"world"`` :
	* ``"usa-states"`` :
	* ``"usa-state-centroids"`` :
	* ``"usa-counties"`` :
translate : array of float
	Array of 2 values in pixel to pan the visualization from its center.
type : string
	The format of the geographic file addressed in ``url``. So far the only supported format is the default one ``geojson``.
url : string
	The url of the file containing the geometries to display.

.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt

.. include:: visualization-api-v1-options-label.txt

ReportGrid.heatGrid
===================

``ReportGrid.heatGrid(selector el, parameters object) void``

The heatgrid visualization divides a two dimensional space into a grid whose columns and rows are associated to the first two axis in your query. Each cell of the grid is colored according to the third axis.

**options:**

color : string OR function(datapoint_, stats_) string
	This parameter determins how the geometries are colored. The default value is ``css`` but the field accept any of the following:
	 * ``"css"`` or ``"css-{int}"`` :
		It uses the colors in the stylesheet to colour the geometries. The colors in the css palette are automatically detected and the color scale is divided proportionally to associate the values in the axis with each color in the palette. You can reduce the number of values used by specifying an integer number after the dash: ``"css-5"`` will only use the first 5 colors in the associated CSS stylesheet.
	 * ``function()`` :
		a custom function whose return value must be a valid CSS color string.
	 * ``"i-{color1},{color2},..."`` or ``"interpolated-{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built on the interpolation between those colors.
	 * ``"s-{color1},{color2},..."`` or ``"sequence-{color1},{color2},..."`` or ``"{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built to match exactly the colors in the list.
	 * ``"f-{color}"`` or ``"fixed-{color}"`` or ``"{color}"`` :
		Use this option to colour all the geometries with the same color.

.. include:: visualization-api-v1-options-cartesian.txt
.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt

.. include:: visualization-api-v1-options-label.txt
.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.leaderBoard
======================

``ReportGrid.leaderBoard(selector el, parameters object) void``

This visualization renders a list of values associated to the datapoints produced by your query.

**options:**

animation : object animation options
	Defines the `animation behavior`_ of the visualization.
click : function(object datapoint_, object stats_) void
	A handler function that is executed when the user click or touches a datapoint_.
effect : string
	States the effect to apply to the list items in the leaderboard.
	 * ``"noeffect"`` : 
		No gradient is applied below the item.
	 * ``"gradient"`` : 
		Applies a gradient below the item that is proportional to the sum of the values in the datapoint_ set.
	 * ``"gradient-max"`` : 
		Same as above but the gradient is proportional to the biggest value in the datapoint_ set.
label : object label options
	Configuration object for text labels.
sort : function(object datapoint_, object datapoint_) int
	Sort function to rearrange the orders of the items in the leaderboard list.

.. _`animation behavior`:

.. include:: visualization-api-v1-options-animation.txt
	

ReportGrid.lineChart
====================

``ReportGrid.lineChart(selector el, parameters object) void``

The ``lineChart`` can be used to build standard line charts, area charts stacked or not.

**options:**

displayarea : bool
	States if an area shape must be rendered below the line chart or not.
effect : string
	States the effect to apply to the line(s) of the chart. The parameters in curly brackets are optional and refine the style.
	 * "noeffect" : 
		The line is rendered with a solid color.
	 * "dropshadow" :
		The line is rendered with a background shadow.
	 * "dropshadow-{offsetx}" :
	 * "dropshadow-{offsetx}-{offsety}" :
	 * "dropshadow-{offsetx}-{offsety}-{levels}" :
		The optional parameters are used to set the offset (x and y) of the shadow and of how many degrees of gray the shadow is composed.
	 * "gradient" :
		The line is rendered with a gradient from the middle outwards.
	 * "gradient-{lightness}" : 
	 * "gradient-{lightness}-{levels}" : 
		The ``lightness`` parameter is used to state how brigther (or darker) the end of the gradient will be and the ``levels`` parameterd states the number of steps that form the gradient.
symbol : string OR function(object datapoint_, object stats_) string
	Each datapoint_ in the line charts can be associated to an optional symbol. The symbol can be described statically using a string or using a function. The symbol must be expressed in SVG PATH format. There is a practical function ``ReportGrid.symbol.get()`` to quickly build symbols.
symbolstyle : function(object datapoint_, object stats_) string
	If symbols are added to the lines you can use ``symbolstyle`` to render a custom style for each of them. The style produced by the custom function must be a string in CSS format.
y0property : string
	This parameter is used to determine what value in the datapoint_ must be used to stack values.
	Usually that value must be calculated transforming the datapoints from your query.
interpolation : string
	Linecharts are not interpolated by default (equivalent to "linear" interpolation) but can be smoothed or transformed using one of the following values: ``"basis"``, ``"basisopen"``, ``"basisclosed"``, ``"cardinal"``, ``"cardinal-{value}"``, ``"cardinalopen"``, ``"cardinalopen-{value}"``, ``"cardinalclosed"``, ``"cardinalclosed-{value}"``, ``"monotone"``, ``"stepafter"``, ``"stepbefore"``, ``"linear"``
	The ``value`` parameter is an optinal ``float`` value for ``cardinal`` interpolations.
``segment`` : object segmentoptions
	An object that describes the options for segmenting the data (see below).
``segmenton`` : string
	A shortcut that is equivalent to : ``{ segment : { on : "propertyname" } }``

.. include:: visualization-api-v1-options-cartesian.txt
.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt
.. include:: visualization-api-v1-options-segment.txt

.. include:: visualization-api-v1-options-label.txt
.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.pieChart
===================

``ReportGrid.pieChart(selector el, parameters object) void``

The ``pieChart`` is obviously used to render pie charts, but it can also be used to render donut charts.
All of the ``radius`` parameters below should be expressed as a ``float`` value between 0 and 1 where 0 is at the center of the pie and 1 is at the edge of the available chart space.

**options:**

animation : object animation options
	Defines the `piechart animation behavior`_ of the visualization.
click : function(object datapoint_, object stats_) void
	A handler function that is executed when the user click or touches a datapoint_.
dontfliplabel : bool
	Determines if the labels on the datapoints should always be set so that even if they are inclined they never end flipped.
effect : string
	The effect to apply to each slice in a pie or donut chart.
	 * ``"noeffect"`` : 
		The pie slices are filled with a solid color.
	 * ``"gradient"`` : 
		Applies a radial gradient.
	 * ``"gradient-{lightness}"`` : 
		Same effect as above but with the control over the intensity of the color variation.
innerradius : float
	Inner radius is zero by default but can be any value between 0 and 1 to build a donut chart.
label : object label options
	Configuration object for text labels.
labelorientation : string
	Label orientation is used to control the behavior of label rotation according to their position in the chart.
	 * ``"fixed"`` or ``"horizontal"``:
		No rotation.
	 * ``"fixed-{angle}"`` :
		Rotation fixed at a certain angle.
	 * ``"ortho"`` or ``"orthogonal"`` : 
		Labels are aligned orthogonally to their radius.
	 * ``"align"`` or ``"aligned"`` : 
		Labels have the same angle as their radius have.
labelradius : float
	States where the datapoint_ labels are positioned.
outerradius : float
	The outer radius is useful when you want accentuated transitions (animations) that can bleed the border of the charts. Another use in combination with ``innerradius`` is to nest two or more donut charts in the same visualization and avoid overlappings.
overradius : float
	The over radius is used to control the slice size when animation is turned on.
tooltipradius : float
	Controls the position of the tooltip balloon when activated and the mouse is positioned over a slice.
sort : function(object datapoint_, object datapoint_) int
	Sort function to rearrange the orders of the slices in the pie chart.

.. include:: visualization-api-v1-options-layout.txt

.. include:: visualization-api-v1-options-padding.txt

.. _`piechart animation behavior`:

.. include:: visualization-api-v1-options-animation.txt

ReportGrid.pivotTable
=====================

``ReportGrid.pivotTable(selector el, parameters object) void``

A pivot table is a data summarization table. It can be created on any number of dimensions (axis) where the dimensions are grouped on columns or rows.

**options:**

click : function(object datapoint_, object stats_) void
	A handler function that is executed when the user click or touches a datapoint_.
columnaxes : int
	This parameter controls how many dimensions of the query are grouped as columns.
	If you have three axis and you want to group just the first on the coloumns and the remaining two on the rows, the ``columnaxes`` value must set to ``1`` (default value). The same visualization can have 2axes on the columns and 1 axis on the rows, setting the value to 1.
displaycolumntotal : bool
	Whether or not to display the totals at the bottom of the columns.
displayheatmap : bool
	Whether or not to display a background color below each cell whose intensity in the color scale is based on the value of the cell itself.
displayrowtotal : bool
	Whether or not to display the totals at the right of the rows.
endcolor : string
	Color for the background cell of highest value in the table. This option is used only if ``displayheatmap`` is set to true.
label : object pivottable label options
	Configuration object for text labels.
startcolor : string
	Color for the background cell with a value of zero. This option is used only if ``displayheatmap`` is set to true.


.. include:: visualization-api-v1-options-label.txt

axisvalue : function(any value, string type) string
	The label to apply to the individual values of the grouped dimensions..
total : function(float value, object stats_) string
	Function to control the label in the total cell.
totalover : function(float value, object stats_) string
	Function to control the label when the mouse goes over a total cell.

.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.scatterGraph
=======================

``ReportGrid.scatterGraph(selector el, parameters object) void``

A scatter graph is a visualization to display data for two variables in a data set (more variables can be addressed using size and colors). The data is displayed is displayed as a collection of points or symbols.

**options:**

symbol : string OR function(object datapoint_, object stats_) : string
	Each point in a scatter graph should be associated to a symbol. Each symbol can be rendered indivisually to have a distinct shape or not. The ``symbol`` can be a static string or a function that returns such string. The string represents a SVG path. You can easily create a SVG path using `ReportGrid.symbol.get`_.
symbolstyle : string OR function(object datapoint_, object stats_) : string
	As much as you can control the sy,bol shape, you can control its style returning a custom style string. The style must be expressed in the CSS format.
``segment`` : object segmentoptions
	An object that describes the options for segmenting the data (see below).
``segmenton`` : string
	A shortcut that is equivalent to : ``{ segment : { on : "propertyname" } }``

.. include:: visualization-api-v1-options-cartesian.txt
.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt
.. include:: visualization-api-v1-options-segment.txt

.. include:: visualization-api-v1-options-label.txt
.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.streamGraph
======================

``ReportGrid.streamGraph(selector el, parameters object) void``

A Streamgraph layout emphasizes legibility of individual layers, arranging the layers in a distinctively organic form.

**options:**

effect : string
	The effect to apply to each slice in a pie or donut chart.
	 * ``"noeffect"`` : 
		No effect is applied to the stream bands and they are filled with a solid color.
	 * ``"gradient"`` : 
		A simple linear and vertical gradient that fills the stream band.
	 * ``"gradient-{lightness}"`` : 
		Same as above but with control over the lightness or darkness of the stop color in the gradient.
	 * ``"gradienth"`` : 
		The band are filled with a complex horizontal and linear gradient that enhance or reduce the color based on the values in the graph.
	 * ``"gradienth-{lightness}"`` : 
		Same as above but with control over the lightness or darkness of the stop color in the gradient.
interpolation : string
	Controls how the lines in the stream graph are interoilated, the allowed values are: "basis", "basisopen", "basisclosed", "cardinal", "cardinalopen", "cardinalclosed", "cardinal-{value}", "cardinalopen-{value}", "cardinalclosed-{value}", "monotone", "stepafter", "stepbefore", "linear".
	The ``{value}`` parameter for certain interpolation values should be replaced with a float value.
``segment`` : object segmentoptions
	An object that describes the options for segmenting the data (see below).
``segmenton`` : string
	A shortcut that is equivalent to : ``{ segment : { on : "propertyname" } }``

.. include:: visualization-api-v1-options-cartesian.txt
.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt
.. include:: visualization-api-v1-options-segment.txt

.. include:: visualization-api-v1-options-label.txt
.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.viz
==============

``ReportGrid.viz(selector el, parameters object, string type) void``

The ``ReportGrid.viz`` method is a generic function to build a visualization programmatically (passing the type of visualization as the third parameter). The ``type`` argument can take any of the names of the methods described above all lower case.

The following two declarations are basically equivalent:

::

	ReportGrid.lineChart("#chart", { ...});
	
	ReportGrid.viz("#chart", { ...}, "linechart");

.. _`Complete Query System`:

-----------------------
Complete Query Model
-----------------------

Visualizations cannot be built without data. The data can be the result of a query to the ReportGrid REST API or some values provided by the user. The Complete Query Model provides a way to customize every aspect of the data collections.

The following example renders the same chart as in the first example of this document but using the Complete Query Model instead of the `Simplified Query Model`_:

::

	ReportGrid.lineChart("#test", {
	  axes : [{
	    type : ".#time:hour"	
	  }, {
	    type : "count"
	  }],
	  data : {
	    src : [{
	      path : "/acme/",
	      event : "impression",
	      query : ".browser * .#time:hour"
	    }]
	  }
	});

Data
===============

**options:**

name : string
	An optional indetifier to be able to reference the dataset by name.
scale : function(array datapoint_) array datapoint_
	Takes the entire datapoint_ dataset and return a transformed version of it. This function is generally used to trim datapoints, to enhance them or to map/reduce to a different dataset.
src : source options OR array source options
	The `Data Source`_ where the data are collected from.
transform : function(array datapoint_, ...) array datapoint_
	A function to transform multiple sets of datapoints_ into a uniquer set of datapoints_.

Data Source
===============

The ReportGrid Visualization API provides two kinds of data source, the ReportGrid Data and the Custom Data. The first is a way to access the data stored in the ReportGrid Analytic Database, the latter is a way to use custom (as in user produced or loaded) data.

ReportGrid Data
---------------

When accessing the ReportGrid Analytics Database using the Visualization API, query can be performed on events and properties. The ``query`` parameter can be used to cross properties and obtain richer datasets. Usually a query is a cross operation on one or more properties:

* ``.#time:hour`` :
	Produces a set of datapoint with the count value of each event (as stated in the ``event`` parameter) and splitted by hour
* ``.browser * .#time:hour`` : 
	Similar to before but each datapoint produced contains also a field ``.browser`` whose value will be the browser type tracked in the database.
* ``.ageRange * .gender * .#time:day`` A three dimensional cross product over age range, gender and time.

**options:**

end : timestamp
	The end of the time span
	The ``start`` and ``end`` paramater must be specified as a timestamp (number), Date or a parsable date string.
event : string
	The name of the event as it has been tracked on the ReportGrid Analytics Database. 
groupby : string
	A valid periodicity string to be used to create a set of grouped values over time.
path : string
	The path where the events are located.
query : string
	A query string that performs some selection and filter over the event properties.
start : timestamp
	The beginning of the time span.
	The ``start`` and ``end`` paramater must be specified as a timestamp (number), Date or a parsable date string.
timezone : string
	UTC shift expressed as a time span in hours (e.g.: +1.5 or -7.0) or as a timezone (e.g.: ``"America/Los_Angeles"``).

Custom Data
---------------

Sometimes you need to cross the data from ReportGrid with external sources. The Custom Data data source should be used with that intent. 

**options:**

data : array datapoint OR string
	The data can be an array of objects that will be treated as datapoints_ or a name referencing an already available dataset from a previous query; if a name identifier is used, it must match a ``name`` in a data_ section.	

-----------------------
Axes
-----------------------

Each visualization has one or more axes. The axes describe the dimensions over which the dataset are broken apart.
At least one axis in the axes set must refer to a dependendent variable, a variable that is calculated from the othe variables. By default, if that axis is omitted a default one based on counts is automatically inferred (equivalent to ``{ axes : [{ type : "count"}] }``.

Axes are vitally important to a visualization and they usually are mapped to some visual axes on the chart. For example a time axis on a line chart is usually mapped to its X axis. This kind of mapping is made automatically.

**options:**

groupby : string
	A valid periodicity string to be used to create a set of grouped values over time. To obtain 24 values for the hours of the day you will set the ``groupby`` to ``day`` and the periodicity to ``hour``. Note that if you are querying the ReportGrid Analytics Database you will need to use the same ``groupby`` and ``periodicity`` values in the query itself.
	The ``periodicity`` in the axis is expressed as part of the type (e.g. ``{ type : '.#time:hour' }``).
scalemode : string
	A axis may or may not be mapped to a visible axis on the chart. Axis in that sense are used to distribute the values in the two dimensional space. This parameter controls where the ticks start and end.
	 * ``"fit"`` :
		The first and last tickmarks in the scale are set to leave a space before and after. This is particularly useful for bar charts that need some mmargin before and after the centers of the bars.
	 * ``"fill"`` :
		The fill mode puts the first value at the very beginning of the available space and the last one at the other edge. It is handy for line charts and other visualization that need to fill the available space from side to side. 
	 * ``"before"`` :
		The available space is divided equally into ``n`` segments where ``n`` is equal to the number of values in the scale and starting at the beginning of the available space. 
	 * ``"after"`` :
		Same as for before but the scale values are stacked on the opposite side.
type : string
	The name of the property that should match the axis in the datapoint. For example, a valid value for a time axis is ".#time:hour"; if the axis maps to the numbers of events occurred ad a certain time, the property will simply be "count". Properties that are part of the tracked event are prefixed with a dot ".", like in ".browser". Special properties like ".#time:day" or "#location" are prefixed with a hash "#" character and may contain a column symbol followed by a special parameter.
values : array any
	Passing values only make sense if the axis is of type ordinal like "male" and "female" or "firefox", "ie", "chrome" ... Values for numeric axis and time axis are always automatically generated. When the values for an ordinal axis are not explicitly declared they are derived from the values in the set of datapoints_. Declaring the values can be important to fix a specific order to the axis values and to be sure that every values has been taken into consideration even if the current resultset doesn't contain datapoints_ for that value.  
variable : string
	"independent", "dependent"
	This parameter is only required when the data is not generated using a query to ReportGrid. 
view : array any
	The view parameter is used to set the min and max of the axis scale. If passed the array must contain exactly two values that are acceptable values for the current axis and must be ordered correctly (the lower value first).

.. _`Datapoints`:

-----------------------
DataPoint
-----------------------

A datapoint is a single piece of information packed ad an anonymous object. The object can contain just one field or more conform the query that generated it. For time series queries you will have one field containing the time information in the format: ``{ ".#time:hour" : 1318001732325, ... }``
A query on count will always have the property ``"count"``. A query over a property will add the value for that property for each datapoint. So, if you are querying ``.impression.browser`` the datapoint can be something like this: { count : 777, ".broswer" : "chrome", .#time:minute" : 1318001732325 } that basically means that at that minute in time time, the aggregated count over the property ``.browser`` with the value ``chrome`` was 777.

-----------------------
Stats
-----------------------

The stats object contains information about a dataset. The stats are always relative to a certain axis.

min :
	The smallest value in the data set.
max :
	The biggest value in the data set.
tot : (only for numeric values)
	The summatory of all the values in the dataset.
count : 
	The number of values in the data set.
values : 
	The unordered set of distinct values in the dataset.

-----------------------
ReportGrid Extra Fields
-----------------------

The reportgrid-viz.js file adds a lot of fields to the ReportGrid object; most of them are related to visualizations but some are generic utility functions and variables.


ReportGrid.compare
==================

``ReportGrid.compare(any a, any b) string``

It takes two argument and returns an integer as the result of their comparison.

examples:
::
	
	console.log(ReportGrid.compare("b", "a"));
	// outputs: 1
	console.log(ReportGrid.compare(1, 2));
	// outputs: -1

ReportGrid.date.parse
=====================

``ReportGrid.date.parse(string date) Date``

Returns a date from a string value. The string can contain a ISO format date or an expression like "2 days ago", "today", "now", "yesterday" ...


ReportGrid.date.range
=====================

``ReportGrid.date.range(date start, date end, string periodicity) array``

Creates an array of timestamp values for the specified ``periodicity`` from ``start`` to ``end``.

ReportGrid.date.snap
====================

``ReportGrid.date.snap(float timestamp, string periodicity, ?int mode) float``

For the specified periodicity, it snaps the timestamp to the closest value and returns it in timestamp format. The mode parameter states if the snap always happen on the value before (mode < 0), to the value after (mode > 0) or to the closest one (mode = 0, default value).

example:
::

	console.log(ReportGrid.date.snap(new Date("2011-08-01 05:46:00"), "day"))
	// outputs the timestamp for "2011-08-01 00:00:00"

ReportGrid.dump
===============

``ReportGrid.dump(any value) string``

It takes one argument of any type and returns a string out of it. It is very convenient to serialize and object in a readable string.

examples:
::
	
	console.log(ReportGrid.dump(123456.789));
	// outputs: "123,456.78"
	console.log(ReportGrid.dump(new Date("2011-09-23")));
	// outputs: "Friday, September 23, 2011"
	console.log(ReportGrid.dump({ a : 1, b : "c" }));
	// outputs: "{a:1,b:c}"
	console.log(ReportGrid.dump([1,2,3]));
	// outputs: "1, 2, 3"
	
ReportGrid.humanize
===================

``ReportGrid.humanize(any value) string``

It applies a set of transformation to a string to make it more readable. The transformations made are:
* splits the text into several words according to upper-case letters (e.g. ``CallMe`` becomes ``Call Me``).
* replaces all the underscores in the string with  white spaces
* capitalize each separate word in the text
* formats integer and float values
* formats integer and float values in range like texts (e.g. ``income_1000-10000`` becomes ``Income 1.000-10.0000``)

ReportGrid.info.viz.version
===========================

``ReportGrid.info.viz.version string``

Contains the version of the reportgrid-viz.js in use.

examples:
::
	
	console.log(ReportGrid.info.viz.version);
	// outputs: "1.0.1.73" <-- this value obviously changes with new releases

ReportGrid.math.random
======================

``ReportGrid.math.random() float``

Returns a random float number between 0 and 1. Note that the random number generator is actually a *pseudo* random number generator. That means that if the generator is used twice in two different contexts (for example reloading the HTML/JS page) it will generate the same sequence of numbers.

ReportGrid.symbol.get
=====================

``ReportGrid.symbol.get(string type, optional int size) string``

It takes the type of symbol and its size (area in pixel) and returns its SVG Path representation.
The symbols you can draw are: "triangleDown", "triangleUp", "square", "diamond", "cross", "circle", "arrowUp", "arrowDown", "arrowDownWide", "arrowRight", "arrowLeft", "star".
If size is not specified 100 is the default value.

example:
::
	
	console.log(ReportGrid.symbol("star"));
	// outputs: "M0,-8.97635689348711L2.1184202268629577,-2.917315990383311 ..."

.. _css palettes: http://api.reportgrid.com/css/colors/