Analytics library
=================

The analytics library automates the process of monitoring events on a page. Two
scripts are provided to do this. One, `reportgrid-analytics.js`, includes a
bundled copy of jQuery 1.4.0; the other, `reportgrid-analytics-nojquery.js`,
requires that you have jQuery enabled on the page already (and is
correspondingly smaller). Apart from this usage distinction they have identical
behavior.

Usage
=====

The analytics script is configured by passing parameters in querystring format
in the script's URL. For example:

    <script src='/path/to/analytics.js?option=value&option=value'></script>

The following options are supported:

* `pageEngagement` = `queueing`, `polling`, or `none`. Defaults to `queueing`.
* `elementEngagement` = `queueing`, `polling`, or `none`. Defaults to `none`.
* `attention` = `true` or `false`. Defaults to `false`.

Implementation status
=====================

The following features are either unimplemented or are in progress:

1. Click tracking
2. Engagement tracking
3. Form submission tracking
4. Page load / visit tracking
5. Unique visitor detection
6. Repeat visitor detection
7. Attention tracking
8. Browser version, OS, and platform tracking
9. Engagement / interaction totaling
10. Referer tracking
11. Search engine keyword tracking
12. Client timezone tracking
