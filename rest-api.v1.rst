=======================
The ReportGrid REST API
=======================

.. contents:: API Methods

--------
Overview
--------

The ReportGrid platform exposes a `REST API <http://en.wikipedia.org/wiki/Representational_State_Transfer>`__ from 
which you can:

- record data points (events and associated properties)
- perform queries, like retrieving counts and time series data
- manage tokens

In addition to the REST API, which may be used from any language and platform that supports HTTP connections, we offer 
a growing number of client libraries designed to make using ReportGrid as simple as possible.

The REST API only supports JSON and adheres strictly to the HTTP specification. All requests should have a Content-Type
header equal to **application/json** and use token authentication.

The API root is located at the following URL:

http://api.reportgrid.com/services/analytics/v1

--------------------
Fundamental Concepts
--------------------

In order to successfully use the ReportGrid API, you should be familiar with a few key concepts: data points, tags, the 
virtual file system, and tokens. 

Events and Data Points
======================

ReportGrid is designed to aggregate, analyze, and visualize *events* and *data points*. We'll begin with discussing 
events, and then explore data points, which are simply a generalization of events.

An event is anything that happens at a particular point in time. For example, the delivery of an email, the withdrawal 
of cash from an ATM, a click on a web page, or the processing of a census form.

Events are identified by *name* and *properties*: the event name is just a string that uniquely identifies the event 
(usually describing what occurred, such as "push to mobile device", while properties are additional metadata that 
describe details of the event (such as the handset, carrier, location, and type of the push).

Time is implicitly a property of every event, and if you do not specify a time for an event, ReportGrid uses the time 
at the moment when the event is tracked through the API.

Data points are very similar to events, but dispense with the restriction on time being part of an event; a data point
is simply a name coupled with associated metadata.

Tags
====

Tags provide a mechanism that can be used to specify special kinds of event metadata where the system can take 
advantage of additional information to improve searches. For example, timestamps and geolocation information 
are kinds of data that should be explicitly specified using tags so that they are not treated as opaque values. 
Special syntax for identifying tagged properties will be listed below in the tracking section of the API.

Virtual File System
===================

Whenever you track data or query using ReportGrid, you must specify a *path* in the ReportGrid virtual file system. 
The Virtual File System is much like a real file system: there is a root directory, and any directory can contain other 
directories, as well as data.

You use the Virtual File System to organize data, for example, tracking events from different customers in different 
folders. Most client libraries allow you to automatically roll-up data to higher-level folders in the file system, so 
you can look at data at differing levels of granularity.

Token
=====

Tokens are how you control access to resources in ReportGrid.

Every ReportGrid account is assigned a master token, which can be used to create other tokens. Tokens are always 
associated with a particular path in the virtual file system, and have access to both that path, and to all of its 
descendants.

Tokens may have read, write, and share permissions, but they cannot have more permissions than their parent token (the 
token used to create them). Typically, you will store one or more tokens for each of your customers, and each token 
will permit access to a customer-specific path in the Virtual File System (e.g. /customers/jdoe/).

All requests to the REST API require token authentication, which is accomplished by using a **tokenId=[guid_tokenId]** 
query string parameter.

-----------
API Methods
-----------

In the following documentation, each API method is specified in a table which describes the request method, the url 
pattern to which the request should be sent, and the format that the body of the request should adhere to. All requests
should specify the Content-Type header to have the value **application/json**.

In the sample requests below, all elements enclosed in parenthesis indicate configurable elements; opitonal elements
are denoted -(elem) and elements where a selection may be made are labeled as (option1 | option2 | ...).

Token Manipulation
==================

ReportGrid's tokens API is located at:

http://api.reportgrid.com/services/analytics/v0/tokens/?tokenId=(guid)

Token Creation
--------------

Tokens can be created by POSTing to the ReportGrid tokens API with a JSON object that describes the path, permissions, 
and limits of the token.  A descendent token's permissions and limits cannot exceed (but may equal) the parent's. 

+--------------------+-------------------------------------------------------+
| method             | POST                                                  |
+--------------------+-------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)"                                |
+--------------------+-------------------------------------------------------+
| body               | A JSON object where each property represents an event |
|                    | to be tracked. See the `Querying`_ section for an     |
|                    | example.                                              |
+--------------------+---------+----------------------------------+----------+
| request parameters | tokenId | (parent token id)                | required |
+--------------------+---------+----------------------------------+----------+

::

  {
    "path": "/startups/launches",
    "permissions": {
      "read": true,
      "write": true,
      "share": false
    },
    "expires": 9223372036854775807,
    "limits": {
      "order": 2,
      "limit": 2,
      "depth": 2
    }
  }


.. csv-table:: Token API

   "rootPath", "The path, relative to the parent's path, that will be associated with this tokenId"
   "expires",  "The expiration date of the token, measured in milliseconds from the start of the Unix Epoch, UTC time"
   "order",    "The maximum number of metadata properties accessible in an intersection query"
   "limit",    "The maximum number of metadata properties associated with an event"
   "depth",    "The maximum depth of the metadata object associated with an event"

Recording Data
==============

Two different methods are available for recording data points. In both cases, all that you need to do is send a POST 
request to the virtual filesystem path at which you desire to store the data; the two methods differ only with respect 
to whether or not the data is automatically timestamped if no timestamp is explicitly specified.

+--------------------+-------------------------------------------------------+
| method             | POST                                                  |
+--------------------+-------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)"                                |
+--------------------+-------------------------------------------------------+
| body               | A JSON object where each property represents an event |
|                    | to be tracked. See the `Querying`_ section for an     |
|                    | example.                                              |
+--------------------+---------+----------------------------------+----------+
| request parameters | tokenId | (your token id)                  | required |
|                    +---------+----------------------------------+----------+
|                    | count   | (number of occurences to record) | optional |
+--------------------+---------+----------------------------------+----------+

Here, the body of the request is a JSON object. Each field in the object corresponds to an event being tracked; this
provides you with the ability to track multiple events at once. For each property in the JSON object, the name of the 
property will used as event identifier. The value of the property can be any valid JSON structure (usually a JSON 
object).

You will notice that in the example above, a couple of fields are prefixed with the "#" character. Properties whose
names start with "#" identify so-called "tag properties" of the object. Tagged properties are used to provide 
information that needs to be treated specially by the system. For example, time and geolocation are specified using the
#timestamp and #location properties. These tags can have the following formats: :: 

  // tell the system to autogenerate a timestamp for the event (if using the basic tracking api, this can be omitted)
  {"myEvent": {"#timestamp": true }} 
  {"myEvent": {"#timestamp": "auto" }} 
  
  // tell the system to use a specific timestamp
  {"myEvent": {"#timestamp": 1315321200000}}
  
  // tell the system to automatically determine a location for the event using IP resolution;
  // you may use the X-Forwarded-For header to specify an IP address if the event is not being tracked
  // directly from a client machine
  {"myEvent": {"#location": true}} 
  {"myEvent": {"#location": "auto"}} 

  // explicitly identify a location
  {"myEvent": {"#location": ["usa", "usa/colorado", "usa/colorado/boulder"]}} 
  {"myEvent": {"#location": {"country": "usa", "state": "usa/colorado", "city": "usa/colorado/boulder"}}} 

The "store" call is identical to the base tracking call specified above, with a couple of very minor differences. 
First, the URL pattern is instead ``(API ROOT)/vfs/store(path)``; second, events sent to the store path are not 
automatically timestamped if the #timestamp tag is absent.

Querying
========

ReportGrid makes it easy to retrieve time series or aggregated metrics on the fly. Following REST conventions, all you 
have to do is perform an HTTP request to the location of the metric you want. Most queries can be done using simple 
GET requests, although a couple of more complex queries require POST.

The following examples will be based upon an event having been tracked by sending a POST to
http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D
with the header (Content-Type = application/json) and the body content: ::

  {
    "customer_support" : {
      "type": "call",
      "duration": 147,
      "representative": "Alice Brewer",
      "resolution": {
        "type": "escalated",
        "to":   "Candice Deming"
      },
      "#timestamp": (see below),
      "#location": (see below)
    }
  }

Anywhere that you see "(path)" below it is referring to the path at which the event was stored; in this example, that
would be "mysupportco/myclient1".


Explore
-------

You can explore the virtual filesystem by simply sending a GET request to any path that you're interested in. Paths
have two different components: the virtual filesystem path, and then the path to the property you're interested in
within the event object. The two types of the path component can be distinguished by the path separator; in the former 
case, it's a "/", while in the latter it is "." to mimic retrieving properties from an object graph.

+--------------------+-----------------------------------------------------------------------------+
| method             | GET                                                                         |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)                                         |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | your token id                    | required                      |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | JSON array of child path element names.                                     |
+--------------------+-----------------------------------------------------------------------------+

For example, given the above tracked event, a request to 
http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.resolution?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D
would return the value ``[".type", ".to"]``

Variable Count
--------------

The simplest query that can be performed simply retrieves the number of times a given event (or property of an event)
was observed. 

+--------------------+-----------------------------------------------------------------------------+
| method             | GET or POST                                                                 |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/count                                   |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | your token id                    | required                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | start    | starting timestamp               | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | end      | ending timestamp                 | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | location | location to seach                | required if used in tracking  |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | Numeric                                                                     |
+--------------------+-----------------------------------------------------------------------------+

If you simply want to count the nuber of occurrences of an event, all you need to specify of the object path is the
event type: 
http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support/count?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

If you want to count the nuber of occurrences of a property of an event, you can walk down the object graph as shown
here. This is useful in the case that not every event has the same structure; for example, the 
.customer_support.resolution.to property may only be present when the resolution type is "escalated".
http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.resolution.to/count?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

Variable Series
---------------

ReportGrid provides special convenience syntax and handling for time-series queries. In this example, we return the 
hourly counts of escalated customer support events.

+--------------------+-----------------------------------------------------------------------------+
| method             | GET or POST                                                                 |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/series/hour                             |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | your token id                    | required                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | start    | starting timestamp               | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | end      | ending timestamp                 | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | location | location to seach)               | required if used in tracking  |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | JSON array of arrays where each inner array has two elements: the first     |
|                    | element is an object identifying the key that the value was stored          |
|                    | against, and the second element is a count. If, for example, you are        |
|                    | querying by both time and location, the format will be something like:      |
|                    |                                                                             |
|                    | ``[[{"timestamp": 1315454910389, "location": "usa"}, 132], ...]``           |
+--------------------+-----------------------------------------------------------------------------+

In addition to "hour" in the example above, you could also use "minute", "day", "week", "month", and "year". 

Series of Summary Statistics
----------------------------

The queries for these series are identical to above, but admit one additional path component describing the type
of summary statistics to return.

+--------------------+-----------------------------------------------------------------------------+
| method             | GET                                                                         |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/series/hour/means                       |
+                    +-----------------------------------------------------------------------------+
|                    | (API ROOT)/vfs/(path)/(object path)/series/hour/standardDeviations          |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | (your token id)                  | required                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | start    | starting timestamp               | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | end      | ending timestamp                 | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | location | (location to seach)              | required if used in tracking  |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | Same as for count series.                                                   |
+--------------------+-----------------------------------------------------------------------------+

Length
------

This query returns the number of distinct values a property was observed to take on.

+--------------------+-----------------------------------------------------------------------------+
| method             | GET                                                                         |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/length"                                 |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | (your token id)                  | required                      |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | Numeric                                                                     |
+--------------------+-----------------------------------------------------------------------------+

Example: 

http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.resolution.to/length?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

Values
------

These queries are used to determine what values a property was recorded to have.

+--------------------+-----------------------------------------------------------------------------+
| method             | GET                                                                         |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/values"                                 |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | (your token id)                  | required                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | start    | starting timestamp               | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | end      | ending timestamp                 | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | location | (location to seach)              | required if used in tracking  |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | JSON array of arrays where each inner array has two elements: the first     |
|                    | element is a value, and the second is a count of the number of times that   |
|                    | value was observed.                                                         |
|                    |                                                                             |
|                    | ``[["escalated", 132], ["resolved", 175]]``                                 |
+--------------------+-----------------------------------------------------------------------------+

Example: 

http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.type/values?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

Histogram
---------

Histogram queries return a histogram of the counts of occurrences of the values of a property. 

+--------------------+-----------------------------------------------------------------------------+
| method             | GET                                                                         |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/histogram                               |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | your token id                    | required                      |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | JSON array of arrays where each inner array has two elements: the first     |
|                    | element is a value, and the second is a count of the number of times that   |
|                    | value was observed.                                                         |
|                    |                                                                             |
|                    | ``[["call", 132], ["email", 175]]``                                         |
+--------------------+-----------------------------------------------------------------------------+

Two variants on the histogram query are also available for properties that may take on very large numbers of values;
these return the top and bottom ``n`` results relative to the count of the keys. 

Example: 

http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.type/histogram?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

+--------------------+-----------------------------------------------------------------------------+
| method             | GET                                                                         |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/histogram/top/``n``                     |
|                    +-----------------------------------------------------------------------------+
|                    | (API ROOT)/vfs/(path)/(object path)/histogram/bottom/``n``                  |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | your token id                    | required                      |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | Same as above.                                                              | 
+--------------------+-----------------------------------------------------------------------------+

Examples: 

http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.type/histogram/top/20?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.type/histogram/bottom/20?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

Value Counts
------------

This is similar to the `Variable Count`_ query described above, but instead of counts of variable occurrences, it 
returns the count of times a given value occurred, either globally or within a specified time period.

+--------------------+-----------------------------------------------------------------------------+
| method             | GET or POST                                                                 |
+--------------------+-----------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/values/(URL-encoded JSON value)/count   |
+--------------------+----------+----------------------------------+-------------------------------+
| request parameters | tokenId  | your token id                    | required                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | start    | starting timestamp               | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | end      | ending timestamp                 | optional                      |
|                    +----------+----------------------------------+-------------------------------+
|                    | location | (location to seach)              | required if used in tracking  |
+--------------------+----------+----------------------------------+-------------------------------+
| response format    | Numeric                                                                     |
+--------------------+-----------------------------------------------------------------------------+

Values can only be retrieved for 'leaves' of the JSON object graph. Thus, only string, numeric, and boolean values
may be supplied, and values must be URL-encoded to escape illegal characters.

Example:

http://api.reportgrid.com/services/analytics/v1/vfs/mysupportco/myclient1/.customer_support.type/values/call/count?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

Value Series
------------

This is similar to the `Variable Series`_ query described above, but instead of counts of variable occurrences, it 
returns the counts of times a given value occurred in a specified time period. 

+--------------------+-----------------------------------------------------------------------------------------------+
| method             | GET or POST                                                                                   |
+--------------------+-----------------------------------------------------------------------------------------------+
| url pattern        | (API ROOT)/vfs/(path)/(object path)/values/(URL-encoded JSON value)/series/hour               |
+--------------------+----------+----------------------------------+-------------------------------------------------+
| request parameters | tokenId  | your token id                    | required                                        |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | start    | starting timestamp               | optional                                        |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | end      | ending timestamp                 | optional                                        |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | location | location to seach)               | required if used in tracking                    |
+--------------------+----------+----------------------------------+-------------------------------------------------+
| response format    | JSON array of arrays where each inner array has two elements: the first                       |
|                    | element is an object identifying the key that the value was stored                            |
|                    | against, and the second element is a count. If, for example, you are                          |
|                    | querying by both time and location, the format will be something like:                        |
|                    |                                                                                               |
|                    | ``[[{"timestamp": 1315454910389, "location": "usa"}, 132], ...]``                             |
+--------------------+-----------------------------------------------------------------------------------------------+

In addition to "hour" in the example above, you could also use "minute", "day", "week", "month", and "year". 

Advanced Searches
-----------------

+--------------------+-----------------------------------------------------------------------------------------------+
| method             | POST                                                                                          |
+--------------------+-----------------------------------------------------------------------------------------------+
| url pattern        | (API ROOT)/search                                                                             |
+--------------------+-----------------------------------------------------------------------------------------------+
| body               | See below                                                                                     |
+--------------------+----------+----------------------------------+-------------------------------------------------+
| request parameters | tokenId  | your token id                    | required                                        |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | start    | starting timestamp               | optional, may be specified in the request body  |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | end      | ending timestamp                 | optional, may be specified in the request body  |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | location | location to seach                | required if used in tracking,                   |
|                    |          |                                  | may be specified in the request body            |
+--------------------+----------+----------------------------------+-------------------------------------------------+
| response format    | In the case of a count query, the return value is simply numeric, as elsewhere.               |
|                    |                                                                                               |
|                    | In the case of a series query, the result is again a JSON array of arrays where each inner    | 
|                    | array has two elements: the first element is an object identifying the key that the value was | 
|                    | stored against, and the second element is a count. If, for example, you are                   |
|                    | querying by both time and location, the format will be something like:                        |
|                    |                                                                                               |
|                    | ``[[{"timestamp": 1315454910389, "location": "usa"}, 132], ...]``                             |
+--------------------+-----------------------------------------------------------------------------------------------+

Unlike the other queries above, these queries are executed using POST requests and have a relatively complex request
body format, examples of which are shown below. Both count and time series queries can be run using the search
directive. Both examples below could be sent to this url:
http://api.reportgrid.com/services/analytics/v1/search?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

Count Example: ::

  {
    "select": "count",
    "from": "/mysupportco/myclient1",
    "where": [
      {"variable": ".customer_support.type", "value: "call"},
      {"variable": ".customer_support.resolution.type", "value: "escalated"}
    ],
    "start": 1315454900000,
    "end": 1315454910000,
    "location": "usa/colorado"
  }

Series Example: ::

  {
    "select": "series/minute",
    "from": "/mysupportco/myclient1",
    "where": [
      {"variable": ".customer_support.type", "value: "call"},
      {"variable": ".customer_support.resolution.type", "value: "escalated"}
    ],
    "start": 1315454900000,
    "end": 1315454910000,
    "location": "usa/colorado"
  }

Intersection Queries
--------------------

Intersection queries are used to retrieve multidimensional data structures similar to those used in a pivot table. 
Instead of, for example, a simple time series, you can retrieve a set of time series where each time series 
corresponds to the result of an advanced search.

+--------------------+-----------------------------------------------------------------------------------------------+
| method             | POST                                                                                          |
+--------------------+-----------------------------------------------------------------------------------------------+
| url pattern        | (API ROOT)/intersect                                                                          |
+--------------------+-----------------------------------------------------------------------------------------------+
| body               | See below                                                                                     |
+--------------------+----------+----------------------------------+-------------------------------------------------+
| request parameters | tokenId  | your token id                    | required                                        |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | start    | starting timestamp               | optional, may be specified in the request body  |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | end      | ending timestamp                 | optional, may be specified in the request body  |
|                    +----------+----------------------------------+-------------------------------------------------+
|                    | location | location to seach                | required if used in tracking,                   |
|                    |          |                                  | may be specified in the request body            |
+--------------------+----------+----------------------------------+-------------------------------------------------+
| response format    | In the case of a count query, the result is a JSON array of arrays where each inner array     |
|                    | represents an n-tuple (value1, value2, ..., valuen, count) where value1..valuen take on       |
|                    | the values of variables specified in the query, and the count returned represents the number  |
|                    | of times this combination of values was observed in the specified time period. For example,   |
|                    |                                                                                               |
|                    | ``[["call", "escalated", 12], ["call", "resolved", 8], ["email", "escalated", 3], ...]``      |
|                    |                                                                                               |
|                    | Here, the order of the elements value1..valuen is the same as the order in which variables    |
|                    | were specified in the query.                                                                  |
|                    |                                                                                               |
|                    | In the case of a series intersection query, the result is a 3-dimensional JSON array similar  | 
|                    | to the above, but where instead of a count, the final member of each tuple is a time series   |
|                    | as described in `Value Series`_ above.                                                        |
+--------------------+-----------------------------------------------------------------------------------------------+

Both examples below could be sent to this url:
http://api.reportgrid.com/services/analytics/v1/intersect?tokenId=A3BC1539-E8A9-4207-BB41-3036EC2C6E6D

Count Example: ::

  {
    "select": "count",
    "from": "/mysupportco/myclient1",
    "properties": [
      {"variable": ".customer_support.type", "limit": 10, "order": "descending"},
      {"variable": ".customer_support.resolution.type", "limit": 10, "order": "ascending"}
    ],
    "start": 1315454900000,
    "end": 1315454910000,
    "location": "usa/colorado"
  }

Series Example: ::

  {
    "select": "series/minute",
    "from": "/mysupportco/myclient1",
    "where": [
      {"variable": ".customer_support.type", "limit": 10, "order": "descending"},
      {"variable": ".customer_support.resolution.type", "limit": 10, "order": "ascending"}
    ],
    "start": 1315454900000,
    "end": 1315454910000,
    "location": "usa/colorado"
  }

In both of these examples, the query engine will find the top 10 counts of customer support type values and the 
bottom 10 counts of customer support resolution type values, and will return a matrix of these as described above.
