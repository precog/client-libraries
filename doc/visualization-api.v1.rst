===================================
The ReportGrid JS Visualization API
===================================

.. contents:: `Table of contents`

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
	     src="http://api.reportgrid.com/js/reportgrid-core.js?tokenId=[YOUR TOKEN ID]"></script>
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

Note that by default, charts are unstyled and you will have to provide your color schemes and styles using a standard CSS file. If you want you can use our default stylesheet including the following line of code inside your <head> tag.

::
	
	<link rel="stylesheet" type="text/css" href="http://api.reportgrid.com/css/rg.css"/>

You can change the color schemes of the visualization loading one of our optional `css palettes`_. All of the color palettes are contained into this location http://api.reportgrid.com/css/colors/

Every visualization is a method of the object ReportGrid and every visualization takes an element placeholder as the first argument. That argument can be a DOM element or a CSS selector (string); usually a DIV element referenced by its ID is used for the effect. In the first sample the "#chart" selector refers to the <div id="chart"></div> node in the DOM.

The second argument is always an object that contains all the info required to make the visualization pop-up.
There are two way to configure that object, a simplified one and a complete one; they both share a common "option" field that works the same in both context.

-----------------------
Simplified Query Model
-----------------------

In the simplified query model you have the options to quickly build a big set of visualizations in a really easy fashion.
The simplidied query model always tries to fill the blanks for you. 
You can have one of the following combinations of parameters:

 * path: the query will retrieve all the count of all of the events at the path
 * path + event/events: the query will retrieve the count of all the events at the path
 * path + event + property: the query will retrieve the count of all the values for the specified property

If path is not specified the root path "/" is always assumed.

The queries can be restricted to a certains time span by specifying the ``start`` and ``end`` parameters. Those parameters must always be specified in pair. When a time series is produced, the system will always fill those parameters automatically when they are not passed.

For a better insight on how the data are queried and retrieved tak a look at the `Complete Query System`_

options:

event : string
	The event name.
events : string || array string
	One or more (array of values) event names.
end : timestamp
	end of the time span
	The ``start`` and ``end`` paramater must be specified as a timestamp (number), Date or a parsable date string.
path : string
	The path where the events are stored. The path must alwyas begin with a slash ``/``. The ending slash is optional.
	Note that any path is a valid value even if no events are stored there. In case the path does not exist because there are no events stored yet, the server will response with an empty set of data.
periodicity : string
	Periodicity can be any of the following values: ``minute``, ``hour``, ``day``, ``week``, ``month``, ``year`` or ``eternity``
	The granularity is choosen automatically based on the start/end range if not specified and based on the type of visualization.
	So if you pick a ``piechart`` the ``periodicity`` will be automatically set to ``eternity`` but not for a ``linechart``.
	Note that if you pick a small granularity with an extended time span you can get a really huge amount of data back that besides slowing down your visualizations will probably not convey any meaningfull visualization.
property : string
	The property name whose values you want to visualize.
start : timestamp
	beginning of the time span.
	The ``start`` and ``end`` paramater must be specified as a timestamp (number), Date or a parsable date string.

-----------------------
Visualization Methods
-----------------------

ReportGrid.barChart
-------------------

``ReportGrid.barChart(selector el, parameters object) void``

Conforming the queried data, for each tick in the X axis might exist one or more bar. The bars can belong to the same Y axis or to different ones. If they belong to the same Y axis the data can be segmented to produce several bars. The bars segmented on the same Y axis can be stacked or not.

options:

barpadding : float
	Padding distance in pixel between groups of bars for the same X tick.
barpaddingaxis : float
	Padding distance in pixel between bars when grouped on different Y axis.
barpaddingdatapoint : float
	Padding distance in pixel between bars when lined horizontally.
effect : string ("noeffect", "gradient", "gradient-{value}")
	The effect to apply to the bars.
	 * ``noeffect`` simply uses a solid fill color
	 * ``gradient`` applies a gradient
	 * ``gradient-{value}`` applies a gradient from the color in the stylesheet darkened or lightned by the value. If the value is 1.0 there will be no gradient at all, use bigger or lower values to make the gradient lighter or darker.
stacked : bool
	Determines if the bars are stacked on top of each other or lined horizontally.

CARTESIAN

LAYOUT


ReportGrid.funnelChart
----------------------

``ReportGrid.funnelChart(selector el, parameters object) void``

The funnel chart is an extremely insightful visualization to quickly grasp ROI-like information. The funnel starts big for a certain variable and reduces for some subordinate value.

options:

arrowsize : float
	The size of the funnel arrow side in pixels.
click : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
effect : string ("noeffect", "gradient", "gradient-{value}")
	The effect to apply to the funnel sections.
	 * ``noeffect`` simply uses a solid fill color
	 * ``gradient`` applies a gradient
	 * ``gradient-{value}`` applies a gradient from the color in the stylesheet darkened or lightned by the value. If the value is 1.0 there will be no gradient at all, use bigger or lower values to make the gradient lighter or darker.
flatness : float
	A value to accentuate or reduce the 3D effect of the chart. The default value is 1. A value near to 0.0 will make the funnel appear almost completely flat.
label : object funnel label options
	Conveys information over labelling the funnel sections.
segmentpadding : float
	Distance in pixel (white space) between the sections of the funnel chart.
sort : function(datapoint a, datapoint b) int
	A custom function to order the datapoints before rendering them.

LAYOUT

FUNNEL LABEL OPTIONS

extends LABEL

ReportGrid.geo
-------------------

``ReportGrid.geo(selector el, parameters object) void``

The geo visualization can be used to produce choropleth charts or ???? charts. Each geo visualization can overlay more than one geographic layer associated or not to the datapoints retrieved by the query. This gives you the option to load geographic features to convey data information and/or to decorate the visualization.
Note that the data contained in the geographic features (if any) is merged in the datapoint when the visualization is rendered. That metadata will be contained in the ``#data`` field. In the same way also the centroids in pixel of the geometries is injected in the datapoint in the ``#centroid`` field.

options:

label : object label options
	Configuration object for text labels. The values passed to this object are used as defaults for the map object(s).
map : object/array map options
	A single map definition or an array of maps.
MAP

options:
classname : string
	An optional class name to associate with the geometries container. The ``classname`` may be used for styling purposes.
click : function(object datapoint, object stats) void
color : string || function(datapoint, stats) string
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
	A handler function that is executed when the user click or touches a datapoint.
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
property : null || string
	The property field in the datapoint associated to the feature ID in the geometric layer. The default value is ``#location``. If the property is set to ``null`` the geographic layer will be rendered but not associated to the data.
radius : float || function(datapoint, stats) float
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

LAYOUT

ReportGrid.heatGrid
-------------------

``ReportGrid.heatGrid(selector el, parameters object) void``

The heatgrid visualization divides a two dimensional space into a grid whose columns and rows are associated to the first two axis in your query. Each cell of the grid is colored according to the third axis.

options:

color : string || function(datapoint, stats) string
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


CARTESIAN
LAYOUT

ReportGrid.leaderBoard
-----------------------------------------------------------

``ReportGrid.leaderBoard(selector el, parameters object) void``

This visualization renders a list of values associated to the datapoints produced by your query.

options:

animation : object animation options
	Defines the animation behavior of the visualization.
click : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
effect : string
	States the effect to apply to the list items in the leaderboard.
	 * ``"noeffect"`` : 
		No gradient is applied below the item.
	 * ``"gradient"`` : 
		Applies a gradient below the item that is proportional to the sum of the values in the datapoint set.
	 * ``"gradient-max"`` : 
		Same as above but the gradient is proportional to the biggest value in the datapoint set.
label : object label options
	Configuration object for text labels.
sort : function(object datapoint, object datapoint) int
	Sort function to rearrange the orders of the items in the leaderboard list.
	

ReportGrid.lineChart
--------------------

``ReportGrid.lineChart(selector el, parameters object) void``

The ``lineChart`` can be used to build standard line charts, area charts stacked or not.

options:

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
symbol : string || function(object datapoint, object stats) string
	Each datapoint in the line charts can be associated to an optional symbol. The symbol can be described statically using a string or using a function. The symbol must be expressed in SVG PATH format. There is a practical function ``ReportGrid.symbol.get()`` to quickly build symbols.
symbolstyle : function(object datapoint, object stats) string
	If symbols are added to the lines you can use ``symbolstyle`` to render a custom style for each of them. The style produced by the custom function must be a string in CSS format.
y0property : string
	This parameter is used to determine what value in the datapoint must be used to stack values.
	Usually that value must be calculated transforming the datapoints from your query.

interpolation : string
	Linecharts are not interpolated by default (equivalent to "linear" interpolation) but can be smoothed or transformed using one of the following values: ``"basis"``, ``"basisopen"``, ``"basisclosed"``, ``"cardinal"``, ``"cardinal-{value}"``, ``"cardinalopen"``, ``"cardinalopen-{value}"``, ``"cardinalclosed"``, ``"cardinalclosed-{value}"``, ``"monotone"``, ``"stepafter"``, ``"stepbefore"``, ``"linear"``
	The ``value`` parameter is an optinal ``float`` value for ``cardinal`` interpolations.

CARTESIAN

LAYOUT

ReportGrid.pieChart
--------------------------------------------------------

``ReportGrid.pieChart(selector el, parameters object) void``

The ``pieChart`` is obviously used to render pie charts, but it can also be used to render donut charts.
All of the ``radius`` parameters below should be expressed as a ``float`` value between 0 and 1 where 0 is at the center of the pie and 1 is at the edge of the available chart space.

options:

animation : object animation options
	Defines the animation behavior of the visualization.
click : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
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
	States where the datapoint labels are positioned.
outerradius : float
	The outer radius is useful when you want accentuated transitions (animations) that can bleach the border of the charts. Another use in combination with ``innerradius`` is to nest two or more donut charts in the same visualization and avoid overlappings.
overradius : float
	The over radius is used to control the slice size when animation is turned on.
tooltipradius : float
	Controls the position of the tooltip balloon when activated and the mouse is positioned over a slice.
sort : function(object datapoint, object datapoint) int
	Sort function to rearrange the orders of the slices in the pie chart.

LAYOUT

ReportGrid.pivotTable
---------------------

``ReportGrid.pivotTable(selector el, parameters object) void``

options:

click : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
columnaxes : int
	xxx
displaycolumntotal : bool
	xxx
displayheatmap : bool
	xxx
displayrowtotal : bool
	xxx
endcolor : string
	xxx
label : object pivottable label options
	Configuration object for text labels.
startcolor : string
	xxx

PIVOT TABLE LABEL OPTIONS

extends LABEL AXIS

total : function(float value, object stats) string
	xxx
totalover : function(float value, object stats) string
	xxx

ReportGrid.scatterGraph
-----------------------

``ReportGrid.scatterGraph(selector el, parameters object) void``

options:

symbol : string || function(object datapoint, object stats) : string
	xxx
symbolstyle : string || function(object datapoint, object stats) : string
	xxx

CARTESIAN

LAYOUT

ReportGrid.streamGraph
----------------------

``ReportGrid.streamGraph(selector el, parameters object) void``

options:

effect : string
	"noeffect", "gradient", "gradienth", "gradient-{lightness}", "gradienth-{lightness}"
	xxx
interpolation : string
	"basis", "basisopen", "basisclosed", "cardinal", "cardinalopen", "cardinalclosed", "monotone", "stepafter", "stepbefore", "linear"
	xxx

CARTESIAN

LAYOUT


ReportGrid.viz
--------------

``ReportGrid.viz(selector el, parameters object, string type) void``

The ``ReportGrid.viz`` method is a generic function to build a visualization programmatically (passing the type of visualization as the third parameter). The ``type`` argument can take any of the names of the methods described above all lower case.

The following two declarations are basically equivalent:

::

	ReportGrid.lineChart("#chart", { ...});
	
	ReportGrid.viz("#chart", { ...}, "linechart");

-----------------------
Complete Query Model
-----------------------

options:

name : string
	xxx
scale : function(array datapoint) array datapoint
	xxx
src : source options || array source options
	xxx
transform : function(array datapoint, ...) array datapoint
	xxx

-----------------------
Axes
-----------------------

options:

groupby : string
	valid periodicity
	xxx
scalemode : string
	"fit", "fill", "before", "after"
	xxx
type : string
	xxx
values : array any
	xxx
variable : string
	"independent", "dependent"
	xxx
view : array any
	xxx

-----------------------
Data
-----------------------

-----------------------
Generic Options
-----------------------

-----------------------
ReportGrid Extra Fields
-----------------------

The reportgrid-viz.js file adds a lot of fields to the ReportGrid object; most of them are related to visualizations but some are generic utility functions and variables.


ReportGrid.compare
------------------

``ReportGrid.compare(any a, any b) string``

It takes two argument and returns an integer as the result of their comparison.

examples:
::
	
	console.log(ReportGrid.compare("b", "a"));
	// outputs: 1
	console.log(ReportGrid.compare(1, 2));
	// outputs: -1

ReportGrid.date.parse
---------------------

``ReportGrid.date.parse(date start, date end, string periodicity) array of timestamps``


ReportGrid.date.range
---------------------

``ReportGrid.date.range(date start, date end, string periodicity) array of timestamps``

ReportGrid.date.snap
---------------------

``ReportGrid.date.snap(float timestampe, string periodicity) float``


ReportGrid.dump
---------------

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
-------------------

``ReportGrid.humanize(any value) string``

ReportGrid.info.viz.version
---------------------------

``ReportGrid.info.viz.version string``

Contains the version of the reportgrid-viz.js in use.

examples:
::
	
	console.log(ReportGrid.info.viz.version);
	// outputs: "1.0.1.73" <-- this value obviously changes with new releases

ReportGrid.math.random
----------------------

``ReportGrid.math.random() float``

ReportGrid.symbol.get
---------------------

``ReportGrid.symbol.get(string type, optional int size) string``

It takes the type of symbol and its size (area in pixel) and returns its SVG Path representation.
The symbols you can draw are: "triangleDown", "triangleUp", "square", "diamond", "cross", "circle", "arrowUp", "arrowDown", "arrowDownWide", "arrowRight", "arrowLeft", "star".
If size is not specified 100 is the default value.

example:
::
	
	console.log(ReportGrid.symbol("star"));
	// outputs: "M0,-8.97635689348711L2.1184202268629577,-2.917315990383311 ..."

ANIMATION

options:

animated : bool
	xxx
duration : int
	xxx
delay : int
	xxx
ease : function(float v) float
	xxx

DATASOURCE

options:

data : array datapoints
	xxx
end : timestamp
	xxx
event : string
	xxx
groupby : string
	xxx
groupfilter : string
	(experimental)
	xxx
name : string
	xxx
path : string
	xxx
query : string
	xxx
start : timestamp
	xxx
timezone : string
	xxx

CARTESIAN

options:

animation : animation options
	xxx
click : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
displayanchorlinerule : bool || function(string type ) bool
	xxx
displayanchorlinetick : bool || function(string type ) bool
	xxx
displayrules : bool || function(string type ) bool
	xxx
displayruleminor : bool || function(string type ) bool
	xxx
displayrulemajor : bool || function(string type ) bool
	xxx
displayticklabel : bool || function(string type ) bool
	xxx
displaytickmajor : bool || function(string type ) bool
	xxx
displaytickmarks : bool || function(string type ) bool
	xxx
displaytickminor : bool || function(string type ) bool
	xxx
label : object LABEL AXIS
	Configuration object for text labels.
labelanchor : string || function(string type) string
	xxx
labelangle : float || function(string type) float
	xxx
labelorientation : string || function(string type) string
	xxx
lengthtick : float
	xxx
lengthtickmajor : float
	xxx
lengthtickminor : float
	xxx
paddingtick : float
	xxx
paddingticklabel : float
	xxx
paddingtickmajor : float
	xxx
paddingtickminor : float
	xxx
segment : segment options
	xxx
segmenton : string
	xxx

LABEL

options:

datapoint : function(object datapoint, object stats) string
	xxx
datapointover : function(object datapoint, object stats) string
	xxx
title : string || function(variable, array datapoints) string
	xxx

LABEL AXIS

extends LABEL

options:

axis : function(string type) string
	xxx
axisvalue : function(anu value, string type) string
	xxx
tickmark : function(any value, string type) string
	xxx

SVG LAYOUT OPTIONS

options:

height : float
	xxx
layout : string
	xxx
main : string
	(experimental)
	xxx
padding : object padding options
	xxx
titleontop : bool
	xxx
yscaleposition : string
	("alternating", "left", "right")
width : float
	xxx

PADDING OPTIONS

options:

bottom : float
	xxx
left : float
	xxx
right : float
	xxx
top : float
	xxx

SEGMENT OPTIONS

options:

on : string
	xxx
scale : function(array datapoints) array
	xxx
transform : function(array datapoints) array
	xxx

.. _css palettes: http://api.reportgrid.com/css/colors/