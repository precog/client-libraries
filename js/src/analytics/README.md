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

* `interaction`: `true` (default) | `false`
  If true, any click or enter-key press anywhere in the document will be
  reported. A short string identifying the element is sent with each event.

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

The following features are either unimplemented or are in progress:

1. Attention tracking
2. OS and platform platform tracking
3. Bounce tracking (this may be covered by visits, unique visits, and referrers)
