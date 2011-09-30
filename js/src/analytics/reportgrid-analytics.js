// ReportGrid analytics client library

(function ($) {

  /**
   * Hardcoded constants.
   * The Swf cookie library uses an absolute URL to refer to its Swf. This
   * should be set prior to distributing this script.
   */

  var swfcookie_swf_url = 'http://api.reportgrid.com/js/swfcookie.swf';


  /**
   * Utility functions.
   * Because time is a side-effect, these functions are located at the top of
   * the script.
   */

  var script_load_time = +new Date();
  var time_since_page_load = function () {
    return +new Date() - script_load_time;
  };

  var from_regexp = function (re, s) {
    var result = re.exec(s);
    return result && result[1];
  };

  var round_to = function (n, rounding_base) {
    return Math.round(n / rounding_base) * rounding_base;
  };


  /**
   * Option parsing.
   * The user configures the script by including query-string parameters. The
   * currently-supported parameters are documented in the README.
   */

  var script_options = (function () {
    var default_options   = {pageEngagement:      'queueing',
                             cookieNamespace:     'all',
                             attentionResolution: 10,
                             crossdomain:         true,
                             interaction:         false,
                             attention:           false,
                             scrolling:           false,
                             rollup:              'domain',
                             rollupName:          '__all',
                             attentionSelector:   'body'};

    var schema            = {pageEngagement:      /^queueing|polling|none$/,
                             cookieNamespace:     /^\w+$/,
                             attentionResolution: /[1-9]\d*/,
                             crossdomain:         /^true|false$/,
                             interaction:         /^true|false$/,
                             attention:           /^true|false$/,
                             scrolling:           /^true|false$/,
							 rollup:              /^all|domain|global|domainglobal|false$/};

    var query_string      = $('script').eq(-1).attr('src').replace(/^.*\?/, '');
    var segments          = query_string.split(/&/);
    var current_kv_pair   = null;
    var specified_options = {};

    var parse = function (value) {
      if (value === 'false') return false;
      if (value == +value)   return +value;
      return value;
    };

    for (var i = 0, l = segments.length; i < l; ++i)
      specified_options[(current_kv_pair = segments[i].split(/=/))[0]] =
        parse(current_kv_pair.slice(1).join('='));

    // No longer checking for undefined options. The reason is that this script
    // must be bundlable with reportgrid-core.js, so not all parameters will be
    // known.
    for (var k in specified_options)
      if (schema.hasOwnProperty(k) && specified_options.hasOwnProperty(k) &&
          ! schema[k].test(specified_options[k]))
        throw new Error('Invalid parameter for option "' + k + '": ' +
                        specified_options[k]);

    return $.extend({}, default_options, specified_options);
  })();


  /**
   * ReportGrid.normalizePath(url): path normalization.
   * Normalizes paths by removing the protocol, 'www.' prefix, querystring, and
   * hashtag parameters from the URL. Originally this mechanism preserved the
   * hashtag, however doing so interferes with ReportGrid's crossdomain AJAX.
   */

  var normalize_path = ReportGrid.normalizePath = function (path) {
    var path_parser = /^(?:(?:file|https?):\/\/)?(?:www\.)?([^\?#]+)/i;
    return "/" + path_parser.exec(path)[1];
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
   * Browser detection.
   * Normalize the name of the rendering engine and its major version. We don't
   * want too many combinations of values, but we do want to know which engines
   * should be optimized for.
   *
   * This is now handled by the jquery.client plugin.
   */

  var browser_version  = $.client.browser + ' ' + $.client.version;
  var operating_system = $.client.os;
  var platform         = /iphone/i.test(operating_system) ||
                         /android/i.test(operating_system) ? 'mobile' :
                                                             'standard';

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
      decodeURIComponent(from_regexp(/[\?&]p=([^&]*)/, document.referrer)).replace(/\+/g, ' ') ||

    /^http:\/\/www\.bing\.com/.test(document.referrer) &&
      decodeURIComponent(from_regexp(/[\?&]q=([^&]*)/, document.referrer)).replace(/\+/g, ' ') ||

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
// referrer and keywords are commented to avoid excess of data
  var standard_event_properties = function (remove) {
    var o = {
      browserVersion:   browser_version,
      operatingSystem:  operating_system,
      platform:         platform,
      totalVisits:      user_visits,
      totalInteraction: user_total_interactions,
      totalEngagement:  round_to(user_total_engagement + time_since_page_load(), 1000),
 //     '~referrer':      referrer,
      timeOffset:       time_offset
 //     '~keywords':      search_keywords
	};
    remove = remove || [];
    for(var i = 0; i < remove.length; i++)
      delete o[remove[i]];
    return o;
  };


  /**
   * ReportGrid.customEvent(event_type, [properties = {}], [options = {}])
   * Automates event tracking for this page. Using this method will add several
   * standard properties to the event, and will automatically use the current
   * page path.
   *
   * Example usage:
   * ReportGrid.customEvent('clickOnHeader');
   * ReportGrid.customEvent('click', {on: 'header'});
   *
   * The page path, timestamp, and count can all be customized using a third
   * option:
   *
   * ReportGrid.customEvent('foo', {bar: 'bif'}, {path:      '...',
   *                                              timestamp: new Date(),
   *                                              count:     n});
   *
   * If you intend to use this, it may be useful to also use
   * ReportGrid.normalizePath() to keep the URL form standardized:
   *
   * ReportGrid.customEvent(..., {path: ReportGrid.normalizePath(url)});
   *
   * A '/' is automatically prepended to the path.
   *
   * If provided, the timestamp should always be a Date object.
   */

  var grabdomain = function(path)
  {
    var found = path.match(/^\/+([^\/]+)\//);
    if(found)
      return found[1];
    return null;
  }
   
  var track = ReportGrid.customEvent = function (event_type, properties, options) {
    var event_object = {};
    var path         = options && options.path || page_path;

    options && delete options.path;

    event_object[event_type] = $.extend({}, standard_event_properties(options && options.remove),
                                            properties || {});
                                            
    var paths = [path];
    if(script_options.rollup)
    {
      var domain;
	  switch(script_options.rollup)
      {
        case "domain":
          if(domain = grabdomain(path)) paths.push(domain + "/" + script_options.rollupName);
          break;
		case "global":
          paths.push("/" + script_options.rollupName);
          break;
		case "domainglobal":
          if(domain = grabdomain(path)) paths.push(domain + "/" + script_options.rollupName);
          paths.push("/" + script_options.rollupName);
          break;
        case "all":
          var parts = path.split(/\//g);
          parts.pop();
          while(parts.length > 0)
          {
            paths.push(parts.join("/") + "/" + script_options.rollupName);
            parts.pop();
          }
          break;
      }
    }

//  for debugging pursposes only
//  console.log("paths: " + paths + ", event: " + JSON.stringify(event_object));
    return ReportGrid.track(paths, $.extend({}, options, {event: event_object}));
  };


  /**
   * User usage patterns.
   * Tracks the total number of user-initiated events that we've observed.
   * These are initially zero because the values aren't necessarily known
   * until the page is loaded.
   */

  var user_visits             = 0;
  var user_total_interactions = 0;
  var user_total_engagement   = 0;


  /**
   * Initial visit tracking.
   * This is done before we know anything about the user. We do this just to
   * log the page view. Once the page is loaded (if crossdomain cookies are
   * used), we then emit the other tracking events.
   */

  track('visited', null, { remove : ["totalInteraction"] });


  /**
   * Initialization function.
   * All of the initialization logic is wrapped up. The reason is that we're
   * using SwfStore to load the user's identity if possible, and lots of events
   * depend on identifying the user accurately.
   *
   * This initialization function is called whether or not SwfStore was able to
   * load Flash cookies.
   */

  var initialize = function () {

  /**
   * SwfCookie library detection.
   * If SwfCookie is present, then replace the cookie() function with a pair of
   * SwfCookie accessors.
   */

  if (swf_cookie) {
    var original_cookie_function = cookie;

    cookie = function (name, value) {
      if (arguments.length > 1) {
        // Store in both places so that if the user later deactivates Flash
        // we'll still have something.
        original_cookie_function(name, value);
        swf_cookie.set(name, value);
        return value;
      } else
        return swf_cookie.get(name) ||
               original_cookie_function(name);
    };
  }


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
           time_since_last_visit > 1000              ? 'hourly' :
                                                       'new';
  })();

  user_visits += cookie('reportgrid_total_visits',
                   (+cookie('reportgrid_total_visits') || 0) + 1);

  user_total_engagement += +cookie('reportgrid_total_engagement') || 0;


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
   * delay is recorded in milliseconds and rounded to the nearest 50.
   */

  if (user_is_unique)
    track('uniqueVisited', null, { remove : ["totalInteraction"] });
  else
    track('repeatVisited', {timeFrame: last_visit_interval}, { remove : ["totalInteraction"] });

// commented out for perfomance reasons
//  $(function () {track('loaded', {delay: round_to(time_since_page_load(), 50)}, { remove : ["totalInteraction", "totalEngagement"] })});


  /**
   * Bounce detection.
   * The idea is that we send a new bounce event for each user who comes to the
   * site from somewhere else. If we see the user again and they have a
   * referrer from this site, then we retroactively unbounce them.
   */

  var current_host = from_regexp(/https?:\/\/([^\/]+)\//, document.referrer);

  if (current_host === (document.location.host || document.location.hostname)) {
    // Unbounce the user, since they have navigated to another page.
    if (! +cookie('reportgrid_already_unbounced')) {
      var bounce_time = +cookie('reportgrid_bounce_time');

      // Remember that we unbounced the user so that we don't do it twice.
      cookie('reportgrid_already_unbounced', 1);
      track('bounce', {
        count: -1,
        timestamp: bounce_time
      }, {
        path: cookie('reportgrid_bounce_path')
      });
    }
  } else {
    // The user is new, so create a bounce event and remember the fact that we
    // did so.
    cookie('reportgrid_already_unbounced', 0);

    var bounce_time = +new Date();
    cookie('reportgrid_bounce_path', page_path);
    cookie('reportgrid_bounce_time', bounce_time);

    track('bounce', {timestamp: bounce_time}, {});
  }


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
   * Form submit tracking.
   * This is not the same as the user clicking a 'submit' button. The main
   * difference is that a form-submit event also includes the values of input
   * fields with finite numbers of possibilities. Right now this includes
   * <select> and <input type='checkbox'>.
   */

  $('form').live('submit', function (e) {
    var children  = $(this).find('select, input[type="checkbox"]');
    var form_data = {};

    children.each(function () {
      form_data[identity_of($(this))] = $(this).val();
    });

    track('formSubmitted', form_data);
  });


  /**
   * Scroll tracking.
   * The page is broken into ten virtual regions. When a region becomes visible
   * to the user, we fire off a 'saw' event (i.e. the user saw this region).
   * Because this creates up to 11 extra API calls per page view, this option
   * is disabled by default.
   */

  var lowest_visible_region = 0;

  if (script_options.scrolling)
    setInterval(function () {
      var window_height   = $(window).height();
      var window_top      = $(window).scrollTop();
      var document_height = $('body').height();

      var visibility      = (window_height + window_top) / document_height * 10 >>> 0;

      if (visibility > lowest_visible_region) {
        track('saw', {region: visibility});
        lowest_visible_region = visibility;
      }
    }, 100);


  /**
   * Interaction tracking.
   * Any element that is clicked on or that receives an 'enter' keypress event
   * is considered to have been interacted with. If the script options specify
   * interaction tracking, then every page element will track interactions.
   */

  $('*').live('click', function (e) {
    if (e.target === this) {
      ++user_total_interactions;

      if (script_options.interaction)
        track('interaction', {element: identity_of($(this)),
                              type:    'click'});
    }
  });

  $('*').live('keypress', function (e) {
    if (e.target === this && e.which === 13) {
      ++user_total_interactions;

      if (script_options.interaction)
        track('interaction', {element: identity_of($(this)),
                              type:    'enterKey'});
    }
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
   *
   * Note that the event sent here is backdated. This prevents misleading
   * statistics about when users were engaged. (See the 'track' function above
   * for details about sending backdated events.)
   */

  if (script_options.pageEngagement === 'queueing' &&
      cookie('reportgrid_page_engagement_time') &&
      cookie('reportgrid_page_engagement_last_url'))
// commented for performance reasons
//    track('engagedQueueing', {
//      time: round_to(+cookie('reportgrid_page_engagement_time'), 100),
//      url : cookie('reportgrid_page_engagement_last_url')
//      timestamp : +cookie('reportgrid_page_last_engagement_start_time')
//    });

  cookie('reportgrid_page_engagement_last_url', page_path);
  cookie('reportgrid_page_last_engagement_start_time', +new Date());

  setInterval(function () {
    cookie('reportgrid_page_engagement_time', time_since_page_load());
    cookie('reportgrid_user_total_engagement',
           user_total_engagement + time_since_page_load());
  }, 100);


  /**
   * Engagement tracking (polling).
   * This uses a logarithmic event progression to continuously monitor the
   * amount of time spent on the page. The first event is fired after one
   * second, the next after two, the next after four, etc.
   *
   * It is important to separate observations made by queueing from those made
   * by polling. The reason is that queueing events are non-cumulative; a user
   * engaged for 10 seconds will emit exactly one event. Polling observations,
   * on the other hand, are cumulative; a user engaged for 10 seconds will emit
   * four events: engaged for 1 second, engaged for 2, 4, and 8.
   */

  var setup_engagement_polling = function (interval) {
    setTimeout(function () {
      track('engagedPolling', {time: round_to(time_since_page_load(), 1000)});
      setup_engagement_polling(interval * 2);
    }, interval);
  };

  if (script_options.pageEngagement === 'polling')
    setup_engagement_polling(1000);


  /**
   * Attention tracking.
   * Track every mouse movement made by the user. This is very expensive, but
   * potentially useful to discover where the user hovers on the page.
   *
   * To do this, each element is split into an nxn grid of logical tiles. When
   * the mouse moves into a new tile (or new element), a new event is reported.
   *
   * The size of the grid per element is configured by setting the
   * attentionResolution script parameter. By default this value is 10.
   */

  if (script_options.attention) {
    var attention_last_element = null;
    var attention_last_x       = 0;
    var attention_last_y       = 0;

    var attention_tiles        = script_options.attentionResolution;
    var attention_tile_size    = 1.0 / attention_tiles;

    $(script_options.attentionSelector + ' *').live('mousemove', function (e) {
      if (this !== e.target) return;

      // Identify the tile.
      var element_position = $(this).offset();
      var relative_x       = e.pageX - element_position.left;
      var relative_y       = e.pageY - element_position.top;

      var tile_x = round_to(relative_x / $(this).width(),  attention_tile_size) *
                   attention_tiles >>> 0;

      var tile_y = round_to(relative_y / $(this).height(), attention_tile_size) *
                   attention_tiles >>> 0;

      if (tile_x === attention_last_x &&
          tile_y === attention_last_y &&
          this   === attention_last_element)
        return;

      attention_last_element = this;
      attention_last_x       = tile_x;
      attention_last_y       = tile_y;

      track('attention', {element: identity_of($(this)),
                          tileX:   tile_x,
                          tileY:   tile_y});
    });
  }

  };    // End of initialize()


  /**
   * Main load logic.
   * SwfCookie loads asynchronously, so it drives the initialization process. We
   * nullify the SwfCookie reference if Flash cookie storage is unavailable.
   */

  var swf_cookie = null;

  if (script_options.crossdomain)
    $(function () {
      swf_cookie = new SwfCookie({namespace: script_options.cookieNamespace,
                                 swf_url:   swfcookie_swf_url,
                                 onready:   initialize,
                                 onerror:   function () {
                                              swf_cookie = null;
                                              initialize();
                                            }});
    });
  else
    initialize();

})(jQuery);
