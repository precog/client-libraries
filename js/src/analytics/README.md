Analytics library
=================

The analytics library automates the process of monitoring events on a page.
Usage involves loading jQuery, loading the ReportGrid client core, and then
loading the analytics script:

    <script src='/path/to/jquery.js'></script>
    <script src='/path/to/reportgrid-core.js'></script>
    <script src='/path/to/reportgrid-analytics.js'></script>

Production builds of `reportgrid-analytics.js` are located in the `build/`
directory.

Usage
=====

The analytics script is configured by passing parameters in querystring format
in the script's URL. For example:

    <script src='/path/to/analytics.js?option=value&option=value'></script>

The list of available options is documented in `reportgrid-analytics.js`.

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

The following features are either unimplemented or are in progress:

1. Attention tracking
2. OS and platform platform tracking
3. Bounce tracking (this may be covered by visits, unique visits, and referrers)
