/* Copyright (C) 2011 by ReportGrid, Inc. All rights reserved.
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * No portion of this Software shall be used in any application which does not
 * use the ReportGrid platform to provide some subset of its functionality.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

// JSON parsing & stringification:
var JSON;
if(!JSON)JSON={};
(function(){"use strict";function f(n){return n<10?'0'+n:n;}
if(typeof Date.prototype.toJSON!=='function'){Date.prototype.toJSON=function(key){return isFinite(this.valueOf())?this.getUTCFullYear()+'-'+
f(this.getUTCMonth()+1)+'-'+
f(this.getUTCDate())+'T'+
f(this.getUTCHours())+':'+
f(this.getUTCMinutes())+':'+
f(this.getUTCSeconds())+'Z':null;};String.prototype.toJSON=Number.prototype.toJSON=Boolean.prototype.toJSON=function(key){return this.valueOf();};}
var cx=/[\u0000\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,escapable=/[\\\"\x00-\x1f\x7f-\x9f\u00ad\u0600-\u0604\u070f\u17b4\u17b5\u200c-\u200f\u2028-\u202f\u2060-\u206f\ufeff\ufff0-\uffff]/g,gap,indent,meta={'\b':'\\b','\t':'\\t','\n':'\\n','\f':'\\f','\r':'\\r','"':'\\"','\\':'\\\\'},rep;function quote(string){escapable.lastIndex=0;return escapable.test(string)?'"'+string.replace(escapable,function(a){var c=meta[a];return typeof c==='string'?c:'\\u'+('0000'+a.charCodeAt(0).toString(16)).slice(-4);})+'"':'"'+string+'"';}
function str(key,holder){var i,k,v,length,mind=gap,partial,value=holder[key];if(value&&typeof value==='object'&&typeof value.toJSON==='function'){value=value.toJSON(key);}
if(typeof rep==='function'){value=rep.call(holder,key,value);}
switch(typeof value){case'string':return quote(value);case'number':return isFinite(value)?String(value):'null';case'boolean':case'null':return String(value);case'object':if(!value){return'null';}
gap+=indent;partial=[];if(Object.prototype.toString.apply(value)==='[object Array]'){length=value.length;for(i=0;i<length;i+=1){partial[i]=str(i,value)||'null';}
v=partial.length===0?'[]':gap?'[\n'+gap+partial.join(',\n'+gap)+'\n'+mind+']':'['+partial.join(',')+']';gap=mind;return v;}
if(rep&&typeof rep==='object'){length=rep.length;for(i=0;i<length;i+=1){k=rep[i];if(typeof k==='string'){v=str(k,value);if(v){partial.push(quote(k)+(gap?': ':':')+v);}}}}else{for(k in value){if(Object.hasOwnProperty.call(value,k)){v=str(k,value);if(v){partial.push(quote(k)+(gap?': ':':')+v);}}}}
v=partial.length===0?'{}':gap?'{\n'+gap+partial.join(',\n'+gap)+'\n'+mind+'}':'{'+partial.join(',')+'}';gap=mind;return v;}}
if(typeof JSON.stringify!=='function'){JSON.stringify=function(value,replacer,space){var i;gap='';indent='';if(typeof space==='number'){for(i=0;i<space;i+=1){indent+=' ';}}else if(typeof space==='string'){indent=space;}
rep=replacer;if(replacer&&typeof replacer!=='function'&&(typeof replacer!=='object'||typeof replacer.length!=='number')){throw new Error('JSON.stringify');}
return str('',{'':value});};}
if(typeof JSON.parse!=='function'){JSON.parse=function(text,reviver){var j;function walk(holder,key){var k,v,value=holder[key];if(value&&typeof value==='object'){for(k in value){if(Object.hasOwnProperty.call(value,k)){v=walk(value,k);if(v!==undefined){value[k]=v;}else{delete value[k];}}}}
return reviver.call(holder,key,value);}
text=String(text);cx.lastIndex=0;if(cx.test(text)){text=text.replace(cx,function(a){return'\\u'+
('0000'+a.charCodeAt(0).toString(16)).slice(-4);});}
if(/^[\],:{}\s]*$/.test(text.replace(/\\(?:["\\\/bfnrt]|u[0-9a-fA-F]{4})/g,'@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,']').replace(/(?:^|:|,)(?:\s*\[)+/g,''))){j=eval('('+text+')');return typeof reviver==='function'?walk({'':j},''):j;}
throw new SyntaxError('JSON.parse');};}}());

// ReportGrid core:
var ReportGrid = window.ReportGrid || {};

(function() {
  var Util = {
	findScript: function(fragment) {
      var scripts = document.getElementsByTagName('SCRIPT');

      for (var i = 0; i < scripts.length; i++) {
        var script = scripts[i];
        var src = script.getAttribute('src');

        if (src && src.indexOf(fragment) != -1) {
          return script;
        }
      }

      return undefined;
    },
    getConfiguration: function() {
      return Util.parseQueryParameters(Util.findScript('reportgrid').getAttribute('src'));
    },

    parseQueryParameters: function(url) {
      var index = url.indexOf('?');

      if (index < 0) return {};

      var query = url.substr(index + 1);

      var keyValuePairs = query.split('&');

      var parameters = {};

      for (var i = 0; i < keyValuePairs.length; i++) {
        var keyValuePair = keyValuePairs[i];

        var split = keyValuePair.split('=');

        var key = split[0];
        var value = '';

        if (split.length >= 2) {
          value = decodeURIComponent(split[1]);
        }

        parameters[key] = value;
      }

      return parameters;
    },

    addQueryParameters: function(url, query) {
      var suffix = url.indexOf('?') == -1 ? '?' : '&';

      var queries = [];

      for (var name in query) {
        var value = (query[name] || '').toString();

        queries.push(name + '=' + encodeURIComponent(value));
      }

      if (queries.length == 0) return url;
      else return url + suffix + queries.join('&');
    },

    getConsole: function(enabled) {
      var console = enabled ? window.console : undefined;
      if (!console) {
        console = {};

        console.log   = function() {}
        console.debug = function() {}
        console.info  = function() {}
        console.warn  = function() {}
        console.error = function() {}
      }

      return console;
    },

    createCallbacks: function(success, failure, msg) {
      var successFn = function(fn, msg) {
        if (fn) return fn;
        else return function(result) {
          if (result !== undefined) {
            $.Log.debug('Success: ' + msg + ': ' + JSON.stringify(result));
          }
          else {
            $.Log.debug('Success: ' + msg);
          }
        }
      }

      var failureFn = function(fn, msg) {
        if (fn) return fn;
        else return function(code, reason) {
          $.Log.error('Failure: ' + msg + ': code = ' + code + ', reason = ' + reason);
        }
      }

      return {
        success: successFn(success, msg),
        failure: failureFn(failure, msg)
      };
    },

    removeLeadingSlash: function(path) {
      if (path.length == 0) return path;
      else if (path.substr(0, 1) == '/') return path.substr(1);
      else return path;
    },

    removeTrailingSlash: function(path) {
      if (path.length == 0) return path;
      else if (path.substr(path.length - 1) == "/") return path.substr(0, path.length - 1);
      else return path;
    },

    removeDuplicateSlashes: function(path) {
      return path.replace(/[/]+/g, "/");
    },

    sanitizePath: function(path) {
      if (path === undefined) throw Error("path cannot be undefined");
      else return Util.removeDuplicateSlashes("/" + path + "/");
    },

    sanitizeProperty: function(property) {
      if (property === undefined) throw Error("Property cannot be undefined");
      else if (property.length == 0) return property;
      else if (property.substr(0, 1) == ".") return property;
      else return "." + property;
    },
    
    getBoundResults: function(o) {
      return o.top ? '/top/' + o.top : (o.bottom ? '/bottom/' + o.bottom : '');
    },

    splitPathVar: function(pathVar) {
      if (pathVar.length == 0) return ["/", ""];
      if (pathVar.substr(0, 1) == ".") return ["/", pathVar]

      var index = pathVar.indexOf('/.');

      if (index <  0) return [Util.sanitizePath(pathVar), ""];

      return [Util.sanitizePath(pathVar.substr(0, index + 1)), pathVar.substr(index + 1)];
    },

    filter: function(c, f) {
      var result = c;

      if (c instanceof Array) {
        result = [];

        for (var i = 0; i < c.length; i++) {
          var e = c[i];

          if (f(e)) result.push(e);
        }
      }
      else if (c instanceof Object) {
        result = {};

        for (var key in c) {
          var value = c[key];

          if (f(key, value)) result[key] = value;
        }
      }

      return result;
    },

    normalizeTime: function(o) {
      if (o instanceof Date || o instanceof String)
        return +o;
      return o;
    },

    normalizeTimestamp: function(o) {
      var time = o["timestamp"] || o["#timestamp"];
      if(!time)
        return;
      delete o["timestamp"];
      if(time === "none" || time === false)
        o["#timestamp"] = false;
      else
        o["#timestamp"] = Util.normalizeTime(time);
    },
    
    defaultQuery: function(o) {
      var q = { tokenId : $.Config.tokenId },
          start = Util.normalizeTime(o.start),
          end = Util.normalizeTime(o.end);
      if(start || end)
      {
        q.start = start || ReportGrid.Zero;
        q.end = end || ReportGrid.Inf;
      }
      if(o.location)
      {
        q.location = o.location;
      }
      if(o.timeZone)
      {
        q.timeZone = o.timeZone;
      }
      return q;
    },
    
    groupQuery: function(o) {
      var q = Util.defaultQuery(o);
      if(o.groupBy)
          q.groupBy = o.groupBy;
      if(o.groups)
          q.groups = typeof o.groups == "string" ? o.groups : o.groups.join(",");
      return q;
    }
  }

  var Network = {
    doAjaxRequest: function(options) {
      var method   = options.method || 'GET';
      var query    = options.query || {};
      var path     = Util.addQueryParameters(options.path, query);
      var content  = options.content;
      var headers  = options.headers || {};
      var success  = options.success;
      var failure  = options.failure || function() {};

      $.Log.info('HTTP ' + method + ' ' + path + ': headers(' + JSON.stringify(headers) + '), content('+ JSON.stringify(content) + ')');

      var createNewXmlHttpRequest = function() {
        if (window.XMLHttpRequest) {
          return new XMLHttpRequest();
        }
        else {
          return new ActiveXObject("Microsoft.XMLHTTP");
        }
      }

      var request = createNewXmlHttpRequest();

      request.open(method, path);

      request.onreadystatechange = function() {
        if (request.readyState == 4) {
          if (request.status == 200) {
            if (request.responseText !== null && request.responseText.length > 0) {
              success(JSON.parse(this.responseText));
            }
            else {
              success(undefined);
            }
          }
          else {
            failure(request.status, request.statusText);
          }
        }
      }

      for (var name in headers) {
        var value = headers[name];

        request.setRequestHeader(name, value);
      }

      if (content !== undefined) {
        request.setRequestHeader('Content-Type', 'application/json');

        request.send(JSON.stringify(content));
      }
      else {
        request.send(null);
      }
    },

    doJsonpRequest: function(options) {
      var method   = options.method || 'GET';
      var query    = options.query || {};
      var path     = Util.addQueryParameters(options.path, query);
      var content  = options.content;
      var headers  = options.headers || {};
      var success  = options.success;
      var failure  = options.failure || function() {};

      $.Log.info('HTTP ' + method + ' ' + path + ': headers(' + JSON.stringify(headers) + '), content('+ JSON.stringify(content) + ')');

      var random   = Math.floor(Math.random() * 214748363);
      var funcName = 'ReportGridJsonpCallback' + random.toString();

      window[funcName] = function(content, meta) {
        if (meta.status.code === 200) {
          success(content);
        }
        else {
          failure(meta.status.code, meta.status.reason);
        }

        document.head.removeChild(document.getElementById(funcName));
        try{
            delete window[funcName];
        }catch(e){
            window[funcName] = undefined;
        }
      }

      var extraQuery = {};

      extraQuery.method   = method;

      for (_ in headers) { extraQuery.headers = JSON.stringify(headers); break; }

      extraQuery.callback = funcName;

      if (content !== undefined) {
        extraQuery.content = JSON.stringify(content);
      }

      var fullUrl = Util.addQueryParameters(path, extraQuery);

      var script = document.createElement('SCRIPT');

      script.setAttribute('type', 'text/javascript');
      script.setAttribute('src',  fullUrl);
      script.setAttribute('id',   funcName);

      // Workaround for document.head being undefined.
      if (! document.head)
        document.head = document.getElementsByTagName('head')[0];

      document.head.appendChild(script);
    },

    createHttpInterface: function(doRequest) {
      return {
        get: function(path, callbacks, query, headers) {
          doRequest(
            {
              method:   'GET',
              path:     path,
              headers:  headers,
              success:  callbacks.success,
              failure:  callbacks.failure,
              query:    query
            }
          );
        },

        put: function(path, content, callbacks, query, headers) {
          doRequest(
            {
              method:   'PUT',
              path:     path,
              content:  content,
              headers:  headers,
              success:  callbacks.success,
              failure:  callbacks.failure,
              query:    query
            }
          );
        },

        post: function(path, content, callbacks, query, headers) {
          doRequest(
            {
              method:   'POST',
              path:     path,
              content:  content,
              headers:  headers,
              success:  callbacks.success,
              failure:  callbacks.failure,
              query:    query
            }
          );
        },

        remove: function(path, callbacks, query, headers) {
          doRequest(
            {
              method:   'DELETE',
              path:     path,
              headers:  headers,
              success:  callbacks.success,
              failure:  callbacks.failure,
              query:    query
            }
          );
        }
      }
    }
  }

  ReportGrid.$ = {};

  var $ = ReportGrid.$;

  $.Util = Util;
  
  $.Config = Util.getConfiguration();

  $.Extend = function(object, extensions) {
    for (var name in extensions) {
      if (object[name] === undefined) {
        object[name] = extensions[name];
      }
    }
  }
  
  $.Bool = function(v) {
    return v === true || v === 1 || (v = (""+v).toLowerCase()) == "true" || v == "on" || v == "1";
  }

  $.Extend($.Config,
    {
      analyticsServer: "http://api.reportgrid.com/services/analytics/v1/",
      useJsonp : "true",
      enableLog : "false"
    }
  );
  
  $.Config.analyticsServer = Util.removeTrailingSlash($.Config.analyticsServer);

  $.Http = function() {
    return $.Bool(ReportGrid.$.Config.useJsonp) ? ReportGrid.$.Http.Jsonp : ReportGrid.$.Http.Ajax;
  }

  $.Http.Ajax  = Network.createHttpInterface(Network.doAjaxRequest);
  $.Http.Jsonp = Network.createHttpInterface(Network.doJsonpRequest);

  var console = Util.getConsole($.Bool($.Config.enableLog));

  $.Log = {
    log:    function(text) { console.log(text);   },
    debug:  function(text) { console.debug(text); },
    info:   function(text) { console.info(text);  },
    warn:   function(text) { console.warn(text);  },
    error:  function(text) { console.error(text); }
  }

  /** Constants */
  ReportGrid.Minute   = 'minute';
  ReportGrid.Hour     = 'hour';
  ReportGrid.Day      = 'day';
  ReportGrid.Week     = 'week';
  ReportGrid.Month    = 'month';
  ReportGrid.Year     = 'year';
  ReportGrid.Eternity = 'eternity';

  var http = $.Http();

  /** Periodicity constants. */
  ReportGrid.Periodicity = {
    Minute:   "minute",
    Hour:     "hour",
    Day:      "day",
    Week:     "week",
    Year:     "year",
    Eternity: "eternity"
  }

  /** Time constants. */
  ReportGrid.Time = {
    Zero:  0,
    Inf:   2147483647
  }

  /** Tracks an event. If no #timestamp is specified, the current time is used.
   *
   * The #timestamp is optional, and to the current time.
   * The #timestamp parameter can be set to "none" (or false) to not track time at all
   *
   * ReportGrid.track("/merchants/Starbucks/locations/USA_CO_Boulder/1/", {
   *   "purchase": {
   *     "item": "Americano",
   *     "size": "Grande"
   *   }
   * });
   */
  ReportGrid.track = function(path_, events, success, failure) {
    if(typeof path_ == "string")
      path_ = [path_];
    var paths = [];
    for(var i = 0; i < path_.length; i++)
      paths.push(Util.sanitizePath(path_[i]));

    if (events == null) throw Error("argument 'events' cannot be null");

    for (var eventName in events) {
      // Allow user to specify Date for timestamp:
      Util.normalizeTimestamp(events[eventName]);
    }

    // Extract out first event for logging:
    var firstEventName, firstEventProperties, firstEventTime;

    for (var eventName in events) {
      firstEventName       = eventName;
      firstEventProperties = events[eventName];
      firstEventTime       = events[eventName]["#timestamp"];
      break;
    }

    var description = 'Track event ' + firstEventName + ' (' + JSON.stringify(firstEventProperties) + ') @ ' + (firstEventTime === false ? "no time tracked" : "current time");
    for(var i = 0; i < paths.length; i++)
    {
      path = paths[i];
      http.post(
        $.Config.analyticsServer + '/vfs' + path,
        events,
        Util.createCallbacks(success, failure, description),
        {tokenId: $.Config.tokenId }
      );
    }
  }

  /**
   * Lists children of the specified path and optional property. You can use this
   * method to list all the path children, all the events at the specified
   * path, or both the path children and the events.
   *
   * Event names are preceded with the dot character ".", while path names are
   * suffixed with the forward slash character "/".
   *
   * @param path      The path to the data.
   * @param options   An object that contains an optional type
   *                  ("all", "path" or "property") and an optional
   *                  property (e.g. "transaction.sender").
   *
   * ReportGrid.children("/", {"type":"all"});
   * > ["foo/", ".baz"]
   *
   * ReportGrid.children("/", {"type":"path"});
   * > ["foo/"]
   *
   * ReportGrid.children("/", {"type":"property"});
   * > [".baz"]
   *
   * ReportGrid.children("/", {"property":"baz"});
   * > [".bar"]
   */
  ReportGrid.children = function(path_, options_, success, failure) {
    var options = options_ || {};

    var path     = Util.sanitizePath(path_);
    var property = Util.sanitizeProperty(options.property || "");
    var type     = (property != "") ? "property" : (options.type || "all").toLowerCase();

    var description = 'List children of ' + path + property;

    var callbacks = Util.createCallbacks(success, failure, description);

    http.get(
      $.Config.analyticsServer + '/vfs' + (path + property),
      {
        success: function(content) {
          var noDotFilter = function(e) { return e.charAt(0) != '.'; };
          var dotFilter   = function(e) { return e.charAt(0) == '.'; };
          var allFilter   = function(e) { return true; }

          var typeToFilter = {
            "path":   noDotFilter,
            "property":  dotFilter,
            "all":    allFilter
          };

          return callbacks.success(Util.filter(content, typeToFilter[type]));
        },

        failure: callbacks.failure
      },
      {tokenId: $.Config.tokenId }
    );
  }

  /**
   * Retrieves total counts of how often the specified property appeared in events
   * of the specified type.
   *
   * ReportGrid.propertyCount("/account/jdoe/emails/", {property: "delivery.status"});
   * > 2392
   */
  ReportGrid.propertyCount = function(path_, options, success, failure) {
    var path     = Util.sanitizePath(path_);
    var property = Util.sanitizeProperty(options.property);

    var description = 'Get total count for ' + path + property;

    http.get(
      $.Config.analyticsServer + '/vfs' + (path + property) + '/count',
      Util.createCallbacks(success, failure, description),
      Util.defaultQuery(options)
    );
  }

  /**
   * Retrieves time series counts of how often the specified property appeared
   * in events of the specified type.
   *
   * Options:
   *  * periodicity: ["minute", "hour", "day", "week", "month", "year", "none" | false] - The granularity of time that you want to see results at.
   *  * start - the start of the time period to query
   *  * end   - the end of the time period to query
   *  * groupBy: ["hour", "day", "week", "month", "year"] - An optional property; if specified, this will be used to batch the returned counts.
   *  * groups : "1,2,3" or [1,2,3] - An optional property (it only makes sense when used in conjunction with "groupBy"); when used filters the group by the specified values.
   *
   * ReportGrid.propertySeries("/atm-events/", {property: "transaction", periodicity: "hour"});
   * > [[{timestamp:1315954800000},10],[...]]
   *
   * Or, if using groupBy:
   * > {"type":"deltas", "zero":0, "data":{"0":293, "1": 386, ..., "59": 222}}
   */
  ReportGrid.propertySeries = function(path_, options, success, failure) {
    var path     = Util.sanitizePath(path_);
    var property = Util.sanitizeProperty(options.property);
    var peri     = options.periodicity || "eternity";
    var query    = Util.groupQuery(options);

    var description = 'Get series for property ' + path + property + ' (periodicity = ' + peri + ')';

    http.get(
      $.Config.analyticsServer + '/vfs' + (path + property) + '/series/' + peri,
      Util.createCallbacks(success, failure, description),
      query
    );
  }

  /**
   * Retrieves all values of the specified property throughout all time.
   *
   * ReportGrid.propertyValues("/customers/jdoe/blog-posts/1/", {property: "click.gender"});
   * > ["male", "female", "unknown"]
   */
  ReportGrid.propertyValues = function(path_, options, success, failure) {
    var path     = Util.sanitizePath(path_);
    var property = Util.sanitizeProperty(options.property);

    var description = 'Get all values of property ' + path + property;
    var bounds = Util.getBoundResults(options);
    http.get(
      $.Config.analyticsServer + '/vfs' + (path + property) + '/values' + bounds,
      Util.createCallbacks(success, failure, description),
      Util.defaultQuery(options)
    );
  }

  /**
   * Retrieves the total number of times the property was equal to the specified
   * value.
   *
   * ReportGrid.propertyValueCount("/customers/jdoe/blog-posts/1/", {property: "click.gender", value: "male"});
   * > 12329
   */
  ReportGrid.propertyValueCount = function(path_, options, success, failure) {
    var path     = Util.sanitizePath(path_);
    var property = Util.sanitizeProperty(options.property);
    var value    = options.value;

    var valueJson = JSON.stringify(value);

    var description = 'Get the count for ' + path + property + ' == ' + valueJson;

    http.get(
      $.Config.analyticsServer + '/vfs' + (path + property) + '/values/' + encodeURIComponent(valueJson) + '/count',
      Util.createCallbacks(success, failure, description),
      Util.defaultQuery(options)
    );
  }

  /**
   * Retrieves the time series count of when the property was equal to the
   * specified value.
   *
   * Options:
   *  * periodicity: ["minute", "hour", "day", "week", "month", "year", "none" | false] - The granularity of time that you want to see results at.
   *  * start - the start of the time period to query
   *  * end   - the end of the time period to query
   *  * groupBy: ["hour", "day", "week", "month", "year"] - An optional property; if specified, this will be used to batch the returned counts.
   *  * groups : "1,2,3" or [1,2,3] - An optional property (it only makes sense when used in conjunction with "groupBy"); when used filters the group by the specified values.
   *
   * ReportGrid.propertyValueSeries("/transactions/", {property: "click.gender", value: "male", periodicity: "hour"});
   * > {"type":"timeseries", "periodicity":"hour", "data":{"1239232323":293}}
   *
   * Or, if using groupBy:
   * > {"type":"deltas", "zero":0, "data":{"0":293, "1": 386, ..., "59": 222}}
   */
  ReportGrid.propertyValueSeries = function(path_, options, success, failure) {
    var path     = Util.sanitizePath(path_);
    var property = Util.sanitizeProperty(options.property);
    var value    = options.value;
    var peri     = options.periodicity || "eternity";
    var query    = Util.groupQuery(options);

    var valueJson = JSON.stringify(value);

    var description = 'Get the time series for ' + path + property + ' = ' + valueJson + ' (periodicity = ' + peri + ')';

    http.get(
      $.Config.analyticsServer + '/vfs' + (path + property) + '/values/' + encodeURIComponent(valueJson) + '/series/' + peri,
      Util.createCallbacks(success, failure, description),
      query
    );
  }

  /**
   * Searches across a range of conditions to retrieve a total count.
   *
   * ReportGrid.searchCount("/advertisers/Nike", {where: {"impression.carrier": "AT&T"}});
   * > 10
   */
  ReportGrid.searchCount = function(path_, options, success, failure) {
    var path = Util.sanitizePath(path_);

    var description = 'Select count from ' + path + ' where ' + JSON.stringify(options.where);

    http.post(
      $.Config.analyticsServer + '/search',
      {
        select: "count",
        from:   path,
        where:  options.where
      },
      Util.createCallbacks(success, failure, description),
      Util.defaultQuery(options)
    );
  }


  /**
   * Searches time series for events that meet the specified constraint. Note
   * that constraints may involve at most one event.
   *
   * Options:
   *
   *  * periodicity: ["minute", "hour", "day", "week", "month", "year", "none" | false] - The granularity of time that you want to see results at.
   *  * start - the start of the time period
   *  * end - the end of the time period
   *  * groupBy: ["hour", "day", "week", "month", "year"] - An optional property; if specified, this will be used to batch the returned counts.
   *                                                        For example, if you batch minute data by hour, then the result will contain a dataset
   *                                                        with 60 entries; the first entry will contain the sum of counts for the first minute
   *                                                        of each hour in the specified time range, etc.
   *  * groups : "1,2,3" or [1,2,3] - An optional property (it only makes sense when used in conjunction with "groupBy"); when used filters the group by the specified values.
   *
   * ReportGrid.searchSeries("/advertisers/Nike", {periodicity: "hour", where: {".impression.carrier": "AT&T"}});
   * > {"type":"timeseries", "periodicity":"hour", "data":{"1239232323":293, "234345468":222, ...}}
   *
   * Or, if using groupBy:
   * > {"type":"deltas", "zero":0, "data":{"0":293, "1": 386, ..., "59": 222}}
   */
  ReportGrid.searchSeries = function(path_, options, success, failure) {
    var path  = Util.sanitizePath(path_);
    var peri  = options.periodicity || "eternity";

    var description = 'Select series/' + peri + ' from ' + path + ' where ' + JSON.stringify(options.where);

    var ob = {
      select: "series/" + peri,
      from:   path,
      where:  options.where
    };
    var query = Util.groupQuery(options);
    
    http.post(
      $.Config.analyticsServer + '/search',
      ob,
      Util.createCallbacks(success, failure, description),
      query
    );
  }
  
  /**
   * Intersect time series for events that meet the specified constraint. Note
   * that constraints may involve at most one event.
   *
   * Options:
   *
   *  * properties - The property/value pairs that each selected event must have. You may specify properties up to the order defined by your token.
   *  * periodicity: ["minute", "hour", "day", "week", "month", "year", "none" | false] - The granularity of time that you want to see results at.
   *  * start - the start of the time period
   *  * end - the end of the time period
   *  * groupBy: ["hour", "day", "week", "month", "year"] - An optional property; if specified, this will be used to batch the returned counts.
   *                                                        For example, if you batch minute data by hour, then the result will contain a dataset
   *                                                        with 60 entries; the first entry will contain the sum of counts for the first minute
   *                                                        of each hour in the specified time range, etc.
   *  * groups : "1,2,3" or [1,2,3] - An optional property (it only makes sense when used in conjunction with "groupBy"); when used filters the group by the specified values.
   *
   * ReportGrid.intersect("/advertisers/Nike", {periodicity: "hour", properties: [{"property" : ".impression.platform", "limit" : 3, "order" : "descending"}]});
   * > {
   *     "iphone":    {"type":"timeseries", "periodicity":"hour", "data":{"1239232323":293, "234345468":222, ...}},
   *     "android":   {"type":"timeseries", "periodicity":"hour", "data":{"1239232323":155, "234345468":222, ...}}, 
   *     "blackberry":{"type":"timeseries", "periodicity":"hour", "data":{"1239232323":65, ...}}
   *   }
   *
   * Or, if using groupBy:
   * > {
   *     "iphone":    {"type":"deltas", "zero":0, "data":{"0":293, "1": 386, ..., "59": 222}},
   *     "android":   {"type":"deltas", "zero":0, "data":{"0":155, "1": 482, ..., "59": 333}}, 
   *     "blackberry":{"type":"deltas", "zero":0, "data":{"0":470, "1": 284, ..., "59": 333}}
   *   }
   */
  ReportGrid.intersect = function(path_, options, success, failure) {
    var path = Util.sanitizePath(path_);
    var peri = options.periodicity || "eternity";

    var description = 'Intersect series/' + peri + ' from ' + path + ' where ' + JSON.stringify(options.properties);
    
    var ob = {
      select:     peri == 'eternity' ? 'count' : "series/" + peri,
      from:       path,
      properties: options.properties
    };

    var query = Util.groupQuery(options);

    http.post(
      $.Config.analyticsServer + '/intersect',
      ob,
      Util.createCallbacks(success, failure, description),
      query
    );
  }
  
  ReportGrid.histogram = function(path_, options, success, failure) {
    var path = Util.sanitizePath(path_);
    var property = Util.sanitizeProperty(options.property);
    var description = 'Histogram ' + path + property;
    var query = Util.defaultQuery(options);
    var bounds = Util.getBoundResults(options);

    http.get(
      $.Config.analyticsServer + '/vfs' + path + property + '/histogram' + bounds,
      Util.createCallbacks(success, failure, description),
      query
    );
  }

  /** Lists all tokens.
   */
  ReportGrid.tokens = function(success, failure) {
    var http = $.Http();

    http.get(
      $.Config.analyticsServer + '/tokens/',
      Util.createCallbacks(success, failure, 'List all tokens'),
      {tokenId: $.Config.tokenId }
    );
  }

  ReportGrid.token = function(token, success, failure) {
    var http = $.Http();
    if(typeof(token) != "string")
    {
        failure = success;
        success = token;
        token = $.Config.tokenId;
    }
    http.get(
      $.Config.analyticsServer + '/tokens/' + token,
      Util.createCallbacks(success, failure, 'List all tokens'),
      {tokenId: $.Config.tokenId }
    );
  }

  /** Creates a new token.
   */
  ReportGrid.newToken = function(newToken, success, failure) {
    var http = $.Http();

    http.post(
      $.Config.analyticsServer + '/tokens/',
      newToken,
      Util.createCallbacks(success, failure, 'Create a token (' + JSON.stringify(newToken) + ')'),
      {tokenId: $.Config.tokenId }
    );
  }

  /** Deletes the token with the specified id.
   */
  ReportGrid.deleteToken = function(tokenId, success, failure) {
    var http = $.Http();

    http.remove(
      $.Config.analyticsServer + '/tokens/' + tokenId,
      Util.createCallbacks(success, failure, 'Delete token ' + tokenId),
      {tokenId: $.Config.tokenId }
    );
  }
})();


