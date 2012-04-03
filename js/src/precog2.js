/* Copyright (C) 2012 by Precog, Inc. All rights reserved.
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
 * use the Precog platform to provide some subset of its functionality.
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

// Precog core:
var Precog = window.Precog || {};

(function() {
  var Util = {
	  findScript: function(fragment) {
      var scripts = document.getElementsByTagName('SCRIPT');

      for (var i = 0; i < scripts.length; i++) {
        var script = scripts[i];
        var src = script.getAttribute('src');
        if (src && ((typeof fragment == "string" && src.indexOf(fragment) != -1) || src.match(fragment))) {
          return script;
        }
      }

      return undefined;
    },
    getConfiguration: function() {
      return Util.parseQueryParameters(Util.findScript(/precog[^\/.]*\.js/).getAttribute('src'));
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

    getProtocol: function() {
      var src = Util.findScript(/precog[^\/.]*\.js/).getAttribute('src');
      if(src && 'https:' == src.substr(0, 6))
        return 'https:';
      else
        return 'http:';
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
    }
  };

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
      var funcName = 'PrecogJsonpCallback' + random.toString();

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

  Precog.$ = {};

  var $ = Precog.$;

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
      analyticsServer: Util.getProtocol() + "//play2012v1.precog.io/v1",
      useJsonp : "true",
      enableLog : "false"
    }
  );

  $.Config.analyticsServer = Util.removeTrailingSlash($.Config.analyticsServer);

  $.Http = function() {
    return $.Bool(Precog.$.Config.useJsonp) ? Precog.$.Http.Jsonp : Precog.$.Http.Ajax;
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

  var http = $.Http();

  Precog.quirrel = function(query, success, failure) {
    var description = 'Quirrel query ' + query;

    http.get(
      $.Config.analyticsServer + '/vfs/',
      Util.createCallbacks(success, failure, description),
      { tokenId: $.Config.tokenId, q : query }
    );
  }

  if("undefined" != typeof ReportGrid && "undefined" != typeof ReportGrid.query)
  {
    // USTORE
    var C=(function(){var e,a,c,f,b,k,i,j,d;var g={setValue:function(l,m,n){if(e){if(n&&a){sessionStorage.setItem(l,m)}else{localStorage.setItem(l,m)}}else{if(c){if(n){i.setAttribute(l,m);i.save(d)}else{f.setAttribute(l,m);f.save(ieDb)}}}},getValue:function(m,n){var l="";if(e){if(n&&a){l=sessionStorage.getItem(m)}else{l=localStorage.getItem(m)}}else{if(c){if(n){i.load(d);l=i.getAttribute(m)}else{f.load(ieDb);l=f.getAttribute(m)}}}return l},deleteValue:function(l,m){if(e){this.setValue(l,null,m)}else{if(c){if(m){i.removeAttribute(l);i.save(d)}else{f.removeAttribute(l);f.save(ieDb)}}}},clearDB:function(l){if(e){if(l){sessionStorage.clear()}else{localStorage.clear()}}else{if(c){h.clearDB(l)}}}};var h={detectIE:function(){if(/MSIE (\d+\.\d+);/.test(navigator.userAgent)){var l=new Number(RegExp.$1);if(l>=5.5&&l<=8){return true}}return false},init:function(){var n=document.createElement("meta");n.name="save";n.content="userdata";document.getElementsByTagName("head").item(0).appendChild(n);var m=new Date().getTime();var l=document.createElement("div");b="ie-db-"+m;ieDb="userStorage";l.setAttribute("id",b);body.appendChild(l);f=document.getElementById(b);f.style.behavior="url('#default#userData')";f.style.display="none";if(window.name===null||window.name===undefined||window.name===""){window.name="ie-sesh-db-"+m}j=window.name;d=j;l=document.createElement("div");l.setAttribute("id",j);f.appendChild(l);i=document.getElementById(j);i.style.behavior="url('#default#userData')";i.style.display="none"},clearDB:function(r){var m=new Date().getTime(),t=document.createElement("div"),l=r?i:f,p=r?d:ieDb,s=l.xmlDocument,n=s.firstChild.attributes,q,o=n.length;while(0<=--o){q=n[o];l.removeAttribute(q.nodeName)}l.save(p)}};return{init:function(){if(typeof(window.localStorage)==="object"){e=true;try{if(typeof(window.sessionStorage)==="object"){a=true}}catch(l){a=false}}else{if(h.detectIE()){c=true;h.init()}}},setValue:function(l,m){g.setValue(l,m,false)},setSessionValue:function(l,m){g.setValue(l,m,true)},getValue:function(l){return g.getValue(l,false)},getSessionValue:function(l){return g.getValue(l,true)},deleteValue:function(l){g.deleteValue(l,false)},deleteSessionValue:function(l){g.deleteValue(l,true)},clearLocalStorage:function(){g.clearDB(false)},clearSessionStorage:function(){g.clearDB(true)},clearDOMStorage:function(){g.clearDB(false);g.clearDB(true)}}})();
    C.init();
    var VALUE_PREFIX = "PRECOG_Q_",
        DATE_PREFIX  = "PRECOG_D_",
        TIMEOUT = 60 * 5 * 1000;

    function idDate(id) { return DATE_PREFIX + id; }
    function idValue(id) { return VALUE_PREFIX + id; }

    function ids()
    {
      var len = VALUE_PREFIX.length,
          result = [],
          count = C.length;
      for(var i = 0; i < count; i++)
      {
        var key = C.key(i);
        if(key.substr(0, len) != VALUE_PREFIX)
          continue;
        result.push(key.substr(len));
      }
      return result;
    }

    function delayedCleanup(id)
    {
      setTimeout(function() { cacheRemove(id); }, TIMEOUT);
    }

    function cacheRemove(id)
    {
      C.deleteValue(idDate(id));
      C.deleteValue(idValue(id));
    }

    function cacheGet(id)
    {
      clearValueIfOld(id);
      var v = C.getValue(idValue(id));
      if(v) 
      {
        delayedCleanup(id);
        return JSON.parse(v);
      } else {
        return null;
      }
    }

    function cacheSet(id, value)
    {
      C.setValue(idDate(id), Date.now().getTime());
      C.setValue(idValue(id), JSON.stringify(value));
    }

    function clearValueIfOld(id)
    {
      var idd = idDate(id);
      var v = C.getValue(idd);
      if(null == v)
        return;
      if(v < Date.now().getTime() - TIMEOUT * 1000)
      {
        C.deleteValue(idd);
        C.deleteValue(idValue(id));
      }
    }

    function cleanOld()
    {
      var list = ids();
      for(var i = 0; i < list.length; i++)
        clearValueIfOld()
    }

    function format(template, args)
    {
      for(key in args)
      {
        template = template.split('${'+key+'}').join(args[key]);
      }
      return template;
    }

    function uid(s)
    {
      s = s.replace(/\s+/gi, " ");
      return haxe.Md5.encode(s);
    }

    cleanOld();

    var queue = {};

    function cachedLoader(query)
    {
      var id = uid(query);
      return function(handler) {
        var val = cacheGet(id);
        if(val)
        {
          handler(val);
        } else if(queue[id])
        {
          queue[id].push(handler);
        } else {
          queue[id] = [];
          Precog.quirrel(query, function(data) {
            cacheSet(id, data);
            delayedCleanup(id);
            handler(data);
            for(var i = 0; i < queue[id].length; i++)
            {
              queue[id][i](data);
            }
            delete queue[id];
          });
        }
      }
    }

    ReportGrid.query.quirrel = function(query, params) {
      return ReportGrid.query.load(cachedLoader(query));
    };
    rg.query.BaseQuery.prototype.quirrel =
    rg.query.Query.prototype.quirrel =
    rg.query.ReportGridBaseQuery.prototype.quirrel =
    rg.query.ReportGridQuery.prototype.quirrel = function(query) {
      return this.data({}).stackCross().asyncEach(function(data, handler) {
        var q = format(query, data);
        cachedLoader(q)(handler);
      });
    };
  }
})();