// JSON
(function($){var escapeable=/["\\\x00-\x1f\x7f-\x9f]/g,meta={'\b':'\\b','\t':'\\t','\n':'\\n','\f':'\\f','\r':'\\r','"':'\\"','\\':'\\\\'};$.toJSON=typeof JSON==='object'&&JSON.stringify?JSON.stringify:function(o){if(o===null){return'null';}
var type=typeof o;if(type==='undefined'){return undefined;}
if(type==='number'||type==='boolean'){return''+o;}
if(type==='string'){return $.quoteString(o);}
if(type==='object'){if(typeof o.toJSON==='function'){return $.toJSON(o.toJSON());}
if(o.constructor===Date){var month=o.getUTCMonth()+1,day=o.getUTCDate(),year=o.getUTCFullYear(),hours=o.getUTCHours(),minutes=o.getUTCMinutes(),seconds=o.getUTCSeconds(),milli=o.getUTCMilliseconds();if(month<10){month='0'+month;}
if(day<10){day='0'+day;}
if(hours<10){hours='0'+hours;}
if(minutes<10){minutes='0'+minutes;}
if(seconds<10){seconds='0'+seconds;}
if(milli<100){milli='0'+milli;}
if(milli<10){milli='0'+milli;}
return'"'+year+'-'+month+'-'+day+'T'+
hours+':'+minutes+':'+seconds+'.'+milli+'Z"';}
if(o.constructor===Array){var ret=[];for(var i=0;i<o.length;i++){ret.push($.toJSON(o[i])||'null');}
return'['+ret.join(',')+']';}
var name,val,pairs=[];for(var k in o){type=typeof k;if(type==='number'){name='"'+k+'"';}else if(type==='string'){name=$.quoteString(k);}else{continue;}
type=typeof o[k];if(type==='function'||type==='undefined'){continue;}
val=$.toJSON(o[k]);pairs.push(name+':'+val);}
return'{'+pairs.join(',')+'}';}};$.evalJSON=typeof JSON==='object'&&JSON.parse?JSON.parse:function(src){return eval('('+src+')');};$.secureEvalJSON=typeof JSON==='object'&&JSON.parse?JSON.parse:function(src){var filtered=src.replace(/\\["\\\/bfnrtu]/g,'@').replace(/"[^"\\\n\r]*"|true|false|null|-?\d+(?:\.\d*)?(?:[eE][+\-]?\d+)?/g,']').replace(/(?:^|:|,)(?:\s*\[)+/g,'');if(/^[\],:{}\s]*$/.test(filtered)){return eval('('+src+')');}else{throw new SyntaxError('Error parsing JSON, source is not valid.');}};$.quoteString=function(string){if(string.match(escapeable)){return'"'+string.replace(escapeable,function(a){var c=meta[a];if(typeof c==='string'){return c;}
c=a.charCodeAt();return'\\u00'+Math.floor(c/16).toString(16)+(c%16).toString(16);})+'"';}
return'"'+string+'"';};})(jQuery);

// USTORE
var USTORE=(function(){var e,a,c,f,b,k,i,j,d;var g={setValue:function(l,m,n){if(e){if(n&&a){sessionStorage.setItem(l,m)}else{localStorage.setItem(l,m)}}else{if(c){if(n){i.setAttribute(l,m);i.save(d)}else{f.setAttribute(l,m);f.save(ieDb)}}}},getValue:function(m,n){var l="";if(e){if(n&&a){l=sessionStorage.getItem(m)}else{l=localStorage.getItem(m)}}else{if(c){if(n){i.load(d);l=i.getAttribute(m)}else{f.load(ieDb);l=f.getAttribute(m)}}}return l},deleteValue:function(l,m){if(e){this.setValue(l,null,m)}else{if(c){if(m){i.removeAttribute(l);i.save(d)}else{f.removeAttribute(l);f.save(ieDb)}}}},clearDB:function(l){if(e){if(l){sessionStorage.clear()}else{localStorage.clear()}}else{if(c){h.clearDB(l)}}}};var h={detectIE:function(){if(/MSIE (\d+\.\d+);/.test(navigator.userAgent)){var l=new Number(RegExp.$1);if(l>=5.5&&l<=8){return true}}return false},init:function(){var n=document.createElement("meta");n.name="save";n.content="userdata";document.getElementsByTagName("head").item(0).appendChild(n);var m=new Date().getTime();var l=document.createElement("div");b="ie-db-"+m;ieDb="userStorage";l.setAttribute("id",b);body.appendChild(l);f=document.getElementById(b);f.style.behavior="url('#default#userData')";f.style.display="none";if(window.name===null||window.name===undefined||window.name===""){window.name="ie-sesh-db-"+m}j=window.name;d=j;l=document.createElement("div");l.setAttribute("id",j);f.appendChild(l);i=document.getElementById(j);i.style.behavior="url('#default#userData')";i.style.display="none"},clearDB:function(r){var m=new Date().getTime(),t=document.createElement("div"),l=r?i:f,p=r?d:ieDb,s=l.xmlDocument,n=s.firstChild.attributes,q,o=n.length;while(0<=--o){q=n[o];l.removeAttribute(q.nodeName)}l.save(p)}};return{init:function(){if(typeof(window.localStorage)==="object"){e=true;try{if(typeof(window.sessionStorage)==="object"){a=true}}catch(l){a=false}}else{if(h.detectIE()){c=true;h.init()}}},setValue:function(l,m){g.setValue(l,m,false)},setSessionValue:function(l,m){g.setValue(l,m,true)},getValue:function(l){return g.getValue(l,false)},getSessionValue:function(l){return g.getValue(l,true)},deleteValue:function(l){g.deleteValue(l,false)},deleteSessionValue:function(l){g.deleteValue(l,true)},clearLocalStorage:function(){g.clearDB(false)},clearSessionStorage:function(){g.clearDB(true)},clearDOMStorage:function(){g.clearDB(false);g.clearDB(true)}}})();

var API = {};

(function() {
  var Util = {
    getConfiguration: function() {
      var findThisScript = function() {
        var scripts = document.getElementsByTagName('SCRIPT');

        for (var i = 0; i < scripts.length; i++) {
          var script = scripts[i];
          var src = script.getAttribute('src');

          if (src && src.indexOf('default.js') != -1) {
            return script;
          }
        }

        return undefined;
      };

      return Util.parseQueryParameters(findThisScript().getAttribute('src'));
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
            API.Log.debug('Success: ' + msg + ': ' + JSON.stringify(result));
          }
          else {
            API.Log.debug('Success: ' + msg);
          }
        }
      }

      var failureFn = function(fn, msg) {
        if (fn) return fn;
        else return function(code, reason) {
          API.Log.error('Failure: ' + msg + ': code = ' + code + ', reason = ' + reason);
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

    normalizeTime: function(o, name) {
      if (name === undefined) {
        if (o instanceof Date) {
           return o.getUTCMilliseconds();
        }

        return o;
      }
      else {
        var time = o[name];

        if (time != null) {
          if (time instanceof Date) {
            o[name] = time.getUTCMilliseconds();
          }
          else if (time instanceof String) {
            o[name] = 0 + time
          }
        }

        return o[name];
      }
    },

    rangeHeaderFromStartEnd: function(options) {
      var headers = {};

      if (options.start !== undefined || options.end !== undefined) {
        var start = Util.normalizeTime(options.start) || ReportGrid.Time.Zero;
        var end   = Util.normalizeTime(options.end)   || ReportGrid.Time.Inf;

        headers.Range = 'time=' + start + '-' + end;
      }

      return headers;
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

      API.Log.info('HTTP ' + method + ' ' + path + ': headers(' + JSON.stringify(headers) + '), content('+ JSON.stringify(content) + ')');

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

      API.Log.info('HTTP ' + method + ' ' + path + ': headers(' + JSON.stringify(headers) + '), content('+ JSON.stringify(content) + ')');

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

        delete window[funcName];
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

  API.Config = Util.getConfiguration();

  var onceMap = {};

  API.alertSafe = function(msg) {
    if (onceMap[msg] === undefined) {
      onceMap[msg] = true;

      alert(msg);
    }
  }

  API.Extend = function(object, extensions) {
    for (var name in extensions) {
      if (object[name] === undefined) {
        object[name] = extensions[name];
      }
    }
  }

  API.Bool = function(v) {
    return v === true || v === 1 || (v = (""+v).toLowerCase()) == "true" || v == "on" || v == "1";
  }

  var console = Util.getConsole(API.Bool(API.Config.enableLog));

  API.Log = {
    log:    function(text) { console.log(text);   },
    debug:  function(text) { console.debug(text); },
    info:   function(text) { console.info(text);  },
    warn:   function(text) { console.warn(text);  },
    error:  function(text) { console.error(text); }
  }

  API.Extend(API.Config,
    {
      useJsonp : "true",
      enableLog : "true"
    }
  );

  API.Http = {};

  API.Http.Ajax  = Network.createHttpInterface(Network.doAjaxRequest);
  API.Http.Jsonp = Network.createHttpInterface(Network.doJsonpRequest);

  API.Extend(API.Http, API.Bool(API.Config.useJsonp) ? API.Http.Jsonp : API.Http.Ajax);
})();

$(function() {
  var RootAccountsAPI = 'https://api.reportgrid.com/services/billing/v1/accounts/';

  var setupHome = function() {
    var animateContentPane = function(panelIndex) {
      return function() {
        $('#middle1contentarea').animate({
          marginTop: -(panelIndex * 402)
        }, 'fast');

        return false;
      }
    }

    var setupPane = function(paneSelector, paneIndex) {
      $(paneSelector).mouseover(animateContentPane(paneIndex)).click(animateContentPane(paneIndex));
    }

    setupPane('#whatbutton', 1);
    setupPane('#howbutton',  2);
    setupPane('#whybutton',  3);
  }

  var setupLogin = function() {
    $('#loginoverlay').click(function() {
      $(this).css({opacity: 0.75}).animate({opacity: 0}, function() { $(this).hide(); });

      $('#loginmenu').hide();
    });

    $('#loginbutton').click(function() {
      var maxWidth  = $(document).width();
      var maxHeight = $(document).height();

      $('#loginoverlay').clearQueue().show().css({opacity: 0}).animate({opacity: 0.75})

      $('#loginmenu').clearQueue().show();
    })

    $('#loginpopupbutton').click(function(e) {
      e.preventDefault();

      var email    = $('#loginform input[name="email"]');
      var password = $('#loginform input[name="password"]');

      API.Http.post(RootAccountsAPI + "get", {
        email:      email.val(),
        password:   password.val()
      }, {
        success: function(response) {
          var content = $('#middlepanel');

          var tokenId = response.id.token;

          USTORE.setSessionValue('email',    email.val());
          USTORE.setSessionValue('password', password.val());
          USTORE.setSessionValue('tokenId',  tokenId);

          window.location = "./control-panel.html";
        },

        failure: function(code, text) {
          alert(text);
        }
      });

      return false;
    });
  }



  var setupArrows = function() {
    var left  = $('.leftarrow');
    var right = $('.rightarrow');

    left.click(function() {
      var c = $(this).parent();
      var ul = c.children('ul');

      var last = ul.children().last();
      ul.prepend(last);

      ul.css('margin-left', ul.margin().left - last.outerWidth());

      var cOffset = c.offset();

      var li = ul.children('li').filter(function(idx) {
        var curOffset = ul.children().eq(idx).offset();

        var delta = curOffset.left - cOffset.left;

        return delta < 0;
      }).last();

      if (li.size() > 0) {
        ul.animate({
          marginLeft: ul.margin().left + li.outerWidth()
        });
      }

      return false;
    });

    right.click(function() {
      var c = $(this).parent();
      var ul = c.children('ul');

      var cOffset = c.offset();

      var li = ul.children('li').filter(function(idx) {
        var curOffset = ul.children().eq(idx).offset();

        var delta = curOffset.left - cOffset.left;

        return delta >= 0;
      });

      if (li.size() > 0) {
        ul.animate({
          marginLeft: ul.margin().left - li.outerWidth()
        }, function() {
          var first = ul.children().first();

          if (first.size() > 0) {
            ul.append(first);

            ul.css('margin-left', ul.margin().left + first.outerWidth());
          }
        });
      }

      return false;
    })
  }

  var setupQuoteSelectors = function() {
    var p = $('#quote');
    var c = p.find('ul');
    var quotes = c.children();
    var selectors = p.find('.quoteselector');

    selectors.each(function(idx, e) {
      $(e).click(function() {
        var curMarginLeft = c.margin().left;

        c.animate({
          marginLeft: -p.outerWidth() * idx
        });

        quotes.removeClass('active');
        quotes.eq(idx).addClass('active');

        return false;
      });
    });
  }

  var setupNewsFeed = function() {
    $.getJSON("http://search.twitter.com/search.json?callback=?", {
      q: "from:ReportGrid"
    },
    function(results) {
      $('#news li').remove();

      var tweets = results.results;

      for (var i = 0; i < tweets.length; i++) {
        var tweet = tweets[i];

        var url = 'http://twitter.com/#!/' + tweet.from_user + '/status/' + tweet.id_str;

        $('#news ul').append('<li><a href="' + url + '">' + tweet.text + '</a></li>');
      }
    });
  }

  var setupAccountCreation = function() {
    try {
      $("#signupForm").validate({
        rules: {
          firstName: "required",
          lastName: "required",
          email: {
            required: true,
            email: true
          },
          password: {
            required: true,
            minlength: 5
          },
          confirmPassword: {
            required: true,
            minlength: 5,
            equalTo: "#signupForm input[name='password']"
          },
          company: {
            required: true,
            minlength: 2
          },
          title: {
            required: true,
            minlength: 3
          },
          street: {
            required: true,
            minlength: 5
          },
          city: {
            required: true,
            minlength: 2
          },
          state: {
            required: true,
            minlength: 2
          },
          postalCode: {
            required: true,
            digits: true
          },
          phone: {
            required: true,
            phoneUS: true
          },
          website: {
            required: true,
            url: true
          } //, agree: "required"
        },
        messages: {
          firstName:    "Please enter your firstname",
          lastName:     "Please enter your lastname",
          email:        "Please enter a valid email address",
          password: {
            required:   "Please provide a password",
            minlength:  "Your password must be at least 5 characters long"
          },
          confirmPassword: {
            required:   "Please provide a password",
            minlength:  "Your password must be at least 5 characters long",
            equalTo:    "Please enter the same password"
          },
          company:      "Please enter your company",
          title:        "Please enter your title at the company you work for",
          street:       "Please enter your street",
          state:        "Please enter your state or province",
          city:         "Please enter your city",
          postalCode:   "Please enter your postal code",
          phone:        "Please enter a valid US phone number",
          state:        "Please enter your state",
          website:      "Please enter your website",
          email:        "Please enter a valid email address",
          agree:        "Please accept our policy"
        }
      });
    } catch(err) {}

    var planId          = function() { return $('#signupForm input[name="planId"]:checked'); }
    var discountCode    = function() { return $('#signupForm input[name="discountCode"]'); }
    var email           = function() { return $('#signupForm input[name="email"]'); }
    var password        = function() { return $('#signupForm input[name="password"]'); }
    var firstName       = function() { return $('#signupForm input[name="firstName"]'); }
    var company         = function() { return $('#signupForm input[name="company"]'); }
    var street          = function() { return $('#signupForm input[name="street"]'); }
    var state           = function() { return $('#signupForm input[name="state"]'); }
    var phone           = function() { return $('#signupForm input[name="phone"]'); }
    var password        = function() { return $('#signupForm input[name="password"]'); }
    var confirmPassword = function() { return $('#signupForm input[name="confirmPassword"]'); }
    var lastName        = function() { return $('#signupForm input[name="lastName"]'); }
    var title           = function() { return $('#signupForm input[name="title"]'); }
    var city            = function() { return $('#signupForm input[name="city"]'); }
    var postalCode      = function() { return $('#signupForm input[name="postalCode"]'); }
    var website         = function() { return $('#signupForm input[name="website"]'); }
    var cardHolder      = function() { return $('#signupForm input[name="cardHolder"]'); }
    var cardExpMonth    = function() { return $('#signupForm input[name="cardExpMonth"]'); }
    var cardExpYear     = function() { return $('#signupForm input[name="cardExpYear"]'); }
    var cardNumber      = function() { return $('#signupForm input[name="cardNumber"]'); }
    var cardCCV         = function() { return $('#signupForm input[name="cardCCV"]'); }

    $('#signup').click(function(e) {
      $('#signup').attr("disabled", "disabled");
      e.preventDefault();


      var request = {
        "email":    email().val(),
        "password": password().val(),
        "planId":   planId().val(),
        "planCreditOption": discountCode().val(),
        "confirmPassword": confirmPassword().val(),
        "contact": {
          "firstName":  firstName().val(),
          "lastName":   lastName().val(),
          "company":    company().val(),
          "title":      title().val(),
          "phone":      phone().val(),
          "website":    website().val(),
          "address":{
            "street":   street().val(),
            "city":     city().val(),
            "state":    state().val(),
            "postalCode": postalCode().val()
          }
        },
        "billing": {
          "cardholder": cardHolder().val(),
          "number":     cardNumber().val(),
          "expMonth":   cardExpMonth().val(),
          "expYear":    cardExpYear().val(),
          "cvv":        cardCCV().val()
        }
      }

      if (cardHolder().val() == "") delete request.billing;


      API.Http.post(RootAccountsAPI, request, {
        success: function(response) {
          $('#signup').removeAttr("disabled");
          var content = $('#middlepanel');

          content.empty().append('<h1>Welcome to the ReportGrid family &mdash; you\'re in good hands now</h1>');
          content.append('<p>Your token id is <strong>' + response.id.token + '</strong>. You will need this token to access any API.</p>');
          content.append('<p>A welcome email has been sent to ' + response.id.email + '. If you have any questions, please visit the <a href="support.html">support page</a> where you can learn about all the different ways we support our customers.</p>');
          content.append('<p>Have fun, and good luck!</p>');
        },

        failure: function(code, text) {
          $('#signup').removeAttr("disabled");
          alert(text);
        }
      });

      return false;
    });
  }
  
  
  var setupSyntaxHighlighting = function() {
    $("pre.literal-block").snippet("javascript",{style:"random",transparent:true,showNum:false});
  }

  setupHome();
  setupLogin();
  setupArrows();
  setupQuoteSelectors();
  setupNewsFeed();
  setupAccountCreation();
  setupSyntaxHighlighting();
});
