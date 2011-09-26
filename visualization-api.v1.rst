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

options:

start : timestamp
	xxx
end : timestamp
	xxx
path : string
	xxx
event : string
	xxx
events : string || array string
	xxx
property : string
	xxx
periodicity : string
	xxx
groupby : string
	xxx

-----------------------
Visualization Methods
-----------------------

ReportGrid.barChart
-------------------

``ReportGrid.barChart(selector el, parameters object) void``

options:

barpadding : float
	xxx
barpaddingaxis : float
	xxx
barpaddingdatapoint : float
	xxx
effect : string ("noeffect", "gradient", "gradient-{value}")
	xxx
stacked : bool
	xxx

.. include:: visualization-api.common-options.v1.rst
.. include:: visualization-api.layout-options.v1.rst
CARTESIAN

LAYOUT


ReportGrid.funnelChart
----------------------

``ReportGrid.funnelChart(selector el, parameters object) void``

options:

label : object funnel label options
	xxx
sort : function()
	xxx
click : function(object datapoint, object datapoint) void
	xxx
segmentpadding : float
	xxx
flatness : float
	xxx
effect : string ("noeffect", "gradient", "gradient-{value}")
	xxx
arrowsize : float
	xxx

LAYOUT

FUNNEL LABEL OPTIONS

extends LABEL

arrow : function(object datapoint, object stats) string
	xxx

ReportGrid.heatGrid
-------------------

``ReportGrid.heatGrid(selector el, parameters object) void``

options:

startcolor : string
	xxx
endcolor : string
	xxx

.. include:: visualization-api.common-options.v1
.. include:: visualization-api.layout-options.v1
CARTESIAN

LAYOUT

ReportGrid.leaderBoard
-----------------------------------------------------------

``ReportGrid.leaderBoard(selector el, parameters object) void``

options:

animation : object animation options
	xxx
label : object label options
	xxx
click : function(object datapoint, object stats)
	xxx
sort : function(object datapoint, object datapoint) int
	xxx
effect : string
	("gradient", "gradient-max")
	xxx

ReportGrid.lineChart
--------------------

``ReportGrid.lineChart(selector el, parameters object) void``

options:

symbol : string || function(object datapoint, object stats) string
	xxx
symbolstyle : function(object datapoint, object stats) string
	xxx
y0property : string
	xxx
displayarea : bool
	xxx
effect : string
	"dropshadow", "gradient", "noeffect", "dropshadow-{offsetx}", "dropshadow-{offsetx}-{offsety}", "dropshadow-{offsetx}-{offsety}-{levels}", "gradient-{lightness}", "gradient-{lightness}-{levels}"
	xxx

interpolation : string
	"basis", "basisopen", "basisclosed", "cardinal", "cardinalopen", "cardinalclosed", "monotone", "stepafter", "stepbefore", "linear"
	xxxx

CARTESIAN

LAYOUT

ReportGrid.pieChart
--------------------------------------------------------

``ReportGrid.pieChart(selector el, parameters object) void``

options:

labelradius : float
	xxx
dontfliplabel : bool
	xxx
displaylabels : bool
	xxx
labelorientation : string
	"fixed", "fixed-{angle}", "ortho", "orthogonal", "align", "aligned", "horizontal"
innerradius : float
	xxx
outerradius : float
	xxx
overradius : float
	xxx
tooltipradius : float
	xxx
animation : object animation options
	xxx
label : object label options
	xxx
sort : function(object datapoint, object datapoint) int
	xxx
click : function(object datapoint, object stats) void
	xxx
effect : string
	"gradient", "noeffect", "gradient-{lightness}"
	xxx

LAYOUT

ReportGrid.pivotTable
---------------------

``ReportGrid.pivotTable(selector el, parameters object) void``

options:

columnaxes : int
	xxx
displayheatmap : bool
	xxx
displaycolumntotal : bool
	xxx
displayrowtotal : bool
	xxx
startcolor : string
	xxx
endcolor : string
	xxx
label : object pivottable label options
	xxx
click : function(object datapoint) void
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

interpolation : string
	"basis", "basisopen", "basisclosed", "cardinal", "cardinalopen", "cardinalclosed", "monotone", "stepafter", "stepbefore", "linear"
	xxx
effect : string
	"noeffect", "gradient", "gradienth", "gradient-{lightness}", "gradienth-{lightness}"
	xxx

CARTESIAN

LAYOUT


ReportGrid.viz
--------------

``ReportGrid.viz(selector el, parameters object, string type) void``


-----------------------
Complete Query Model
-----------------------

options:

name : string
	xxx
transform : function(array datapoint, ...) array datapoint
	xxx
scale : function(array datapoint) array datapoint
	xxx
src : source options || array source options
	xxx

-----------------------
Axes
-----------------------

options:

type : string
	xxx
view : array any
	xxx
values : array any
	xxx
groupby : string
	valid periodicity
	xxx
variable : string
	"independent", "dependent"
	xxx
scalemode : string
	"fit", "fill", "before", "after"
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

query : string
	xxx
path : string
	xxx
event : string
	xxx
name : string
	xxx
start : timestamp
	xxx
end : timestamp
	xxx
timezone : string
	xxx
data : array datapoints
	xxx
groupby : string
	xxx
groupfilter : string
	(experimental)
	xxx

CARTESIAN

options:

animation : animation options
	xxx
segment : segment options
	xxx
segmenton : string
	xxx
click : function(object datapoint, object stats) void
	xxx
label : object LABEL AXIS
	xxx
displaytickmarks : bool || function(string type ) bool
	xxx
displaytickminor : bool || function(string type ) bool
	xxx
displaytickmajor : bool || function(string type ) bool
	xxx
displayticklabel : bool || function(string type ) bool
	xxx
displayanchorlinetick : bool || function(string type ) bool
	xxx
displayrules : bool || function(string type ) bool
	xxx
displayruleminor : bool || function(string type ) bool
	xxx
displayrulemajor : bool || function(string type ) bool
	xxx
displayanchorlinerule : bool || function(string type ) bool
	xxx
lengthtick : float
	xxx
lengthtickminor : float
	xxx
lengthtickmajor : float
	xxx
paddingtick : float
	xxx
paddingtickminor : float
	xxx
paddingtickmajor : float
	xxx
paddingticklabel : float
	xxx
labelorientation : string || function(string type) string
	xxx
labelanchor : string || function(string type) string
	xxx
labelangle : float || function(string type) float
	xxx

LABEL

options:

title : string || function(variable, array datapoints) string
	xxx
datapoint : function(object datapoint, object stats) string
	xxx
datapointover : function(object datapoint, object stats) string
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

width : float
	xxx
height : float
	xxx
layout : string
	xxx
main : string
	(experimental)
	xxx
titleontop : bool
	xxx
yscaleposition : string
	("alternating", "left", "right")
padding : object padding options
	xxx

PADDING OPTIONS

options:

top : float
	xxx
bottom : float
	xxx
left : float
	xxx
right : float
	xxx

SEGMENT OPTIONS

options:

on : string
	xxx
transform : function(array datapoints) array
	xxx
scale : function(array datapoints) array
	xxx

.. _css palettes: http://api.reportgrid.com/css/colors/