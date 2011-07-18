// ReportGrid analytics client library

(function ($) {

  var from_regexp = function (re, s) {
    var result = re.exec(s);
    return result && result[1];
  };


  /**
   * Option parsing.
   * The user configures the script by including query-string parameters. The
   * currently-supported parameters are:
   *
   * pageEngagement:    queueing (default) | polling | none
   * elementEngagement: queueing           | polling | none (default)
   * reportParentPaths: true | false (default)
   * attention:         true | false (default)
   */

  var default_options = {pageEngagement:    'queueing',
                         elementEngagement: 'none',
                         reportParentPaths:  false,
                         attention:          false};

  var script_options = (function () {
    var query_string      = $('script').eq(-1).attr('src').replace(/^.*\?/, '');
    var segments          = query_string.split(/&/);
    var current_kv_pair   = null;
    var specified_options = {};

    for (var i = 0, l = segments.length; i < l; ++i)
      specified_options[(current_kv_pair = segments[i].split(/=/))[0]] =
        current_kv_pair.slice(1).join('=');

    return $.extend({}, default_options, specified_options);
  })();


  /**
   * ReportGrid.normalizePath(url): path normalization.
   * Normalizes paths by removing the protocol, 'www.' prefix, querystring, and
   * hashtag parameters from the URL. Originally this mechanism preserved the
   * hashtag, however doing so interferes with ReportGrid's crossdomain AJAX.
   */

  var normalize_path = ReportGrid.normalizePath = function (path) {
    var path_parser = /^(?:https?:\/\/)?(?:www\.)?([^\/]+)\/([^?#]+)/i;
    return [].join.call([].slice.call(path_parser.exec(path), 1), '/');
  };

  var page_path = normalize_path(document.location.href);


  /**
   * Cookie handling.
   * The cookie(name, [value]) accessor manipulates cookies on the document.
   * Each cookie stored will last for about 100 years.
   */

  var cookie = function (name, value) {
    var in_a_century = (function (d) {
      d.setDate(d.getDate() + 36500);
      return d;
    })(new Date());

    if (arguments.length <= 1)
      return from_regexp(new RegExp('\\b' + name + '=([^;]*)'), document.cookie);
    document.cookie = name + '=' + value + '; expires=' + in_a_century.toUTCString();
    return value;
  };


  /**
   * User identity detection.
   * Determine whether the user had an identity prior to visiting the page.
   * If so, they're not unique; otherwise, they are unique and will be assigned
   * a new identity.
   *
   * For repeat visitors (non-uniques), we also calculate the amount of time
   * that has elapsed since their last visit. We use this to put them into one
   * of a few categories: hourly, daily, weekly, or monthly visitors.
   */

  var mark_user_as_unique = function () {
    for (var uuid = '', i = 0; i < 32; ++i)
      uuid += (Math.random() * 16 >>> 0).toString(16);

    user_is_unique = true;
    return cookie('reportgrid_identity', uuid);
  };

  var user_is_unique = false;
  var user_identity  = cookie('reportgrid_identity') || mark_user_as_unique();
  var user_visits    = cookie('reportgrid_total_visits',
                         (+cookie('reportgrid_total_visits') || 0) + 1);

  var last_visit_interval = (function () {
    var record_user_visit = function () {
      var original = cookie('reportgrid_last_visit') || +new Date();
      cookie('reportgrid_last_visit', +new Date());
      return original;
    };

    var time_since_last_visit = +new Date() - record_user_visit();

    return time_since_last_visit > 3600000 * 24 * 30 ? 'yearly' :
           time_since_last_visit > 3600000 * 24 * 7  ? 'monthly' :
           time_since_last_visit > 3600000 * 24      ? 'weekly' :
           time_since_last_visit                     ? 'hourly' :
                                                       'new';
  })();


  /**
   * Browser detection.
   * Normalize the name of the rendering engine and its major version. We don't
   * want too many combinations of values, but we do want to know which engines
   * should be optimized for.
   */

  var browser_version = ($.browser.msie    ? 'IE' :
                         $.browser.mozilla ? 'FF' :
                         $.browser.opera   ? 'Opera' : 'Webkit') +
                        parseInt($.browser.version);


  /**
   * Referrer detection.
   * There are two use cases for referrers. One is to simply record the user's
   * previous URL in a reasonably normalized way; this is useful for affiliate
   * marketing and such. The other is to detect search engine traffic and to
   * record the keywords that were used to find the page.
   */

  var referrer = !! document.referrer && normalize_path(document.referrer);


  /**
   * SEO tracking.
   * The top three search engines all use regular patterns for keywords. When
   * reported, the keywords will be separated by spaces.
   */

  var search_keywords =
    /^http:\/\/www\.google\.com/.test(document.referrer) &&
      decodeURIComponent(from_regexp(/[\?&]q=([^&]*)/, document.referrer)) ||

    /^http:\/\/search\.yahoo\.com/.test(document.referrer) &&
      decodeURIComponent(from_regexp(/[\?&]p=([^&]*)/, document.referrer)).replace(/+/g, ' ') ||

    /^http:\/\/www\.bing\.com/.test(document.referrer) &&
      decodeURIComponent(from_regexp(/[\?&]q=([^&]*)/, document.referrer)).replace(/+/g, ' ') ||

    '';


  /**
   * Time zone offset.
   * A simple way to determine which time zone the user's browser is in. This
   * is expressed in UTC hour offset; e.g. for UTC-0600, the value would be -6.
   */

  var time_offset = new Date().getHours() - new Date().getUTCHours();


  /**
   * Element identification.
   * For tracking purposes it's often useful to know which element was clicked
   * on or otherwise interacted with. This function generates a CSS selector
   * to mark an element.
   *
   * Note that right now it doesn't generate unique CSS selectors; this may
   * change in the future.
   */

  var identity_of = function (element) {
    if (element.attr('id')) return '#' + element.attr('id');

    var node_and_classes = element[0].nodeName.toLowerCase();
    for (var classes = element[0].className.split(/\s+/),
             i = 0, l = classes.length; i < l; ++i)
      if (classes[i])
        node_and_classes += '.' + classes[i];

    return node_and_classes;
  };


  /**
   * Standard event properties.
   * This is a collection of useful attributes that are independent of the
   * event being reported. They are automatically attached to events sent via
   * ReportGrid.customEvent(), and they appear with all of the events tracked
   * by the analytics client.
   */

  var standard_event_properties = function () {
    return {browserVersion: browser_version,
            totalVisits:    user_visits,
            referrer:       referrer,
            timeOffset:     time_offset,
            '~keywords':    search_keywords};
  };


  /**
   * ReportGrid.customEvent(event_type, [properties = {}], [path])
   * Automates event tracking for this page. Using this method will add several
   * standard properties to the event, and will automatically use the current
   * page path.
   *
   * Example usage:
   * ReportGrid.customEvent('clickOnHeader');
   * ReportGrid.customEvent('click', {on: 'header'});
   *
   * The page path can be overridden by specifying a third argument:
   *
   * ReportGrid.customEvent('foo', {bar: 'bif'}, '...');
   *
   * If you intend to use this, it may be useful to also use
   * ReportGrid.normalizePath() to keep the URL form standardized:
   *
   * ReportGrid.customEvent('...', ReportGrid.normalizePath(url));
   *
   * A '/' is automatically prepended to the path.
   */

  var track = ReportGrid.customEvent = function (event_type, properties, path) {
    var event_object = {};
    event_object[event_type] = $.extend({}, standard_event_properties(),
                                            properties || {});
    return ReportGrid.track('/' + (path || page_path), {event: event_object});
  };


  /**
   * Visit/load initialization.
   * Creates three events. First, a page-visit event indicates that the user
   * attempted to load the page and succeeded in loading this script.
   *
   * Second, a uniqueVisit or repeatVisit event is triggered depending on
   * whether the user is new or a repeat visitor. If the user is a repeat
   * visitor, then the timeframe of their last visit is also recorded.
   *
   * Third, a page-load event is triggered once the page is done loading. This
   * contains a 'delay' field to indicate the delay perceived by the user. The
   * delay is recorded in milliseconds.
   */

  track('visited');

  if (user_is_unique) track('uniqueVisited');
  else                track('repeatVisited', {timeFrame: last_visit_interval});

  var script_load_time = +new Date();
  var time_since_page_load = function () {
    return +new Date() - script_load_time;
  };

  $(function () {track('loaded', {'~delay': time_since_page_load()})});


  /**
   * Click tracking.
   * Every reasonably-clickable element is tracked. Right now this includes
   * links, buttons, and submit buttons. A coarse-grained selector for the
   * clicked element is sent with the event; if precision is important, each
   * element should be given a unique ID; e.g:
   *
   * <a href='somewhere' id='something-unique'>...</a>
   */

  $('a, button, input[type="submit"]').live('click', function (e) {
    var node_name  = this.nodeName.toLowerCase(),
        event_name = node_name === 'a'      ? 'linkClicked' :
                     node_name === 'button' ? 'buttonClicked' :
                     node_name === 'input'  ? 'submitClicked' :
                                               node_name + 'Clicked';

    track(event_name, {element:   identity_of($(this)),
                       newWindow: $(this).attr('target') === '_blank'});
  });


  /**
   * Email tracking.
   * An event is tracked each time the user clicks on a link that points to an
   * e-mail address. This is counted as a regular link click, but also counted
   * as a special 'email' event.
   */

  $('a[href^="mailto:"]').live('click', function (e) {
    track('emailed', {address: $(this).attr('href').replace(/^mailto:/, '')});
  });


  /**
   * Engagement tracking (queueing).
   * If the script options specify that engagements should be tracked by
   * queueing, then we maintain the last page view time for the user by updating
   * a cookie. The next time the user visits the site, we check the cookie and
   * record the engagement time after the fact.
   *
   * The cookies required to support queueing are enabled even if engagement
   * tracking is done by some other mechanism. This enables you to change the
   * method of engagement tracking later without losing data.
   *
   * For a more accurate model, see 'polling' engagement tracking.
   */

  if (script_options.pageEngagement === 'queueing' &&
      cookie('reportgrid_page_engagement_time') &&
      cookie('reportgrid_page_engagement_last_url'))

    track('engaged', {time: +cookie('reportgrid_page_engagement_time')},
                     cookie('reportgrid_page_engagement_last_url'));

  cookie('reportgrid_page_engagement_last_url', page_path);

  setInterval(function () {
    cookie('reportgrid_page_engagement_time', time_since_page_load());
  }, 100);

})(jQuery);
