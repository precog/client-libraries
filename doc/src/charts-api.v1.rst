===================================
The ReportGrid Charts API
===================================

.. contents:: :depth: 2

---------------
Overview
---------------

ReportGrid JavaScript charts are HTML5 widgets that you can embed in your web pages. First of all you have to be sure to include the required javascript files.

The minimal setup requires that you add a reference to reportgrid-charts.js. This file has no dependencies and is used to generates all of the charts in the ReportGrid library.

::
	
	<!DOCTYPE html>
	<html>
	  <head>
	    <title>MySite.com</title>
	    <link rel="stylesheet" type="text/css" href="http://api.reportgrid.com/css/rg-charts.css"/>
	    <script type="text/javascript"
	     src="http://api.reportgrid.com/js/reportgrid-charts.js"></script>
	    <script type="text/javascript">
	document.addEventListener("DOMContentLoaded", function(){
	  ReportGrid.barChart("#chart", {
	    axes : ['gender', 'population'],
	    data : [{ gender : 'male', population: 137 }, { gender : 'female', population: 143 }]
	  });
	}, false);
	    </script>
	  </head>
	  <body>
	  <div id="chart"></div>
	  </body>
	</html>

The charts are styled with a default theme using the ``rg-charts.css`` stylesheet. If you prefer you can customize the visualizations providing an alternative stylesheet or refining the existing one with an additional CSS. CSS styles applies to SVG (used to render the visualizations) as much as they are used to style normal HTML elements.

You can change the color schemes of the visualization loading one of our optional `css palettes`_. The palettes CSS files are located at http://api.reportgrid.com/css/colors/

Every visualization is a method of the object ``ReportGrid`` and every visualization takes an element placeholder as the first argument. That argument can be a DOM element or a CSS selector (string); usually you will want to use the ID selector of an existing DIV in your page. In the example above the "#chart" selector refers to the <div id="chart"></div> node in the DOM.
The second argument is a parameter objects that must provide at least two fields: ``axes`` and ``data`` (or ``load``).

The second argument is always an object that contains all the info required to make the visualization render.

Note that in this document whenever you encounter a string enclosed in curly brackets ``{value}`` it means that the entire string must be replaced with a custom value (e.g. for ``gradient:{value}`` you can use either ``gradient:0.75``, ``gradient:1.25`` or any valid value for gradient).

-----------------------
Data Loading
-----------------------

Loading data is the first step to generate meaningful visualizations. The data can be provided by any accessible source and format provided that it is passed as an array of data points to the visualization engine. In this document we will refer to a data point as to a JavaScript object containing key/value pairs and to a dataset as an array of data points.
To build a pie chart that depicts male population VS female population you will probably need a dataset in the following format:

::
	
	var data = [{ gender : "male", population: 137 }, { gender : "female", population: 143 }];

In the parameters object (the second argument of every visualization method) you always have to set a data source this can be done by setting either ``data`` or ``load`` (see below). The other mandatory parametr for the parameters object is ``axes``. So the complete pie chart example for population by gender is:

::
	
	ReportGrid.pieChart("#chart", {
	  axes : ['gender', 'population'],
	  data : [{ gender : 'male', population: 137 }, { gender : 'female', population: 143 }]
	});



Data
=======================

When you have your data points already available in your script you can pass them directly to the visualization using the ``data`` parameter as in the example above.


Load
=======================

Sometimes the data needs to be loaded from a service and is not immeditely available. In that case you can wrap your data loading operation in a function passed to the ``load`` parameter. The signature for that function is ``function(callback : Array<DataPoint> -> Void) -> Void``, which means a function that takes a function as an argument. That argument function should be called by your code once the data is available. Supposing that you are using JQuery to load some JSON objects from a service, that could be coded this way:

::
	
	ReportGrid.pieChart("#chart", {
	  axes : ['gender', 'population'],
	  load : function(callback) {
	  	$.get(serviceurl, callback, 'json');
	  }
	});

Sometimes you want to be able to transform your data set before serving the data to the visualization, to do that the Charts API provides a very interesting Query API.

Axes
=======================

Axes describe the dimensions present in the datapoints that the user wants to display in the visualizations. The number of axes depends on the type of the visualization and for that reason they are always passed as an array. The array can be as simple as an array of strings (only stating the type name of each axis) or a complete JavaScript object that can bring more options than just the type name itself.

If you decide for the object definition the only mandatory parameter is ``type``. ``type``must match one of the property names present in the data points you want to render. If your axis represents a time series you might want to follow the special notation: ``time:periodicity`` (where ``periodicity`` can be: ``minute``, ``hour``, ``day``, ``week``, ``month`` or ``year``) that will build a correct time scale 

**options:**

``type`` (mandatory) : string
	The axis type name, must match a property in you data point objects.
``scalemode`` : string (``fit``, ``fill``, ``before`` or ``after``)
	States how the scale should 'fill' the available space. Every visualization has its default scalemode automatically set and this option is there if you want to override the default behavior.
	 * ``"fit"`` :
		The first and last tickmarks in the scale are set to leave a space before and after. This is particularly useful for bar charts that need some mmargin before and after the centers of the bars.
	 * ``"fill"`` :
		The fill mode puts the first value at the very beginning of the available space and the last one at the other edge. It is handy for line charts and other visualization that need to fill the available space from side to side. 
	 * ``"before"`` :
		The available space is divided equally into ``n`` segments where ``n`` is equal to the number of values in the scale and starting at the beginning of the available space. 
	 * ``"after"`` :
		Same as for before but the scale values are stacked on the opposite side.
``values`` : array mixed
	Specifies exactly which values should be displayed for this axis. It is also useful to precisely order the values along the axis.
``variable`` : string (``"dependent"`` or ``"independent"``)
	It is not usually neede to specify this property unless you want for example to reverse the axis in a cartesian chart.
``view`` : array mixed
	The array must contain the lowest and highest value to display in the chart. If ``view`` is omitted it will be generated automatically from all the values in the data set.

-----------------------
Visualization Methods
-----------------------

ReportGrid.barChart
===================

``ReportGrid.barChart(selector el, parameters object) void``

Based on the values in the data set, for each tick in the X axis might exist one or more bar. The bars can belong to the same Y axis or to different ones (in that case more than 2 ``axes`` must be specified). If they belong to the same Y axis the data can be segmented to produce several bars. The bars segmented on the same Y axis can be stacked or not.
Bar charts can also be rendered horizontally.

**options:**

``barpadding`` : float (default is 12 pixels)
	Padding distance in pixel between groups of bars for the same X tick.
``barpaddingaxis`` : float (default is 4 pixels)
	Padding distance in pixel between bars when grouped on different Y axis.
``barpaddingdatapoint`` : float (default is 2 pixels)
	Padding distance in pixel between bars when lined horizontally.
``effect`` : string ("noeffect", "gradient", "gradient:{value}", default is "gradient:1.25")
	The effect to apply to the bars.
	 * ``noeffect`` :
		simply uses a solid fill color
	 * ``gradient`` :
	 	applies a gradient
	 * ``gradient:{value}`` :
	 	applies a gradient from the color in the stylesheet darkened or lightned by the value. If the value is 1.0 there will be no gradient at all, use bigger or lower values to make the gradient lighter or darker.
``horizontal`` : bool (default is false)
	Renders the bar chart with horizontal bars.
``stacked`` : bool (default is true)
	Determines if the bars are stacked on top of each other or lined horizontally.

.. include:: visualization-api-v1-options-cartesian.txt
.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt

.. include:: visualization-api-v1-options-label.txt
.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.funnelChart
======================

``ReportGrid.funnelChart(selector el, parameters object) void``

The funnel chart is an extremely insightful visualization to quickly grasp ROI-like information. The funnel starts big for a certain variable and 'funnel' into subordinate values.
Note that the subordinate values do not necessarely need to be smaller and you can achieve that way a reversed funnel effect.
The funnel chart requires two axes, one to segment the funnel and one to size the segments.

**options:**

``arrowsize`` : float
	The size of the funnel arrow side in pixels.
``click`` : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
``effect`` : string ("noeffect", "gradient", "gradient:{value}")
	The effect to apply to the funnel sections.
	* ``noeffect`` simply uses a solid fill color
	* ``gradient`` applies a gradient
	* ``gradient:{value}`` : applies a gradient from the color in the stylesheet darkened or lightned by the value. If the value is 1.0 there will be no gradient at all, use bigger or lower values to make the gradient lighter or darker (default is 1.25).
``flatness`` : float
	A value to accentuate or reduce the 3D effect of the chart. The default value is 1. A value near to 0.0 will make the funnel appear almost completely flat.
``label`` : object labeloptions
	Conveys information over `labelling the funnel sections`_.
``segmentpadding`` : float
	Distance in pixel (white space) between the sections of the funnel chart.
``sort`` : function(object a, object b) int
	A custom function to order the datapoints before rendering them.

.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt

.. _`labelling the funnel sections`:

.. include:: visualization-api-v1-options-label.txt

``arrow`` : function(object datapoint, object stats) string
	A function to generate a custom label to put over the section arrows. If the returned value is ``null`` the arrow will not be displayed.

ReportGrid.geo
===================

``ReportGrid.geo(selector el, parameters object) void``

The geo visualization can be used to produce choropleth charts or point/area diagrams. Each geo visualization can overlay more than one geographic layer associated or not to the datapoints retrieved by the query. This gives you the option to load geographic features to convey data information and/or to decorate the visualization.
Note that the data contained in the geographic features (if any) is merged in the datapoint when the visualization is rendered. That metadata will be contained in the ``#data`` field. In the same way also the centroids in pixel of the geometries is injected in the datapoint in the ``#centroid`` field.
Geo charts require two axes, one to identify the geomtry features and one to apply the colouring or scaling the overlapped symbol.

**options:**

``label`` : object label options
	Configuration object for text labels. The values passed to this object are used as defaults for the map object(s).
``map`` : object/array map options
	A single map definition or an array of `Map Options`_.

Map Options
-----------

**options:**

``classname`` : string
	An optional class name to associate with the geometries container. The ``classname`` may be used for styling purposes.
``click`` : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
``color`` : string OR function(datapoint, stats) string
	This parameter determins how the geometries are colored. The default value is ``css`` but the field accept any of the following:
	 * ``"css"`` or ``"css:{int}"`` :
	 	It uses the colors in the stylesheet to colour the geometries. The colors in the css palette are automatically detected and the color scale is divided proportionally to associate the values in the axis with each color in the palette. You can reduce the number of values used by specifying an integer number after the dash: ``"css-5"`` will only use the first 5 colors in the associated CSS stylesheet.
	 * ``function()`` :
		a custom function whose return value must be a valid CSS color string.
	 * ``"i:{color1},{color2},..."`` or ``"interpolated:{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built on the interpolation between those colors.
	 * ``"s:{color1},{color2},..."`` or ``"sequence:{color1},{color2},..."`` or ``"{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built to match exactly the colors in the list.
	 * ``"f:{color}"`` or ``"fixed:{color}"`` or ``"{color}"`` :
		Use this option to colour all the geometries with the same color.
``label`` : object label options
	Configuration object for text labels.
``mapping`` : mixed (a URL or a json object)
	The mapping propertyes is used to map the geometries identifiers to something different. The mapping can be done using a JavaScript object that has as keys the values matching the id field of the data point and the values match the id in the grometry file.
``mode`` : string
	The projection mode for the ``azimuthal`` projection. The value can be either ``orthographic`` or ``stereographic``.
``origin`` : array of float
	Moves the origin of the projection (available for the ``albers`` and ``azimuthal`` projections).
``parallels`` : array of float
	Redefines the parallels in the ``albers`` projection.
``projection`` : string
	The kind of projection to use to render the geographic features. The available values are: ``mercator``, ``albers``, ``albersusa`` and ``azimuthal``
``property`` : null OR string
	The property field in the datapoint associated to the feature ID in the geometric layer. The default value is ``#location``. If the property is set to ``null`` the geographic layer will be rendered but not associated to the data.
``radius`` : float OR function(datapoint, stats) float
	If the geometry contains Point geometries they are rendered as ``svg:circle`` elements whose radius can be a fixed value or calculated using a custom function.
``scale`` : float
	A scale factor to reduce or enlarge the geographic visualization.
``template`` : string
	To simplify geo consumption some premade templates and geographic files are made available by the ReportGrid API.
	The available templates are:
	* ``"world"`` :
	* ``"usa-states"`` :
	* ``"usa-state-centroids"`` :
	* ``"usa-counties"`` :
``translate`` : array of float
	Array of 2 values in pixel to pan the visualization from its center.
``type`` : string
	The format of the geographic file addressed in ``url``. So far the only supported format is the default one ``geojson``.
``url`` : string
	The url of the file containing the geometries to display.

.. include:: visualization-api-v1-options-layout.txt
.. include:: visualization-api-v1-options-padding.txt

.. include:: visualization-api-v1-options-label.txt

ReportGrid.heatGrid
===================

``ReportGrid.heatGrid(selector el, parameters object) void``

The heatgrid visualization divides a two dimensional space into a grid whose columns and rows are associated to the first two axis in your query. Each cell of the grid is colored according to the third axis.

**options:**

``color`` : string OR function(datapoint, stats) string
	This parameter determins how the geometries are colored. The default value is ``css`` but the field accept any of the following:
	 * ``"css"`` or ``"css:{int}"`` :
		It uses the colors in the stylesheet to colour the geometries. The colors in the css palette are automatically detected and the color scale is divided proportionally to associate the values in the axis with each color in the palette. You can reduce the number of values used by specifying an integer number after the dash: ``"css-5"`` will only use the first 5 colors in the associated CSS stylesheet.
	 * ``function()`` :
		a custom function whose return value must be a valid CSS color string.
	 * ``"i:{color1},{color2},..."`` or ``"interpolated:{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built on the interpolation between those colors.
	 * ``"s:{color1},{color2},..."`` or ``"sequence:{color1},{color2},..."`` or ``"{color1},{color2},..."`` :
		You pass a list of colors and the color scale is built to match exactly the colors in the list.
	 * ``"f:{color}"`` or ``"fixed:{color}"`` or ``"{color}"`` :
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
The leaderboard requires two axes, one to generate the items in the list and the other to quantify each item.

**options:**

``animation`` : object animation options
	Defines the `animation behavior`_ of the visualization.
``click`` : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
``displaybar`` : bool (defaultr true)
	Makes the measurement bar visible or not.
``effect`` : string
	States the effect to apply to the list items in the leaderboard.
	 * ``"noeffect"`` : 
		No gradient is applied below the item.
	 * ``"gradient"`` : 
		Applies a gradient below the item that is proportional to the sum of the values in the datapoint set.
	 * ``"gradient-max"`` : 
		Same as above but the gradient is proportional to the biggest value in the datapoint set.
``label`` : object label options
	Configuration object for text labels.
``sort`` : function(object datapoint, object datapoint) int
	Sort function to rearrange the orders of the items in the leaderboard list.
	Configuration object for text labels.
``usemax`` : bool (default false)
	By default percentage values and the length of the measurement bar is the ratio between the current value and the total sum; activating this flag the proportion will be done using the maximum value instead.

.. _`animation behavior`:

.. include:: visualization-api-v1-options-animation.txt
	

ReportGrid.lineChart
====================

``ReportGrid.lineChart(selector el, parameters object) void``

The ``lineChart`` can be used to build standard line charts, area charts stacked or not. It requires at least two axes but can support more.

**options:**

``displayarea`` : bool
	States if an area shape must be rendered below the line chart or not.
``effect`` : string
	States the effect to apply to the line(s) of the chart. The parameters in curly brackets are optional and refine the style.
	 * "noeffect" : 
		The line is rendered with a solid color.
	 * "dropshadow" :
		The line is rendered with a background shadow.
	 * "dropshadow:{offsetx}" :
	 * "dropshadow:{offsetx}:{offsety}" :
	 * "dropshadow:{offsetx}:{offsety}:{levels}" :
		The optional parameters are used to set the offset (x and y) of the shadow and of how many degrees of gray the shadow is composed.
	 * "gradient" :
		The line is rendered with a gradient from the middle outwards.
	 * "gradient:{lightness}" : 
	 * "gradient:{lightness}:{levels}" : 
		The ``lightness`` parameter is used to state how brigther (or darker) the end of the gradient will be and the ``levels`` parameterd states the number of steps that form the gradient.
``symbol`` : string OR function(object datapoint, object stats) string
	Each datapoint in the line charts can be associated to an optional symbol. The symbol can be described statically using a string or using a function. The symbol must be expressed in SVG PATH format. There is a practical function ``ReportGrid.symbol.get()`` to quickly build symbols.
``symbolstyle`` : function(object datapoint, object stats) string
	If symbols are added to the lines you can use ``symbolstyle`` to render a custom style for each of them. The style produced by the custom function must be a string in CSS format.
``y0property`` : string
	This parameter is used to determine what value in the datapoint must be used to stack values.
	Usually that value must be calculated transforming the datapoints from your query.
``interpolation`` : string
	Linecharts are not interpolated by default (equivalent to "linear" interpolation) but can be smoothed or transformed using one of the following values: ``"basis"``, ``"basisopen"``, ``"basisclosed"``, ``"cardinal"``, ``"cardinal:{value}"``, ``"cardinalopen"``, ``"cardinalopen:{value}"``, ``"cardinalclosed"``, ``"cardinalclosed:{value}"``, ``"monotone"``, ``"stepafter"``, ``"stepbefore"``, ``"linear"``
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
The pie chart requires two axes, one for the slicing and one to set the slice size.

**options:**

``animation`` : object animation options
	Defines the `piechart animation behavior`_ of the visualization.
``click`` : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
``dontfliplabel`` : bool
	Determines if the labels on the datapoints should always be set so that even if they are inclined they never end flipped.
``effect`` : string
	The effect to apply to each slice in a pie or donut chart.
	 * ``"noeffect"`` : 
		The pie slices are filled with a solid color.
	 * ``"gradient"`` : 
		Applies a radial gradient.
	 * ``"gradient:{lightness}"`` : 
		Same effect as above but with the control over the intensity of the color variation.
``innerradius`` : float
	Inner radius is zero by default but can be any value between 0 and 1 to build a donut chart.
``label`` : object label options
	Configuration object for text labels.
``labelorientation`` : string
	Label orientation is used to control the behavior of label rotation according to their position in the chart.
	 * ``"fixed"`` or ``"horizontal"``:
		No rotation.
	 * ``"fixed:{angle}"`` :
		Rotation fixed at a certain angle.
	 * ``"ortho"`` or ``"orthogonal"`` : 
		Labels are aligned orthogonally to their radius.
	 * ``"align"`` or ``"aligned"`` : 
		Labels have the same angle as their radius have.
``labelradius`` : float
	States where the datapoint labels are positioned.
``outerradius`` : float
	The outer radius is useful when you want accentuated transitions (animations) that can bleed the border of the charts. Another use in combination with ``innerradius`` is to nest two or more donut charts in the same visualization and avoid overlappings.
``overradius`` : float
	The over radius is used to control the slice size when animation is turned on.
``tooltipradius`` : float
	Controls the position of the tooltip balloon when activated and the mouse is positioned over a slice.
``sort`` : function(object datapoint, object datapoint) int
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

``click`` : function(object datapoint, object stats) void
	A handler function that is executed when the user click or touches a datapoint.
``columnaxes`` : int
	This parameter controls how many dimensions of the query are grouped as columns.
	If you have three axis and you want to group just the first on the coloumns and the remaining two on the rows, the ``columnaxes`` value must set to ``1`` (default value). The same visualization can have 2axes on the columns and 1 axis on the rows, setting the value to 1.
``displaycolumntotal`` : bool
	Whether or not to display the totals at the bottom of the columns.
``displayheatmap`` : bool
	Whether or not to display a background color below each cell whose intensity in the color scale is based on the value of the cell itself.
``displayrowtotal`` : bool
	Whether or not to display the totals at the right of the rows.
``endcolor`` : string
	Color for the background cell of highest value in the table. This option is used only if ``displayheatmap`` is set to true.
``label`` : object pivottable label options
	Configuration object for text labels.
``startcolor`` : string
	Color for the background cell with a value of zero. This option is used only if ``displayheatmap`` is set to true.


.. include:: visualization-api-v1-options-label.txt

``axisvalue`` : function(any value, string type) string
	The label to apply to the individual values of the grouped dimensions..
``total`` : function(float value, object stats) string
	Function to control the label in the total cell.
``totalover`` : function(float value, object stats) string
	Function to control the label when the mouse goes over a total cell.

.. include:: visualization-api-v1-options-label-axis.txt

ReportGrid.sankey
=======================

``ReportGrid.sankey(selector el, parameters object) void``

Sankey diagrams are a type of flow diagram in which nodes are connecte by arrows whose width is proportional to the flow quantity. They are typically used to visualize transfer of quantities between processes. They can also be seen as bi-dimensional funnel charts.
The visualization is composed of nodes rendered as rectangle whose height is proportional to their weight in the graph. The nodes are layed on a set of vertical lines (those lines can be styled using CSS rules and are hidden by default) called layers. The nodes are automatically associated to the layers unless you provide a ``layoutmap`` definition that exactly the location of the nodes. If the ``layoutmap`` is not defined, the visualization will use the algorithm specified in ``layoutmethod``.
Complex layouts may use dummy nodes to fill the graph in a way that edges are correctly positioned. The dummy node id is by convention prefixed with the pund "#" character. Dummy nodes do not render labels or mouse over.
The sankey requires only one axis to quantify each edge node but it expects the data points to be formatted in a certain way. All the edge data points must contain a pair of ``head``/``tail`` fields that identify the nodes uniquely and a weight field that matches the mandatory axis type. Optionally, you can pass a set of node data points that are used to set the absolute weight of each node (that value can be greater than the sum of each edge flowing in or out). Those nodes must have the field `ìd`` and a weight field matchind the mandatory axis type.


``backedgespacing`` : number
	The vertical spacing between roll-back edges
``click`` : function(datapoint, stats) void
	A function that is invoked when the user clicks on a node.
``clickedge`` : function(object, stats) void
	A function that is invoked when the user clicks on an edge.
``displayentry`` : bool or function(datapoint) bool
	If a function (or static value) is provided, the entry edge will be displayed conditionally (if it exists).
``displayexit`` : bool or function(datapoint) bool
	If a function (or static value) is provided, the exit edge will be displayed conditionally (if it exists).
``dummyspacing`` : number
	A vertical spacing reserved to dummy nodes.
``edgeclass`` : string or function(datapoint, stats) void
	Additional class to append to the edges. It is used mainly for styiling.
``extraheight`` : number
	The height in pixel for the entry/exit edges.
``extraradius`` :  : number
	The minimum radius in pixel for the entry/exit edges.
``extrawidth`` : number
	The max width in pixel for the entry/exit edges.
``imageheight`` : number
	The height of the images associated with the nodes.
``imagepath`` : function(datapoint, stats) : string
	A function that given the datapoint data return an absolute URL to an image file. The image can be in PNG or JPG format. The image will be automatically resized to the `ìmagewidth`` and ``imageheight`` values.
``imagespacing`` : number
	The space in pixel between the image associated to a node and to the node itself.
``imagewidth`` : number
	The width of the images associated with the nodes.
``label`` : object sankey label options
	Configuration object for text labels.
``labelnodespacing`` : number
	Space between the label over the nodes and the node itself.
``layerwidth`` : number
	The widht of the node rectangles in pixels. The remaining space is occupied by the edges.
``layoutmap`` : object { layers : array of array of strings, dummies : array of array of strings }
	The layers property must contain an array of array of node identifiers. The first array dimension matches the layers in the sankey, the inner array represents the disposition of the nodes in each layer from top to bottom. The second field contains a mapping of how the dummy nodes connect the real nodes. Each array in the array must contain a path from a concrete node to another that wrap one or more dummy nodes.
``layoutmethod`` : string ("sugiyama" - default - or "weightbalance")
	When a ``layoutmap`` is not provided states the method for laying out the nodes on the sankey. More methods will be added in the future.
``nodeclass`` : string or function(datapoint, stats) void
	Additional class to append to the nodes. It is used mainly for styiling.
``nodespacing`` : number
	Vertical spacing in pixels between two nodes. Note that if you use images or labels on top of the nodes, the ``nodespacing`` value should be greater than both or some overlapping might happen.

.. include:: visualization-api-v1-options-label.txt
.. include:: visualization-api-v1-options-label-sankey.txt

ReportGrid.scatterGraph
=======================

``ReportGrid.scatterGraph(selector el, parameters object) void``

A scatter graph is a visualization to display data for two variables in a data set (more variables can be addressed using size and colors). The data is displayed is displayed as a collection of points or symbols.
The scatter graph requires two axes, one for the X axis and one for the Y axis.

**options:**

``symbol`` : string OR function(object datapoint, object stats) : string
	Each point in a scatter graph should be associated to a symbol. Each symbol can be rendered indivisually to have a distinct shape or not. The ``symbol`` can be a static string or a function that returns such string. The string represents a SVG path. You can easily create a SVG path using `ReportGrid.symbol`_.
``symbolstyle`` : string OR function(object datapoint, object stats) : string
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
The scatter graph requires two axes, one for the X axis and one for the Y axis.

**options:**

``effect`` : string
	The effect to apply to each slice in a pie or donut chart.
	 * ``"noeffect"`` : 
		No effect is applied to the stream bands and they are filled with a solid color.
	 * ``"gradient"`` : 
		A simple linear and vertical gradient that fills the stream band.
	 * ``"gradient:{lightness}"`` : 
		Same as above but with control over the lightness or darkness of the stop color in the gradient.
	 * ``"gradienth"`` : 
		The band are filled with a complex horizontal and linear gradient that enhance or reduce the color based on the values in the graph.
	 * ``"gradienth:{lightness}"`` : 
		Same as above but with control over the lightness or darkness of the stop color in the gradient.
``interpolation`` : string
	Controls how the lines in the stream graph are interoilated, the allowed values are: "basis", "basisopen", "basisclosed", "cardinal", "cardinalopen", "cardinalclosed", "cardinal:{value}", "cardinalopen:{value}", "cardinalclosed:{value}", "monotone", "stepafter", "stepbefore", "linear".
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

ReportGrid.chart
================

``ReportGrid.chart(selector el, parameters object, string type) void``

The ``ReportGrid.chart`` method is a generic function to build a visualization programmatically (passing the type of visualization as the third parameter). The ``type`` argument can take any of the names of the methods described above all lower case.

The following two declarations are basically equivalent:

::

	ReportGrid.lineChart("#chart", { ...});
	
	ReportGrid.chart("#chart", { ...}, "linechart");

-----------------------
Stats
-----------------------

Some custom function receive as a parameter a ``stat`` object. The stats object contains statistic information about a dataset. The stats are always relative to a specific axis.

``min`` :
	The smallest value in the data set.
``max`` :
	The biggest value in the data set.
``tot`` : (only for numeric values)
	The summatory of all the values in the dataset.
``count`` : 
	The number of values in the data set.
``values`` : 
	The unordered set of distinct values in the dataset.

--------------------------
ReportGrid Utilities
--------------------------

The reportgrid-viz.js file adds a lot of fields to the ReportGrid object; most of them are related to visualizations but some are generic utility functions and variables.


ReportGrid.charts.ready
=======================

``ReportGrid.charts.ready(callback function) void``

The passed callback function is called once when all the visualizations on the page are rendered for the first time.

ReportGrid.compare
==================

``ReportGrid.compare(any a, any b) string``

It takes two argument and returns an integer as the result of their comparison.

examples:

::
	
	console.log(ReportGrid.compare("b", "a"));
	// outputs: 1
	console.log(ReportGrid.compare(1, 2));
	// outputs: :1

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

	console.log(ReportGrid.date.snap(new Date("2011:08:01 05:46:00"), "day"))
	// outputs the timestamp for "2011:08:01 00:00:00"

ReportGrid.dump
===============

``ReportGrid.dump(any value) string``

It takes one argument of any type and returns a string out of it. It is very convenient to serialize and object in a readable string.

examples:

::
	
	console.log(ReportGrid.dump(123456.789));
	// outputs: "123,456.78"
	console.log(ReportGrid.dump(new Date("2011:09-23")));
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
* formats integer and float values in range like texts (e.g. ``income_1000:10000`` becomes ``Income 1.000:10.0000``)

ReportGrid.info.charts.version
==============================

``ReportGrid.info.charts.version string``

Contains the version of the reportgrid-charts.js in use.

examples:

::
	
	console.log(ReportGrid.info.charts.version);
	// outputs: "1.0.1.73" <-- this value obviously changes with new releases

ReportGrid.math.random
======================

``ReportGrid.math.random() float``

Returns a random float number between 0 and 1. Note that the random number generator is actually a *pseudo* random number generator. That means that if the generator is used twice in two different contexts (for example reloading the HTML/JS page) it will generate the same sequence of numbers.

ReportGrid.symbol
=====================

``ReportGrid.symbol(string type, optional int size) string``

It takes the type of symbol and its size (area in pixel) and returns its SVG Path representation.
The symbols you can draw are: "triangleDown", "triangleUp", "square", "diamond", "cross", "circle", "arrowUp", "arrowDown", "arrowDownWide", "arrowRight", "arrowLeft", "star".
If size is not specified 100 is the default value.

example:

::
	
	console.log(ReportGrid.symbol("star"));
	// outputs: "M0,-8.97635689348711L2.1184202268629577,-2.917315990383311 ..."

.. _css palettes: http://api.reportgrid.com/css/colors/