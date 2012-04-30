$estr = function() { return js.Boot.__string_rec(this,''); }
if(typeof js=='undefined') js = {}
js.Boot = function() { }
js.Boot.__name__ = ["js","Boot"];
js.Boot.__unhtml = function(s) {
	return s.split("&").join("&amp;").split("<").join("&lt;").split(">").join("&gt;");
}
js.Boot.__trace = function(v,i) {
	var msg = i != null?i.fileName + ":" + i.lineNumber + ": ":"";
	msg += js.Boot.__unhtml(js.Boot.__string_rec(v,"")) + "<br/>";
	var d = document.getElementById("haxe:trace");
	if(d == null) alert("No haxe:trace element defined\n" + msg); else d.innerHTML += msg;
}
js.Boot.__clear_trace = function() {
	var d = document.getElementById("haxe:trace");
	if(d != null) d.innerHTML = "";
}
js.Boot.__closure = function(o,f) {
	var m = o[f];
	if(m == null) return null;
	var f1 = function() {
		return m.apply(o,arguments);
	};
	f1.scope = o;
	f1.method = m;
	return f1;
}
js.Boot.__string_rec = function(o,s) {
	if(o == null) return "null";
	if(s.length >= 5) return "<...>";
	var t = typeof(o);
	if(t == "function" && (o.__name__ != null || o.__ename__ != null)) t = "object";
	switch(t) {
	case "object":
		if(o instanceof Array) {
			if(o.__enum__ != null) {
				if(o.length == 2) return o[0];
				var str = o[0] + "(";
				s += "\t";
				var _g1 = 2, _g = o.length;
				while(_g1 < _g) {
					var i = _g1++;
					if(i != 2) str += "," + js.Boot.__string_rec(o[i],s); else str += js.Boot.__string_rec(o[i],s);
				}
				return str + ")";
			}
			var l = o.length;
			var i;
			var str = "[";
			s += "\t";
			var _g = 0;
			while(_g < l) {
				var i1 = _g++;
				str += (i1 > 0?",":"") + js.Boot.__string_rec(o[i1],s);
			}
			str += "]";
			return str;
		}
		var tostr;
		try {
			tostr = o.toString;
		} catch( e ) {
			return "???";
		}
		if(tostr != null && tostr != Object.toString) {
			var s2 = o.toString();
			if(s2 != "[object Object]") return s2;
		}
		var k = null;
		var str = "{\n";
		s += "\t";
		var hasp = o.hasOwnProperty != null;
		for( var k in o ) { ;
		if(hasp && !o.hasOwnProperty(k)) {
			continue;
		}
		if(k == "prototype" || k == "__class__" || k == "__super__" || k == "__interfaces__") {
			continue;
		}
		if(str.length != 2) str += ", \n";
		str += s + k + " : " + js.Boot.__string_rec(o[k],s);
		}
		s = s.substring(1);
		str += "\n" + s + "}";
		return str;
	case "function":
		return "<function>";
	case "string":
		return o;
	default:
		return String(o);
	}
}
js.Boot.__interfLoop = function(cc,cl) {
	if(cc == null) return false;
	if(cc == cl) return true;
	var intf = cc.__interfaces__;
	if(intf != null) {
		var _g1 = 0, _g = intf.length;
		while(_g1 < _g) {
			var i = _g1++;
			var i1 = intf[i];
			if(i1 == cl || js.Boot.__interfLoop(i1,cl)) return true;
		}
	}
	return js.Boot.__interfLoop(cc.__super__,cl);
}
js.Boot.__instanceof = function(o,cl) {
	try {
		if(o instanceof cl) {
			if(cl == Array) return o.__enum__ == null;
			return true;
		}
		if(js.Boot.__interfLoop(o.__class__,cl)) return true;
	} catch( e ) {
		if(cl == null) return false;
	}
	switch(cl) {
	case Int:
		return Math.ceil(o%2147483648.0) === o;
	case Float:
		return typeof(o) == "number";
	case Bool:
		return o === true || o === false;
	case String:
		return typeof(o) == "string";
	case Dynamic:
		return true;
	default:
		if(o == null) return false;
		return o.__enum__ == cl || cl == Class && o.__name__ != null || cl == Enum && o.__ename__ != null;
	}
}
js.Boot.__init = function() {
	js.Lib.isIE = typeof document!='undefined' && document.all != null && typeof window!='undefined' && window.opera == null;
	js.Lib.isOpera = typeof window!='undefined' && window.opera != null;
	Array.prototype.copy = Array.prototype.slice;
	Array.prototype.insert = function(i,x) {
		this.splice(i,0,x);
	};
	Array.prototype.remove = Array.prototype.indexOf?function(obj) {
		var idx = this.indexOf(obj);
		if(idx == -1) return false;
		this.splice(idx,1);
		return true;
	}:function(obj) {
		var i = 0;
		var l = this.length;
		while(i < l) {
			if(this[i] == obj) {
				this.splice(i,1);
				return true;
			}
			i++;
		}
		return false;
	};
	Array.prototype.iterator = function() {
		return { cur : 0, arr : this, hasNext : function() {
			return this.cur < this.arr.length;
		}, next : function() {
			return this.arr[this.cur++];
		}};
	};
	if(String.prototype.cca == null) String.prototype.cca = String.prototype.charCodeAt;
	String.prototype.charCodeAt = function(i) {
		var x = this.cca(i);
		if(x != x) return null;
		return x;
	};
	var oldsub = String.prototype.substr;
	String.prototype.substr = function(pos,len) {
		if(pos != null && pos != 0 && len != null && len < 0) return "";
		if(len == null) len = this.length;
		if(pos < 0) {
			pos = this.length + pos;
			if(pos < 0) pos = 0;
		} else if(len < 0) len = this.length + len - pos;
		return oldsub.apply(this,[pos,len]);
	};
	$closure = js.Boot.__closure;
}
js.Boot.prototype.__class__ = js.Boot;
js.Lib = function() { }
js.Lib.__name__ = ["js","Lib"];
js.Lib.isIE = null;
js.Lib.isOpera = null;
js.Lib.document = null;
js.Lib.window = null;
js.Lib.alert = function(v) {
	alert(js.Boot.__string_rec(v,""));
}
js.Lib.eval = function(code) {
	return eval(code);
}
js.Lib.setErrorHandler = function(f) {
	js.Lib.onerror = f;
}
js.Lib.prototype.__class__ = js.Lib;
if(typeof haxe=='undefined') haxe = {}
haxe.Log = function() { }
haxe.Log.__name__ = ["haxe","Log"];
haxe.Log.trace = function(v,infos) {
	js.Boot.__trace(v,infos);
}
haxe.Log.clear = function() {
	js.Boot.__clear_trace();
}
haxe.Log.prototype.__class__ = haxe.Log;
Std = function() { }
Std.__name__ = ["Std"];
Std["is"] = function(v,t) {
	return js.Boot.__instanceof(v,t);
}
Std.string = function(s) {
	return js.Boot.__string_rec(s,"");
}
Std["int"] = function(x) {
	if(x < 0) return Math.ceil(x);
	return Math.floor(x);
}
Std.parseInt = function(x) {
	var v = parseInt(x,10);
	if(v == 0 && x.charCodeAt(1) == 120) v = parseInt(x);
	if(isNaN(v)) return null;
	return v;
}
Std.parseFloat = function(x) {
	return parseFloat(x);
}
Std.random = function(x) {
	return Math.floor(Math.random() * x);
}
Std.prototype.__class__ = Std;
haxe.Timer = function(time_ms) {
	if( time_ms === $_ ) return;
	this.id = haxe.Timer.arr.length;
	haxe.Timer.arr[this.id] = this;
	this.timerId = window.setInterval("haxe.Timer.arr[" + this.id + "].run();",time_ms);
}
haxe.Timer.__name__ = ["haxe","Timer"];
haxe.Timer.delay = function(f,time_ms) {
	var t = new haxe.Timer(time_ms);
	t.run = function() {
		t.stop();
		f();
	};
	return t;
}
haxe.Timer.measure = function(f,pos) {
	var t0 = haxe.Timer.stamp();
	var r = f();
	haxe.Log.trace(haxe.Timer.stamp() - t0 + "s",pos);
	return r;
}
haxe.Timer.stamp = function() {
	return Date.now().getTime() / 1000;
}
haxe.Timer.prototype.id = null;
haxe.Timer.prototype.timerId = null;
haxe.Timer.prototype.stop = function() {
	if(this.id == null) return;
	window.clearInterval(this.timerId);
	haxe.Timer.arr[this.id] = null;
	if(this.id > 100 && this.id == haxe.Timer.arr.length - 1) {
		var p = this.id - 1;
		while(p >= 0 && haxe.Timer.arr[p] == null) p--;
		haxe.Timer.arr = haxe.Timer.arr.slice(0,p + 1);
	}
	this.id = null;
}
haxe.Timer.prototype.run = function() {
}
haxe.Timer.prototype.__class__ = haxe.Timer;
Stress = function() { }
Stress.__name__ = ["Stress"];
Stress.cookie = null;
Stress.main = function() {
	Stress.cookie = new SwfCookie({ onready : Stress.run, batch_requests : true});
}
Stress.run = function() {
	var counter = Stress.cookie.get("key9");
	var setter = new haxe.Timer(10);
	setter.run = function() {
		var _g = 0;
		while(_g < 10) {
			var i = _g++;
			Stress.cookie.set("key" + i,++counter);
		}
	};
	var getter = new haxe.Timer(3000);
	getter.run = function() {
		haxe.Log.trace(Stress.cookie.getAll(),{ fileName : "Stress.hx", lineNumber : 23, className : "Stress", methodName : "run"});
	};
}
Stress.prototype.__class__ = Stress;
IntIter = function(min,max) {
	if( min === $_ ) return;
	this.min = min;
	this.max = max;
}
IntIter.__name__ = ["IntIter"];
IntIter.prototype.min = null;
IntIter.prototype.max = null;
IntIter.prototype.hasNext = function() {
	return this.min < this.max;
}
IntIter.prototype.next = function() {
	return this.min++;
}
IntIter.prototype.__class__ = IntIter;
$_ = {}
js.Boot.__res = {}
js.Boot.__init();
{
	js.Lib.document = document;
	js.Lib.window = window;
	onerror = function(msg,url,line) {
		var f = js.Lib.onerror;
		if( f == null )
			return false;
		return f(msg,[url+":"+line]);
	}
}
{
	var d = Date;
	d.now = function() {
		return new Date();
	};
	d.fromTime = function(t) {
		var d1 = new Date();
		d1["setTime"](t);
		return d1;
	};
	d.fromString = function(s) {
		switch(s.length) {
		case 8:
			var k = s.split(":");
			var d1 = new Date();
			d1["setTime"](0);
			d1["setUTCHours"](k[0]);
			d1["setUTCMinutes"](k[1]);
			d1["setUTCSeconds"](k[2]);
			return d1;
		case 10:
			var k = s.split("-");
			return new Date(k[0],k[1] - 1,k[2],0,0,0);
		case 19:
			var k = s.split(" ");
			var y = k[0].split("-");
			var t = k[1].split(":");
			return new Date(y[0],y[1] - 1,y[2],t[0],t[1],t[2]);
		default:
			throw "Invalid date format : " + s;
		}
	};
	d.prototype["toString"] = function() {
		var date = this;
		var m = date.getMonth() + 1;
		var d1 = date.getDate();
		var h = date.getHours();
		var mi = date.getMinutes();
		var s = date.getSeconds();
		return date.getFullYear() + "-" + (m < 10?"0" + m:"" + m) + "-" + (d1 < 10?"0" + d1:"" + d1) + " " + (h < 10?"0" + h:"" + h) + ":" + (mi < 10?"0" + mi:"" + mi) + ":" + (s < 10?"0" + s:"" + s);
	};
	d.prototype.__class__ = d;
	d.__name__ = ["Date"];
}
/**
* SwfCookie adapted from:
*
* SwfStore - a JavaScript library for cross-domain flash cookies
*
* http://github.com/nfriedly/Javascript-Flash-Cookies
*
* Copyright (c) 2010 by Nathan Friedly - Http://nfriedly.com
*/

/*jslint browser: true, devel: true*/
/*globals SwfCookie*/

(function(){

	"use strict"; // http://ejohn.org/blog/ecmascript-5-strict-mode-json-and-more/

	var counter = 0; // a counter for element id's and whatnot
	
	var alpnum = /[^a-z0-9_]/ig; //a regex to find anything thats not letters and numbers

	/**
	* SwfCookie constructor - creates a new SwfCookie object and embeds the .swf into the web page.
	*
	* usage: 
	* var mySwfCookie = new SwfCookie(config);
	*
	* @param {object} config
	* @param {string} [config.swf_url=swfcookie.swf] - Url to swfcookie.swf. Must be an absolute url (with http:// and all) to work cross-domain
	* @param {functon} [config.onready] Callback function that is fired when the SwfCookie is loaded. Recommended.
	* @param {function} [config.onerror] Callback function that is fired if the SwfCookie fails to load. Recommended.
	* @param {string} [config.namespace="swfcookie"] The namespace to use in both JS and the SWF. Allows a page to have more than one instance of SwfCookie.
	* @param {integer} [config.timeout=10] The number of seconds to wait before assuming the user does not have flash.
	* @param {boolean} [config.debug=false] Is debug mode enabled? If so, mesages will be logged to the console and the .swf will be rendered on the page (although it will be an empty white box unless it cannot communicate with JS. Then it will log errors to the .swf). Works only if the swf is compiled in debug mode.
	* @param {boolean} [config.batch_requests=true]
	* @param {integer} [config.batch_delay=1000] time in ms
	*/
	window.SwfCookie = function(config){
		// make sure we have something of a configuration
		config = config || {};
		var defaults = {
			swf_url: 'swfcookie.swf',
			namespace: 'swfcookie',
			debug: false,
			timeout: 10,
			onready: null,
			onerror: null,
			batch_requests : true,
			batch_delay : 50
		};
		var key;
		for(key in defaults){
			if(defaults.hasOwnProperty(key)){
				if(!config.hasOwnProperty(key)){
					config[key] = defaults[key];
				}
			}
		}
		config.namespace = config.namespace.replace(alpnum, '_');
		
		if(window.SwfCookie[config.namespace]){
			throw "There is already an instance of SwfCookie using the '" + config.namespace + "' namespace. Use that instance or specify an alternate namespace in the config.";
		}
		
		this.config = config;
		
		// a couple of basic timesaver functions
		function id(){
			return "SwfCookie_" + config.namespace + "_" +  (counter++);
		}
		
		function div(visible){
			var d = document.createElement('div');
			document.body.appendChild(d);
			d.id = id();
			if(!visible){
				// setting display:none causes the .swf to not render at all
				d.style.position = "absolute";
				d.style.top = "-2000px";
				d.style.left = "-2000px";
			}
			return d;
		}
	
		// get a logger ready if appropriate
		if(config.debug){
			// if we're in a browser that doesn't have a console, build one
			if(typeof console === "undefined"){
				var loggerOutput = div(true);
				window.console = {
					log: function(msg){
						var m = div(true);
						m.innerHTML = msg;
						loggerOutput.appendChild(m);
					}
				};
			}
			this.log = function(type, source, msg){
				source = (source === 'swfCookie') ? 'swf' : source;
				if(typeof(console[type]) !== "undefined"){
					console[type]('SwfCookie - ' + config.namespace + ' (' + source + '): ' + msg);
				} else {
					console.log('SwfCookie - ' + config.namespace + ": " + type + ' (' + source  + '): ' + msg);
				}
			};
		} else {
			this.log = function(){}; // if we're not in debug, then we don't need to log anything
		}
	
		this.log('info','js','Initializing...');
	
		// the callback functions that javascript provides to flash must be globally accessible
		SwfCookie[config.namespace] = this;
	
		var swfContainer = div(config.debug);
		
		var swfName = id();
		
		var flashvars = "logfn=SwfCookie." + config.namespace + ".log&amp;" + 
			"onload=SwfCookie." + config.namespace + ".onload&amp;" +  // "onload" sets this.ready and then calls the "onready" config option
			"onerror=SwfCookie." + config.namespace + ".onerror&amp;" + 
			"LSOName=" + config.namespace;
			
		swfContainer.innerHTML = '<object height="100" width="500" codebase="http://download.macromedia.com/pub/shockwave/cabs/flash/swflash.cab" id="' + 
			swfName + '" classid="clsid:D27CDB6E-AE6D-11cf-96B8-444553540000">' +
			'	<param value="' + config.swf_url + '" name="movie">' + 
			'	<param value="' + flashvars + '" name="FlashVars">' +
			'	<param value="always" name="allowScriptAccess">' +
			'	<embed height="375" align="middle" width="500" pluginspage="http://www.macromedia.com/go/getflashplayer" ' +
			'flashvars="' + flashvars + '" type="application/x-shockwave-flash" allowscriptaccess="always" quality="high" loop="false" play="true" ' +
			'name="' + swfName + '" bgcolor="#ffffff" src="' + config.swf_url + '">' +
			'</object>';
		
		var swf = this.swf = document[swfName] || window[swfName];
		
		var cache = {}, collecting = false;
		
		function batchDelayedFlush()
		{
			if(collecting)
				return;
			collecting = true;
			setTimeout(function() {
				swf.setObject(cache);
				collecting = false;
				cache = {};
			}, config.batch_delay);
		}
		
		this.batchSet = function(key, value)
		{
			cache[key] = value;
			batchDelayedFlush();
		}
		
		this.batchSetObject = function(ob)
		{
			var key;
			for(key in ob)
				if(ob.hasOwnProperty(key))
					cache[key] = ob[key];
			batchDelayedFlush();
		}
		
		this.batchGet = function(key)
		{
			if(cache.hasOwnProperty(key))
				return cache[key];
			else
				return this.swf.get(key);
		}
		
		this.batchGetAll = function(data)
		{
			var key;
			for(key in cache)
				if(cache.hasOwnProperty(key))
					data[key] = cache[key];
			return data;
		}
		
		this.batchRemove = function(key)
		{
			delete cache[key];
		}
		
		this.batchClear = function()
		{
			cache = {};
		}

		this._timeout = setTimeout(function(){
			SwfCookie[config.namespace].log('error','js','Timeout reached, assuming ' + config.swf_url + ' failed to load and firing the onerror callback.');
			if(config.onerror){
				config.onerror();
			}
		}, config.timeout * 1000);
	};
	
	// we need to check everything we send to flash because it can't take functions as arguments
	function checkData(data)
	{
		if(typeof data === "function"){
			throw 'SwfCookie Error: Functions cannot be used as keys or values.';
		}
	}

	SwfCookie.prototype = {
  
		version: "1.5",
		
		/**
		* This is an indicator of wether or not the SwfCookie is initialized. 
		* Use the onready and onerror config options rather than checking this variable.
		*/
		ready: false,

		/**
		* Sets the given key to the given value in the swf
		* @param {string} key
		* @param {string} value
		*/
		set: function(key, value){
			this._checkReady();
			checkData(key);
			checkData(value);
			//this.log('debug', 'js', 'Setting ' + key + '=' + value);
			if(this.config.batch_requests)
				this.batchSet(key, value);
			else
				this.swf.set(key, value);
		},
		
		/**
		* Sets the given key/value pairs contained in the argument object in the swf
		* @param {string} ob
		*/
		setObject: function(ob){
			this._checkReady();
			checkData(ob);
			//this.log('debug', 'js', 'Setting ' + ob);
			if(this.config.batch_requests)
				this.batchSetObject(ob);
			else
				this.swf.setObject(ob);
		},
	
		/**
		* Retrieves the specified value from the swf.
		* @param {string} key
		* @return {string} value
		*/
		get: function(key){
			this._checkReady();
			checkData(key);
			//this.log('debug', 'js', 'Reading ' + key);
			if(this.config.batch_requests)
			{
				var value = this.batchGet(key);
				if(null != value)
					return value;
			}
			return this.swf.get(key);
		},

		/**
		* Retrieves all stored values from the swf. 
		* @return {object}
		*/
		getAll: function(){
			this._checkReady();
			//this.log('debug', 'js', 'Reading ' + key);
			var data = this.swf.getAll();
			// presumably the user wants to loop through their values, not including the internal __flashBugFix value
			if(data.__flashBugFix)
			{
				delete data.__flashBugFix;
			}
			if(this.config.batch_requests)
				return this.batchGetAll(data);
			else
				return data;
		},
    
	    /**
		* Delete the specified key from the swf
		*
		* @param {string} key
		*/
		remove: function(key){
			this._checkReady();
			checkData(key);
			if(this.config.batch_requests)
				this.batchRemove(key);
			this.swf.remove(key);
		},
		
		/**
		* Delete the specified key from the swf
		*
		* @param {string} key
		*/
		clear: function(){
			this._checkReady();
			if(this.config.batch_requests)
				this.batchClear();
			this.swf.clear();
		},
		
		/**
		* We need to run this check before tying to work with the swf
		*
		* @private
		*/
		_checkReady: function(){
			if(!this.ready){
				throw 'SwfCookie is not yet finished initializing. Pass a config.onready callback or wait until this.ready is true before trying to use a SwfCookie instance.';
			}
		},
		
		/**
		* This is the function that the swf calls to announce that it has loaded.
		* This function in turn fires the onready function if provided in the config.
		*
		* @private
		*/
		"onload": function(){
			// deal with scope the easy way
			var that = this;
			// wrapping everything in a timeout so that the JS can finish initializing first
			// (If the .swf is cached in IE, it fires the callback *immediately* before JS has 
			// finished executing.  setTimeout(function, 0) fixes that)
			setTimeout(function(){
			  clearTimeout(that._timeout);
			  that.ready = true;
			  
			  // There is a bug in flash player where if no values have been saved and the page is 
			  // then refreshed, the flashcookie gets deleted - even if another tab *had* saved a
			  // value to the flashcookie.
			  // So to fix, we immediately save something
			  that.set('__flashBugFix','1');
			  
			  //this.log('info', 'js', 'Ready!')
			  if(that.config.onready){
			    that.config.onready();
			  }
			}, 0);
		},
		
		
		/**
		* If the swf had an error but is still able to communicate with JavaScript, it will call this function.
		* This function is also called if the time limit is reached and flash has not yet loaded.
		* This function is most commonly called when either flash is not installed or local storage has been disabled.
		* If an onerror function was provided in the config, this function will fire it.
		*
		* @private
		*/
		onerror: function(){
			clearTimeout(this._timeout);
			//this.log('info', 'js', 'Error reported by storage.swf');
			if(this.config.onerror){
				this.config.onerror();
			}
		}
		
	};
}());;
{
	String.prototype.__class__ = String;
	String.__name__ = ["String"];
	Array.prototype.__class__ = Array;
	Array.__name__ = ["Array"];
	Int = { __name__ : ["Int"]};
	Dynamic = { __name__ : ["Dynamic"]};
	Float = Number;
	Float.__name__ = ["Float"];
	Bool = { __ename__ : ["Bool"]};
	Class = { __name__ : ["Class"]};
	Enum = { };
	Void = { __ename__ : ["Void"]};
}
{
	Math.__name__ = ["Math"];
	Math.NaN = Number["NaN"];
	Math.NEGATIVE_INFINITY = Number["NEGATIVE_INFINITY"];
	Math.POSITIVE_INFINITY = Number["POSITIVE_INFINITY"];
	Math.isFinite = function(i) {
		return isFinite(i);
	};
	Math.isNaN = function(i) {
		return isNaN(i);
	};
}
js.Lib.onerror = null;
haxe.Timer.arr = new Array();
Stress.main()