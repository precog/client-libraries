(function (definition) {
    /*jshint strict: false*/

    // Montage Require
    if (typeof bootstrap === "function") {
        bootstrap("precog", definition);

    // CommonJS
    } else if (typeof exports === "object") {
        module.exports = definition();

    // RequireJS
    } else if (typeof define === "function") {
        define(definition);

    // SES (Secure EcmaScript)
    } else if (typeof ses !== "undefined") {
        if (!ses.ok()) {
            return;
        } else {
            ses.makePrecog = definition;
        }
    // <script>
    } else {
        precog = definition();
        // TODO, add compatibility layer with current implementation
    }
})(function(){
  "use strict";

  var util = {
    constants : {
      PORT_HTTP  : 80,
      PORT_HTTPS : 443
    },
    _nodejs : {
      ajax : function(request) {
        complete_request(request);
        var protocol = request.protocol;
        delete request.protocol;
        var deferred = new precog.Deferred(),
            http     = require(protocol),
            req      = http.request(request, function(res) {

              var body = '';
              res.setEncoding('utf8');
              res.on('data', function (chunk) {
                body += chunk;
              });
              res.on('end', function () {
                deferred.fulfill(body);
              });
            });
        req.on('error', deferred.reject);
        req.end();
        return deferred.promise;
      }
    },
    extend : function(dst, src) {
      for(key in src) {
        dst[key] = src[key];
      }
    },
    eachField : function(ob, handler) {
      if(!ob) return;
      for(var key in ob) {
        if(!ob.hasOwnProperty(key)) continue;
        handler(key, ob[key]);
      }
    },
    objectMap : function(ob, handler) {
      var result = [];
      util.eachField(ob, function(key, value) {
        result.push(handler(key, value));
      });
      return result;
    },
    stripSlashes : function(s) {
      if(s.substr(0, 1) === "/")
        s = s.substr(1);
      if(s.substr(-1) === "/")
        s = s.substr(0, s.length - 1);
      return s;
    },
    actionUrl : function(api, service, action, qs) {
      var buffer = [service],
          t;
      if("object" === typeof action) {
        qs = action;
        action = null;
      }
      if(t = api.config("version")) {
        buffer.push(t);
      }
      if(action) {
        buffer.push(action);
      }
      t = api.config("basePath");
      buffer.push(t === "/" ? "" : t);
      
      var querystring = util.objectMap(qs, function(key, value) {
        return encodeURIComponent(key) + "=" + encodeURIComponent(value);
      }).join("&");

      return "/" + buffer.join("/") + (querystring ? "?" + querystring : "");
    }
  };
  util.ajax = util._nodejs.ajax;

  var precog = {};

  function complete_request(request) {
    if(!request.protocol) request.protocol = "http";
    if(!request.port)     request.port = request.protocol === "http" ? util.constants.PORT_HTTP : util.constants.PORT_HTTPS;
    if(!request.method)   request.method = "GET";
    if(!request.path)     request.path = "/";
    
    if(!request.headers)  request.headers = {};
//    if(!request.headers["Accept"]) 
//      request.headers["Accept"] = "applicaiton/json;text/plain";
//    if(!request.headers["Connection"]) 
//      request.headers["Connection"] = "close";
//    if(!request.headers['Content-length'])
//      request.headers['Content-length'] = '0';
//    if(!request.headers['Expect'])
//      request.headers['Expect'] = '100-continue';
  }

  function assemble_service(config) {
    var service = config("protocol") + "://" + config("hostname");
      if(config("protocol") === 'http' && config("port") !== util.constants.PORT_HTTP)
        service += ":" + config("port"); 
      else if(config("protocol") === 'https' && config("port") !== util.constants.PORT_HTTPS)
        service += ":" + config("port"); 
      return service;
  }

  var config_setters = {
    analyticsService : {
      filter : function(value) {
        return util.stripSlashes(value);
      },
      after  : function(config) {
        var service  = config("analyticsService"),
            protocol = service.split("://")[0],
            hostname = service.split("://").slice(1).join("://"),
            pos      = hostname.indexOf(":");

        config("protocol", protocol, true);
        if(pos >= 0){
          config("port", hostname.substring(pos+1), true);
          config("hostname", hostname.substring(0, pos), true);
        } else {
          config("port", config("protocol") === "http" ? util.constants.PORT_HTTP: util.constants.PORT_HTTPS, true);
          config("hostname", hostname, true);
        }
      }
    },
    hostname : {
      filter : function(value) { return util.stripSlashes(value);},
      after  : assemble_service
    },
    port     : {
      filter : function(value) { return parseInt(value); },
      after  : assemble_service
    },
    protocol : {
      filter : function(value) { return value.toLowerCase(); },
      after  : function(config) {
        var protocol = config("protocol"),
            port = config("port");
        if(port === util.constants.PORT_HTTP || port === util.constants.PORT_HTTPS) {
          port = protocol === "http" ? util.constants.PORT_HTTP : util.constants.PORT_HTTPS;
          config("port", port, true);
        }
        assemble_service(config);
      }
    },
    basePath : {
      filter : function(value) {
        if(!value) {
          value = "/";
        } else {
          value = util.stripSlashes(value) + "/";
        }
        return value;
      }
    },
    apiKey   : {},
    version  : {}
  };



  precog.Api = function(options){
    options = options || {};
    var config_params = {};

    function config(key, value, silent){
      if("undefined" === typeof value){
        return config_params[key];
      } else {
        if(!config_setters[key]) throw "invalid config parameter: " + key;
        config_params[key] = config_setters[key].filter && config_setters[key].filter(value) || value;
        if(!silent && config_setters[key].after)
          config_setters[key].after(config);
      }
    }

    this.config = config;

    this.query = function(query, options){
      options = options || {};
      var description = 'Precog query ' + query,
          parameters = { apiKey : config("apiKey"), q : query };

      if(options.limit)
        parameters.limit = options.limit;
      if(options.basePath)
        parameters.basePath = options.basePath;
      if(options.skip)
        parameters.skip = options.skip;
      if(options.order)
        parameters.order = options.order;
      if(options.sortOn)
        parameters.sortOn = JSON.stringify(options.sortOn);
      if(options.sortOrder)
        parameters.sortOrder = options.sortOrder;

      parameters.q = query;

      var request = {
        protocol : config("protocol"),
        hostname : config("hostname"),
        method   : "GET",
        port     : config("port"),
        path     : util.actionUrl(this, "analytics", "fs", parameters)
      };

      return util.ajax(request).then(JSON.parse);
    }

    config("basePath", null, true);
    config("version", "v1", true);
    util.eachField(options, config);
  };

  /***
    based on https://github.com/ForbesLindesay/promises-a
  */
  precog.Deferred = function() {
    var resolved  = false,
        fulfilled = false,
        val,
        waiting = [],
        running = false,
        prom = {then: then, valueOf: valueOf, done: done}

    function next(skipTimeout) {
      if (waiting.length) {
        running = true;
        waiting.shift()(skipTimeout || false)
      } else {
        running = false;
      }
    }
    function then(cb, eb) {
      var def = new precog.Deferred();
      function done(skipTimeout) {
        var callback = fulfilled ? cb : eb;
        function timeoutDone() {
          var value;
          try {
            value = callback(val)
          } catch (ex) {
            def.reject(ex)
            return next();
          }
          def.fulfill(value);
          next(true);
        }
        if (typeof callback === 'function') {
          if (skipTimeout)
            timeoutDone();
          else
            setTimeout(timeoutDone, 0);
        } else if (fulfilled) {
          def.fulfill(val);
          next(skipTimeout);
        } else {
          def.reject(val);
          next(skipTimeout);
        }
      }
      waiting.push(done);
      if (resolved && !running) {
        next();
      }
      return def.promise;
    }
    function resolve(success, value) {
      if (resolved) return;
      if (success  && value && typeof value.then === 'function') {
        value.then(fulfill, reject);
        return;
      }
      resolved = true;
      fulfilled = success;
      val = value;
      next();
    }
    function fulfill(val) {
      resolve(true, val);
    }
    function reject(err) {
      resolve(false, err);
    }

    function valueOf() {
      return fulfilled ? val : prom;
    }

    function done(cb, eb) {
      var p = this; // support 'hot' promises
      if (cb || eb) {
        p = p.then(cb, eb);
      }
      p.then(null, function (reason) {
        setTimeout(function () {
          throw reason;
        }, 0)
      });
    }

    this.promise = prom;
    this.fulfill = fulfill;
    this.reject  = reject;
  }

  precog.util = util;

  return precog;
});