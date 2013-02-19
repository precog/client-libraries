(function(exports){
  if("undefined" === typeof this.define){
    this.define = function(callback){
      var value = callback(this.require, exports);
      if("undefined" !== typeof value)
        exports['precog'] = value;
    }
  }

  var util = {
    ajax_nodejs : function(options, success, failure) {
      var http = require(options.protocol);
      var req = https.request(options, function(res) {
        res.on('data', success);
      });

      req.on('error', failure);
      req.end();
    },
    actionUrl: function(api, action, options) {
      var version = (options && options.version) || api.config("version");
      return "/" + (version ? version + "/" : "") + (action ? action + "/" : "");
    }
  };
  util.ajax = ajax_nodejs;

    //inside this is where exposed functions go

  define(function (require, exports) {
    function assemble_service(config) {
      var service = 
            config.protocol +
            "://" +
            config.host;
        if(config.protocol === 'http' && config.port != 80)
          service += ":" + config.port; 
        else if(config.protocol === 'https' && config.port != 8081)
          service += ":" + config.port; 
        return service;
    }

    var config_setters = {
      analyticsService : function(value, config) {
        config.protocol = value.split("://").shift().toLowerCase();
        config.host = value.split("://").pop();
        var pos = config.host.lastIndexOf(":");
        if(pos >= 0){
          config.port = config.host.substring(pos+1);
          config.host = config.host.substring(0, pos);
        } else {
          config.host = config.protocol === "http" ? "80": "8081";
        }
        return value;
      },
      host             : function(value, config) {
        config.host = value;
        config.analyticsService = assemble_service(config); 
        return value;
      },
      port             : function(value, config) {
        config.port = value;
        config.analyticsService = assemble_service(config); 
        return value;
      },
      protocol         : function(value, config) {
        config.protocol = value.toLowerCase();
        config.analyticsService = assemble_service(config); 
        return value;
      },
      basePath         : function(value, config) { return value; },
      apiKey           : function(value, config) { return value; },
      version          : function(value, config) { return value; }
    };

    exports.Api = function(options){
      options = options || {};
      var config_params = {};

      this.config = function(key, value){
        if("undefined" === typeof value){
          return config_params[key];
        } else {
          if(!config_setters[key]) throw "invalid config parameter: " + key;
          config_params[key] = config_setters[key](value, config_params);
        }
      }

      this.query = function(query, options){
        options = options || {};
        var description = 'Precog query ' + query,
            parameters = { apiKey : this.config("apiKey") q : query };

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

        return http.get(
          Util.actionUrl("analytics", "fs", options) + Util.actionPath(null, options),
          Util.createCallbacks(success, failure, description),
          parameters
        );
      }

      this.config("version", "v1");
      for(var key in options) {
        if(!options.hasOwnProperty(key)) continue;
        this.config(key, options[key]);
      }
    };
  }
  );

})(typeof exports === 'undefined'? this['precog']={}: exports);