Analytics library
=================

The analytics library automates the process of monitoring events on a page.
Usage involves loading jQuery, loading the ReportGrid client core, and then
loading the analytics script:

    <script src='http://ajax.googleapis.com/ajax/libs/jquery/1.4.0/jquery.min.js'></script>
    <script src='http://api.reportgrid.com/js/reportgrid-core.js?tokenId={TOKEN_ID}'></script>
    <script src='http://api.reportgrid.com/js/reportgrid-analytics-src.js'></script>

Where {TOKEN_ID} is the token id used to track the events.
	
Production builds of `reportgrid-analytics*.js` are located in the `build/`
directory.

A single-file build is also available. This includes a pre-trimmed version of
jQuery 1.4.0, a copy of reportgrid-core, and a copy of reportgrid-analytics, all
minified together:

    <script src='http://api.reportgrid.com/js/reportgrid-analytics-all.min.js?tokenId={TOKEN_ID}'></script>

When using this script, query-string configuration options for both scripts
should be combined. For example, if `reportgrid-core.js` is configured with
`tokenId=ABCDEF` and `reportgrid-analytics.js` is configured with
`interaction=true`, then the script URL would look like this:

    <script src='...min.js?tokenId=ABCDEF&interaction=true'></script>

Usage
=====

The analytics script is configured by passing parameters in querystring format
in the script's URL. For example:

    <script src='/path/to/analytics.js?option=value&option=value'></script>

These options are supported:

* `pageEngagement`: `queueing` (default) | `polling` | `none`

Determines the method used to track the amount of time a user spends on a
page. By default (queueing), the user's engagement time is recorded but not
sent immediately. When the user visits another page on your website, their
last recorded time is sent. This has the advantage that it minimizes the
number of API calls made while still providing engagement data.

Another option is to use polling, which emits API requests at doubling
intervals starting with one second. The advantage of this approach is that you
get real-time engagement data (especially relevant for users who view only one
page) at the cost of using more API calls.

Engagement tracking can be disabled by setting the option to `none`.

* `crossdomain`: `true` (default) | `false`

If true, uses Flash (on devices where Flash is available) to cookie a user
across multiple domains that are logically grouped. This impacts unique/repeat 
visitor events. Sites are considered connected if they share the same 
`cookieNamespace` setting.

* `cookieNamespace`: `<identifier>` (`all` by default)

If crossdomain cookies are used, this setting impacts their scope. The owner of
multiple sites should choose a namespace unique to their organization and reuse
this namespace on each site. Sites sharing the same namespace will share visitor
uniqueness information.

* `interaction`: `true` (default) | `false`

If true, any click or enter-key press anywhere in the document will be
reported. A short string identifying the element is sent with each event.

* `attention`: `true` | `false` (default)

If true, each element is broken into a 10x10 logical grid, and every time the
user's mouse crosses a tile boundary a new `attention` event will be created.
This can be useful for building heat-maps of user activity.

* `attentionResolution`: `1`, `2`, ..., `10` (default), ...

Customizes the number of grid cells used for attention tracking. Greater values
result in more precision. So, for instance, setting this to 5 will result in a
5x5 attention grid rather than the default 10x10.

* `scrolling`: `true` | `false` (default)

If true, the user's vertical scrolling will be tracked. This involves breaking
the document into ten evenly-sized strips and reporting when lower ones become
visible to the user.

Implementation status
=====================

The following features are implemented:

1. Click tracking
2. Page load / visit tracking
3. Unique visitor detection
4. Repeat visitor detection
5. Client timezone tracking
6. Referrer tracking
7. Engagement tracking (queueing)
8. Form submission tracking
9. Scroll tracking
10. Browser version tracking
11. Search engine keyword tracking
12. Interaction tracking
13. Engagement / interaction totaling
14. Engagement tracking (polling)
15. Attention tracking
16. OS and platform platform tracking
17. Bounce tracking
