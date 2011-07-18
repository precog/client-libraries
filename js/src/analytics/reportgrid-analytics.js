// ReportGrid analytics client library

(function ($) {
  var

  default_options = {pageEngagement:    'queueing',
                     elementEngagement: 'none',
                     reportParentPaths:  false,
                     attention:          false},

  // Script options: This becomes a hash that incorporates the default
  // options with user overrides.

    script_options = (function (querystring) {
      for (var specified_options = {},
               segments          = querystring.split(/&/),
               current_kv_pair   = null,
               i = 0, l = segments.length; i < l; ++i)

        specified_options[(current_kv_pair = segments[i].split(/=/))[0]] =
          current_kv_pair.slice(1).join('=');

      return $.extend({}, default_options, specified_options);
    })($('script').eq(-1).attr('src').replace(/^.*\?/, '')),

  // Page path construction: The page's URL without a querystring or 'www.'
  // prefix.

    path_parser = /^(?:https?:\/\/)?(?:www\.)?([^\/]+)\/([^?#]+)(?:\?[^#]*)?(#.*)?$/i,
    normalize_path = function (path) {
      return [].join.call([].slice.call(path_parser.exec(path), 1), '/');
    },

    page_path = '/' + normalize_path(document.location.href),

  // User identification: Check the cookie to see whether the user
  // already has an identity. Otherwise provide a new one.

    from_regexp = function (re, s) {
      var result = re.exec(s);
      return result && result[1];
    },

    in_a_century = (function (d) {
      d.setDate(d.getDate() + 36500);
      return d;
    })(new Date()),

    cookie = function (name, value) {
      if (arguments.length <= 1)
        return from_regexp(new RegExp('\\b' + name + '=([^;]*)'), document.cookie);
      document.cookie = name + '=' + value + '; expires=' + in_a_century.toUTCString();
      return value;
    },

    user_is_unique = false,

    new_user_identity = function () {
      user_is_unique = true;
      for (var uuid = '', i = 0; i < 32; ++i)
        uuid += (Math.random() * 16 >>> 0).toString(16);
      return cookie('reportgrid_identity', uuid);
    },

    user_identity = cookie('reportgrid_identity') || new_user_identity(),

    user_visits = cookie('reportgrid_total_visits',
                    (+cookie('reportgrid_total_visits') || 0) + 1),

  // Repeat visitor timeframe: If the user has been here before, then they'll
  // have a timestamp cookie indicating when they last visited. We read this to
  // determine the time interval of their last visit. This could be daily,
  // weekly, monthly, or yearly.

    last_visit_time = function () {
      var original = cookie('reportgrid_last_visit') || +new Date();
      cookie('reportgrid_last_visit', +new Date());
      return original;
    },

    time_since_last_visit = +new Date() - last_visit_time(),

    last_visit_interval = time_since_last_visit > 3600000 * 24 * 30 ? 'yearly' :
                          time_since_last_visit > 3600000 * 24 * 7  ? 'monthly' :
                          time_since_last_visit > 3600000 * 24      ? 'weekly' :
                          time_since_last_visit                     ? 'hourly' :
                                                                      'new',

  // Browser detection: Generalize the rendering engine and version to a
  // standard format.

    browser_version = ($.browser.msie    ? 'IE' :
                       $.browser.mozilla ? 'FF' :
                       $.browser.opera   ? 'Opera' : 'Webkit') +
                      parseInt($.browser.version),

  // Referrer detection: See whether a referrer exists, and if so normalize the
  // path like we did with the one for this page.

    referrer = !! document.referrer && normalize_path(document.referrer),

  // Search engine keywords: Detect certain referrers and parse the query
  // string to find out what the user was searching for.

    google_search =
      /^http:\/\/www\.google\.com/.test(document.referrer) &&
      decodeURIComponent(from_regexp(/[\?&]q=([^&]*)/, document.referrer)),

    yahoo_search =
      /^http:\/\/search\.yahoo\.com/.test(document.referrer) &&
      decodeURIComponent(from_regexp(/[\?&]p=([^&]*)/, document.referrer)).replace(/+/g, ' '),

    bing_search =
      /^http:\/\/www\.bing\.com/.test(document.referrer) &&
      decodeURIComponent(from_regexp(/[\?&]q=([^&]*)/, document.referrer)).replace(/+/g, ' '),

    search_keywords = google_search || yahoo_search || bing_search || '',

  // Time offset detection: See what the difference is between local time and
  // UTC. This tells us the timezone of the user.

    time_offset = new Date().getHours() - new Date().getUTCHours(),

  // Element identity: Given an element, determine a reasonably small path that
  // identifies it within the document.

    identity_of = function (element) {
      if (element.attr('id')) return element.attr('id');
      var node_and_classes =
        [element[0].nodeName].concat(element[0].className.split(/\s+/)).join('.');
      if ($(node_and_classes).length === 1) return node_and_classes;
      else identity_of(element.parent()) + ' > :eq(' + element.index() + ')';
    },

  // Standard event properties: These are automatically attached to each event.
  // Custom properties can be added as well.

    standard_event_properties = function () {
      return {browserVersion: browser_version,
              totalVisits:    user_visits,
              referrer:       referrer,
              timeOffset:     time_offset,
              '~keywords':    search_keywords};
    },

  // Event tracking function: Make it simpler to track events by preloading the
  // page path and a standard set of event properties.

    track = ReportGrid.customEvent = function (event_type, properties) {
      var event_object = {};
      event_object[event_type] = $.extend({}, standard_event_properties(),
                                              properties || {});
      return ReportGrid.track(page_path, {event: event_object});
    };

  // Individual events.
  // From here down is the processing to handle individual events that are
  // triggered by user actions.

  // Visit/load tracking
  track('visited');

  var script_load_time = +new Date();
  $(function () {track('loaded', {'~delay': +new Date() - script_load_time})});

  // Unique visit tracking
  if (user_is_unique) track('uniqueVisited');
  else                track('repeatVisited', {timeFrame: last_visit_interval});

  // Link/button/etc. click tracking
  $('a, button, input[type="submit"]').live('click', function (e) {
    var node_name  = this.nodeName.toLowerCase(),
        event_name = node_name === 'a'      ? 'linkClicked' :
                     node_name === 'button' ? 'buttonClicked' :
                     node_name === 'input'  ? 'submitClicked' :
                                               node_name + 'Clicked';
    track(event_name, {element:   identity_of($(this)),
                       newWindow: $(this).attr('target') === '_blank'});
  });

  // Email link tracking
  $('a[href^="mailto:"]').live('click', function (e) {
    track('emailed', {address: $(this).attr('href').replace(/^mailto:/, '')});
  });

})(jQuery);
