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

(function() {
  var Precog = window.Precog = (window.Precog || {});
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
    getPageConfiguration: function() {
      return Util.parseQueryParameters(window.location.href);
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

      if (queries.length === 0) return url;
      else return url + suffix + queries.join('&');
    },

    getConsole: function(enabled) {
      var console = enabled ? window.console : undefined;
      if (!console) {
        console = {};

        console.log   = function() {};
        console.debug = function() {};
        console.info  = function() {};
        console.warn  = function() {};
        console.error = function() {};
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
        };
      };

      var failureFn = function(fn, msg) {
        if (fn) return fn;
        else return function(code, reason) {
          $.Log.error('Failure: ' + msg + ': code = ' + code + ', reason = ' + reason);
        };
      };

      return {
        success: successFn(success, msg),
        failure: failureFn(failure, msg)
      };
    },

    removeTrailingSlash: function(path) {
      if (path.length === 0) return path;
      else if (path.substr(path.length - 1) == "/") return path.substr(0, path.length - 1);
      else return path;
    },

    removeDuplicateSlashes: function(path) {
      return path.replace(/[\/]+/g, "/");
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
      };

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
      };

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
      };

      var extraQuery = {};

      extraQuery.method   = method;

      for (var _ in headers) { extraQuery.headers = JSON.stringify(headers); break; }

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
      };
    }
  };

  Precog.$ = {};

  var $ = Precog.$;

  $.Util = Util;

  $.PageConfig = Util.getPageConfiguration();
  $.Config = Util.getConfiguration();

  $.Extend = function(object, extensions) {
    for (var name in extensions) {
      if (object[name] === undefined) {
        object[name] = extensions[name];
      }
    }
  };

  $.Bool = function(v) {
    return v === true || v === 1 || (v = (""+v).toLowerCase()) == "true" || v == "on" || v == "1";
  };

  $.Extend($.Config,
    {
      analyticsService: Util.getProtocol() + "//api.precog.io/v1",
      useJsonp : "true",
      enableLog : "false"
    }
  );

  $.Config.analyticsService = Util.removeTrailingSlash($.PageConfig.analyticsService || $.Config.analyticsService);
  $.Config.tokenId = $.PageConfig.tokenId || $.Config.tokenId;

  $.Http = function() {
    return $.Bool(Precog.$.Config.useJsonp) ? Precog.$.Http.Jsonp : Precog.$.Http.Ajax;
  };

  $.Http.Ajax  = Network.createHttpInterface(Network.doAjaxRequest);
  $.Http.Jsonp = Network.createHttpInterface(Network.doJsonpRequest);

  var console = Util.getConsole($.Bool($.Config.enableLog));

  $.Log = {
    log:    function(text) { console.log(text);   },
    debug:  function(text) { console.debug(text); },
    info:   function(text) { console.info(text);  },
    warn:   function(text) { console.warn(text);  },
    error:  function(text) { console.error(text); }
  };

  var http = $.Http();

  var executeQuery = function(query, success, failure) {
    var description = 'Precog query ' + query;

    http.get(
      $.Config.analyticsService + '/vfs/',
      Util.createCallbacks(success, failure, description),
      { tokenId: $.Config.tokenId, q : query }
    );
  };

  // USTORE
  /* Copyright (c) 2010-2012 Marcus Westin */
  $.Store = (function(){function h(){try{return d in b&&b[d]}catch(a){return!1}}function i(){try{return e in b&&b[e]&&b[e][b.location.hostname]}catch(a){return!1}}var a={},b=window,c=b.document,d="localStorage",e="globalStorage",f="__storejs__",g;a.disabled=!1,a.set=function(a,b){},a.get=function(a){},a.remove=function(a){},a.clear=function(){},a.transact=function(b,c,d){var e=a.get(b);d||(d=c,c=null),typeof e=="undefined"&&(e=c||{}),d(e),a.set(b,e)},a.getAll=function(){},a.getKeys=function(){},a.serialize=function(a){return JSON.stringify(a)},a.deserialize=function(a){return typeof a!="string"?undefined:JSON.parse(a)};if(h())g=b[d],a.set=function(b,c){if(c===undefined)return a.remove(b);g.setItem(b,a.serialize(c))},a.get=function(b){return a.deserialize(g.getItem(b))},a.remove=function(a){g.removeItem(a)},a.clear=function(){g.clear()},a.getAll=function(){var b={};for(var c=0;c<g.length;++c){var d=g.key(c);b[d]=a.get(d)}return b},a.getKeys=function(){var a=[];for(var b=0;b<g.length;++b)a.push(g.key(b));return a};else if(i())g=b[e][b.location.hostname],a.set=function(b,c){if(c===undefined)return a.remove(b);g[b]=a.serialize(c)},a.get=function(b){return a.deserialize(g[b]&&g[b].value)},a.remove=function(a){delete g[a]},a.clear=function(){for(var a in g)delete g[a]},a.getAll=function(){var b={};for(var c=0;c<g.length;++c){var d=g.key(c);b[d]=a.get(d)}return b},a.getKeys=function(){var a=[];for(var b=0;b<g.length;++b)a.push(g.key(b));return a};else if(c.documentElement.addBehavior){var j,k;try{k=new ActiveXObject("htmlfile"),k.open(),k.write('<script>document.w=window</script><iframe src="/favicon.ico"></frame>'),k.close(),j=k.w.frames[0].document,g=j.createElement("div")}catch(l){g=c.createElement("div"),j=c.body}function m(b){return function(){var c=Array.prototype.slice.call(arguments,0);c.unshift(g),j.appendChild(g),g.addBehavior("#default#userData"),g.load(d);var e=b.apply(a,c);return j.removeChild(g),e}}function n(a){return"_"+a}a.set=m(function(b,c,e){c=n(c);if(e===undefined)return a.remove(c);b.setAttribute(c,a.serialize(e)),b.save(d)}),a.get=m(function(b,c){return c=n(c),a.deserialize(b.getAttribute(c))}),a.remove=m(function(a,b){b=n(b),a.removeAttribute(b),a.save(d)}),a.clear=m(function(a){var b=a.XMLDocument.documentElement.attributes;a.load(d);for(var c=0,e;e=b[c];c++)a.removeAttribute(e.name);a.save(d)}),a.getAll=m(function(b){var c=b.XMLDocument.documentElement.attributes;b.load(d);var e={};for(var f=0,g;g=c[f];++f)e[g]=a.get(g);return e}),a.getKeys=m(function(a){var b=a.XMLDocument.documentElement.attributes;a.load(d);var c=[];for(var e=0,f;f=b[e];++e)c.push(f);return c})}try{a.set(f,f),a.get(f)!=f&&(a.disabled=!0),a.remove(f)}catch(l){a.disabled=!0}return typeof module!="undefined"&&typeof module!="function"?module.exports=a:typeof define=="function"&&define.amd&&define(a),a})();
//  try {
//    delete window["store"];
//  } catch(e) { window["store"] = null; }

    // MD5
    var Md5 = function(){};
    Md5.prototype = {
  bitOR: function(a,b) { var lsb = a & 1 | b & 1; var msb31 = a >>> 1 | b >>> 1; return msb31 << 1 | lsb; },
  bitXOR: function(a,b) { var lsb = a & 1 ^ b & 1; var msb31 = a >>> 1 ^ b >>> 1; return msb31 << 1 | lsb; },
  bitAND: function(a,b) { var lsb = a & 1 & (b & 1); var msb31 = a >>> 1 & b >>> 1; return msb31 << 1 | lsb; },
  addme: function(x,y) { var lsw = (x & 65535) + (y & 65535); var msw = (x >> 16) + (y >> 16) + (lsw >> 16); return msw << 16 | lsw & 65535; },
  rhex: function(num) { var str = ""; var hex_chr = "0123456789abcdef"; var _g = 0; while(_g < 4) { var j = _g++; str += hex_chr.charAt(num >> j * 8 + 4 & 15) + hex_chr.charAt(num >> j * 8 & 15); } return str; },
  str2blks: function(str) { var nblk = (str.length + 8 >> 6) + 1, blks = [], _g1 = 0, _g = nblk * 16; while(_g1 < _g) { blks[++_g1] = 0; } var i = 0; while(i < str.length) { blks[i >> 2] |= str.charCodeAt(i) << (str.length * 8 + i) % 4 * 8; i++; } blks[i >> 2] |= 128 << (str.length * 8 + i) % 4 * 8; var l = str.length * 8; var k = nblk * 16 - 2; blks[k] = l & 255; blks[k] |= (l >>> 8 & 255) << 8; blks[k] |= (l >>> 16 & 255) << 16; blks[k] |= (l >>> 24 & 255) << 24; return blks; },
  rol: function(num,cnt) { return num << cnt | num >>> 32 - cnt; },
  cmn: function(q,a,b,x,s,t)  { return this.addme(this.rol(this.addme(this.addme(a,q),this.addme(x,t)),s),b); },
  ff: function(a,b,c,d,x,s,t) { return this.cmn(this.bitOR(this.bitAND(b,c),this.bitAND(~b,d)),a,b,x,s,t); },
  gg: function(a,b,c,d,x,s,t) { return this.cmn(this.bitOR(this.bitAND(b,d),this.bitAND(c,~d)),a,b,x,s,t); },
  hh: function(a,b,c,d,x,s,t) { return this.cmn(this.bitXOR(this.bitXOR(b,c),d),a,b,x,s,t); },
  ii: function(a,b,c,d,x,s,t) { return this.cmn(this.bitXOR(c,this.bitOR(b,~d)),a,b,x,s,t); },
  doEncode: function(str) { var x = this.str2blks(str),a = 1732584193,b = -271733879,c = -1732584194,d = 271733878,step,i = 0; while(i < x.length) { var olda = a;var oldb = b;var oldc = c;var oldd = d;step = 0;a = this.ff(a,b,c,d,x[i],7,-680876936);d = this.ff(d,a,b,c,x[i + 1],12,-389564586);c = this.ff(c,d,a,b,x[i + 2],17,606105819);b = this.ff(b,c,d,a,x[i + 3],22,-1044525330);a = this.ff(a,b,c,d,x[i + 4],7,-176418897);d = this.ff(d,a,b,c,x[i + 5],12,1200080426);c = this.ff(c,d,a,b,x[i + 6],17,-1473231341);b = this.ff(b,c,d,a,x[i + 7],22,-45705983);a = this.ff(a,b,c,d,x[i + 8],7,1770035416);d = this.ff(d,a,b,c,x[i + 9],12,-1958414417);c = this.ff(c,d,a,b,x[i + 10],17,-42063);b = this.ff(b,c,d,a,x[i + 11],22,-1990404162);a = this.ff(a,b,c,d,x[i + 12],7,1804603682);d = this.ff(d,a,b,c,x[i + 13],12,-40341101);c = this.ff(c,d,a,b,x[i + 14],17,-1502002290);b = this.ff(b,c,d,a,x[i + 15],22,1236535329);a = this.gg(a,b,c,d,x[i + 1],5,-165796510);d = this.gg(d,a,b,c,x[i + 6],9,-1069501632);c = this.gg(c,d,a,b,x[i + 11],14,643717713);b = this.gg(b,c,d,a,x[i],20,-373897302);a = this.gg(a,b,c,d,x[i + 5],5,-701558691);d = this.gg(d,a,b,c,x[i + 10],9,38016083);c = this.gg(c,d,a,b,x[i + 15],14,-660478335);b = this.gg(b,c,d,a,x[i + 4],20,-405537848);a = this.gg(a,b,c,d,x[i + 9],5,568446438);d = this.gg(d,a,b,c,x[i + 14],9,-1019803690);c = this.gg(c,d,a,b,x[i + 3],14,-187363961);b = this.gg(b,c,d,a,x[i + 8],20,1163531501);a = this.gg(a,b,c,d,x[i + 13],5,-1444681467);d = this.gg(d,a,b,c,x[i + 2],9,-51403784);c = this.gg(c,d,a,b,x[i + 7],14,1735328473);b = this.gg(b,c,d,a,x[i + 12],20,-1926607734);a = this.hh(a,b,c,d,x[i + 5],4,-378558);d = this.hh(d,a,b,c,x[i + 8],11,-2022574463);c = this.hh(c,d,a,b,x[i + 11],16,1839030562);b = this.hh(b,c,d,a,x[i + 14],23,-35309556);a = this.hh(a,b,c,d,x[i + 1],4,-1530992060);d = this.hh(d,a,b,c,x[i + 4],11,1272893353);c = this.hh(c,d,a,b,x[i + 7],16,-155497632);b = this.hh(b,c,d,a,x[i + 10],23,-1094730640);a = this.hh(a,b,c,d,x[i + 13],4,681279174);d = this.hh(d,a,b,c,x[i],11,-358537222);c = this.hh(c,d,a,b,x[i + 3],16,-722521979);b = this.hh(b,c,d,a,x[i + 6],23,76029189);a = this.hh(a,b,c,d,x[i + 9],4,-640364487);d = this.hh(d,a,b,c,x[i + 12],11,-421815835);c = this.hh(c,d,a,b,x[i + 15],16,530742520);b = this.hh(b,c,d,a,x[i + 2],23,-995338651);a = this.ii(a,b,c,d,x[i],6,-198630844);d = this.ii(d,a,b,c,x[i + 7],10,1126891415);c = this.ii(c,d,a,b,x[i + 14],15,-1416354905);b = this.ii(b,c,d,a,x[i + 5],21,-57434055);a = this.ii(a,b,c,d,x[i + 12],6,1700485571);d = this.ii(d,a,b,c,x[i + 3],10,-1894986606);c = this.ii(c,d,a,b,x[i + 10],15,-1051523);b = this.ii(b,c,d,a,x[i + 1],21,-2054922799);a = this.ii(a,b,c,d,x[i + 8],6,1873313359);d = this.ii(d,a,b,c,x[i + 15],10,-30611744);c = this.ii(c,d,a,b,x[i + 6],15,-1560198380);b = this.ii(b,c,d,a,x[i + 13],21,1309151649);a = this.ii(a,b,c,d,x[i + 4],6,-145523070);d = this.ii(d,a,b,c,x[i + 11],10,-1120210379);c = this.ii(c,d,a,b,x[i + 2],15,718787259);b = this.ii(b,c,d,a,x[i + 9],21,-343485551);a = this.addme(a,olda);b = this.addme(b,oldb);c = this.addme(c,oldc);d = this.addme(d,oldd);i += 16; } return this.rhex(a) + this.rhex(b) + this.rhex(c) + this.rhex(d); }
};
  $.Md5 = function(s) { return new Md5().doEncode(s); };

  // CACHE
  Precog.cache = (function(){
    var VALUE_PREFIX = "PRECOG_Q_",
        DATE_PREFIX  = "PRECOG_D_",
        enabled,
        timeout = 300000; // 5 minutes

    function idDate(id) { return DATE_PREFIX + id; }
    function idValue(id) { return VALUE_PREFIX + id; }
    function ids()
    {
      var keys = $.Store.getKeys(),
          len = VALUE_PREFIX.length,
          result = [],
          count = keys.length;
      for(var i = 0; i < count; i++)
      {
        var key = keys[i];
        if(key.substr(0, len) != VALUE_PREFIX)
          continue;
        result.push(key.substr(len));
      }
      return result;
    }
    function delayedCleanup(id)
    {
      setTimeout(function() { cacheRemove(id); }, timeout);
    }
    function cacheRemove(id)
    {
      $.Store.remove(idDate(id));
      $.Store.remove(idValue(id));
    }
    function cacheGet(id)
    {
      clearValueIfOld(id);
      var v = $.Store.get(idValue(id));
      if(v)
      {
        delayedCleanup(id);
        return v;
      } else {
        return null;
      }
    }
    function cacheSet(id, value)
    {
      $.Store.set(idDate(id), new Date().getTime());
      $.Store.set(idValue(id), value);
    }

    function clearValueIfOld(id)
    {
      var idd = idDate(id);
      var v = $.Store.get(idd);
      if(!v)
        return;
      if(v < new Date().getTime() - timeout * 1000)
      {
        $.Store.remove(idd);
        $.Store.remove(idValue(id));
      }
    }
    function cleanOld()
    {
      var list = ids();
      for(var i = 0; i < list.length; i++)
        clearValueIfOld(list[i]);
    }
    function clearAll()
    {
      var list = ids();
      for(var i = 0; i < list.length; i++)
        cacheRemove(list[i]);
    }
    function format(template, args)
    {
      for(var key in args)
      {
        template = template.split('${'+key+'}').join(args[key]);
      }
      return template;
    }
    function uid(s)
    {
      s = s.replace(/\s+/gi, " ");
      return $.Md5(s);
    }
    var queue = {};
    function executeCachedQuery(query, success, failure)
    {
      var id = uid(query),
          val = cacheGet(id);
      if(val)
      {
        success(val);
      } else if(queue[id]) {
        queue[id].push(success);
      } else {
        queue[id] = [];
        executeQuery(query, function(data) {
          cacheSet(id, data);
          delayedCleanup(id);
          success(data);
          for(var i = 0; i < queue[id].length; i++)
          {
            queue[id][i](data);
          }
          delete queue[id];
        }, failure);
      }
    }

    cleanOld();

    return {
      isEnabled : function() {
        return enabled;
      },
      enable : function() {
        if($.Store.disabled)
        {
          this.disable();
        } else {
          enabled = true;
          Precog.query = executeCachedQuery;
        }
      },
      disable : function() {
        enabled = false;
        Precog.query = executeQuery;
      },
      setTimeout : function(t) {
        timeout = +t;
      },
      cachedResults : function() {
        return ids().length;
      },
      cachedIds : function() {
        return ids();
      },
      clear : function() {
        clearAll();
      }
    };
  })();

  Precog.cache.enable();

  if(window.ReportGrid && window.ReportGrid.query)
  {
    var r = window.ReportGrid;
    r.query.precog = function(q) {
      return r.query.load(function(handler) {
        Precog.query(q, handler);
      });
    };

    var f = function(q) {
      return this.data({}).stackCross().asyncEach(function(data, handler) {
        Precog.query(format(q, data), handler);
      });
    };
    /*
    var pk = r.$.pk;
    pk.rg_query_BaseQuery.prototype.precog = pk.rg_query_Query.prototype.precog = f;
    if(pk.rg_query_ReportGridBaseQuery)
      pk.rg_query_ReportGridBaseQuery.prototype.precog = pk.rg_query_ReportGridQuery.prototype.precog = f;
    */
  }
})();