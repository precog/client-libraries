Analytics library
=================

The analytics library automates the process of monitoring events on a page. Two
scripts are provided to do this. One, `reportgrid-analytics.js`, includes a
bundled copy of jQuery 1.4.0; the other, `reportgrid-analytics-no-jquery.js`,
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

The following features are functional:

1. Click tracking
2. Page load / visit tracking
3. Unique visitor detection
4. Repeat visitor detection
5. Client timezone tracking

The following features are either unimplemented or are in progress:

1. Engagement tracking
2. Form submission tracking
3. Attention tracking
4. Browser version, OS, and platform tracking
5. Engagement / interaction totaling
6. Referer tracking
7. Search engine keyword tracking
