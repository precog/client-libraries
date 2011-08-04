/**
 * based on SwfStore by Nathan Friedly:
 *
 * http://github.com/nfriedly/Javascript-Flash-Cookies
 *
 * Copyright (c) 2010 by Nathan Friedly - Http://nfriedly.com
 */

import flash.events.Event;
import flash.Lib;
import flash.errors.Error;
import flash.errors.SecurityError;
import flash.events.NetStatusEvent;
import flash.external.ExternalInterface;
import flash.net.SharedObject;
import flash.net.SharedObjectFlushStatus;
import flash.system.Security;
 
class SwfCookie 
{
	static function main()
	{
		Lib.current.addEventListener(Event.ADDED_TO_STAGE, ready);
	}
	
	static function ready(_)
	{
		new SwfCookie();
	}
	
	private static inline var DEFAULT_LSO_NAME = "SwfCookie";
	var dataStore(getDataStore, null) : SharedObject;
	var LSOName : String;
	
	/**
	* The JS function to call for logging.
	* Should be specified as "logfn" in the flashvars
	*/
	var logFn : String;
	
	public function new()
	{
		if (!ExternalInterface.available)
		{
			localLog("External Interface is not avaliable! (No communication with JavaScript.) Exiting.");
			return;
		} 
		
#if debug
		if (haxe.Firebug.detect())
			haxe.Firebug.redirectTraces();
#end
		
		var params : Dynamic = Lib.current.loaderInfo.parameters;
		// since even logging involves communicating with javascript, 
		// the next thing to do is find the external log function
		if (params.logfn)
			logFn = params.logfn;

		log('Initializing Flash...');
		
		// this is necessary to work cross-domain
		Security.allowDomain("*");
		Security.allowInsecureDomain("*");
		
		// grab the namespace if supplied
		if (null != params.LSOName)
		{
			LSOName = params.LSOName;
		} else {
			LSOName = DEFAULT_LSO_NAME;
		}
		
		try 
		{
			// expose our external interface
			ExternalInterface.addCallback("set", set);
			ExternalInterface.addCallback("setObject", setObject);
			ExternalInterface.addCallback("get", get);
			ExternalInterface.addCallback("getAll", getAll);
			ExternalInterface.addCallback("remove", remove);
			ExternalInterface.addCallback("clear", clear);
			
			log('Ready! Firing onload if provided');
			
			// if onload was set in the flashvars, assume it's a string function name and call it.
			// (This means that the function must be in the global scope. I'm not sure how to call a scoped function.)

			if(params.onload){
				ExternalInterface.call(params.onload);
				// and we're done!
			}
		} catch (error:SecurityError) {
			log("A SecurityError occurred: " + error.message + "\n");
			onError();
		} catch (error:Error) {
			log("An Error occurred: " + error.message + "\n");
			onError();
		}
		
		var start = Date.now();
		var timer = new haxe.Timer(2000), me = this;
		timer.run = function()
		{
			if (null != me.dataStore)
			{
				me.dataStore.close();
				me.dataStore = null;
				trace("close instance, elapsed: " + Math.round((Date.now().getTime() - start.getTime()) / 100) / 10);
			}
		}
	}
	
	function getDataStore()
	{
		// try to initialize our lso
		try {
			if (null == dataStore)
				dataStore = SharedObject.getLocal(LSOName);
			return dataStore;
		} catch(error:Error){
			// user probably unchecked their "allow third party data" in their global flash settings
			log('Unable to create a local shared object. Exiting - ' + error.message);
			onError();
			return null;
		}
	}
	
#if debug
	function localLog(s : String) trace(s)
	function log(s : String)
	{
	//	trace(s);
		if(null != logFn){
			try{
				ExternalInterface.call(logFn, 'debug', 'swfCookie', s);
			} catch(error:Error){
				localLog("Error logging to js: " + error.message);
			} 
		} else {
			localLog(s);
		}
	}
#else
	function localLog(s : String) { }
	function log(s : String) { }
#end
	
	/**
	* Attempts to notify JS when there was an error during initialization
	*/
	private function onError()
	{
		try
		{
			if (ExternalInterface.available && null != Lib.current.loaderInfo.parameters.onerror)
			{
				ExternalInterface.call(Lib.current.loaderInfo.parameters.onerror);
			}
		} catch (error:Error){
			log('Error attempting to fire JS onerror callback - ' + error.message);
		}
	}
	
	function flush()
	{
		var flushStatus:String = null;
		try 
		{
			flushStatus = dataStore.flush(10000);
		} catch (error:Error) {
			log("Error...Could not write SharedObject to disk - " + error.message );
		}
		if (flushStatus != null) {
			switch (flushStatus) {
				case SharedObjectFlushStatus.PENDING:
					log("Requesting permission to save object...");
					dataStore.addEventListener(NetStatusEvent.NET_STATUS, onFlushStatus);
				case SharedObjectFlushStatus.FLUSHED:
					// don't really need another message when everything works right
					log("Value flushed to disk.");
			//		dataStore.close();
			//		dataStore = null;
					//dataStore.fps = 1;
			}
		}
	}
	
	/**
	* Saves the data to the LSO, and then flushes it to the disk
	*
	* @param {string} key
	* @param {string} value - Expects a string. Objects will be converted to strings, functions tend to cause problems.
	*/
	public function set(key:String, val : Dynamic)
	{
		try
		{
			log('Setting ' + key + '=' + Std.string(val));
			Reflect.setField(dataStore.data, key, val);
		} catch(error:Error){
			log('Unable to save data - ' + error.message);
		}
		
		flush();
	}
	
	public function setObject(ob : Dynamic)
	{
		try
		{
			log('Setting ' + Std.string(ob));
			for (key in Reflect.fields(ob))
				Reflect.setField(dataStore.data, key, Reflect.field(ob, key));
		} catch(error:Error){
			log('Unable to save object data - ' + error.message);
		}
		
		flush();
	}
	
	public function clear()
	{
		dataStore.clear();
	}

	/**
	* Reads and returns data from the LSO
	*/
	public function get(key:String):String {
		try
		{
			log('Reading ' + key);
			return Reflect.field(dataStore.data, key);
		} catch(error:Error){
			log('Unable to read data - ' + error.message);
		}
		return null;
	}
	
	/**
	* Deletes an item from the LSO
	*/
	public function remove(key:String)
	{
		try
		{
			log("Deleting " + key);
			Reflect.deleteField(dataStore.data, key);
		} catch (error:Error){
			log("Error deleting key - " + error.message);
		}
	}

	/** 
	* This retrieves all stored data
	*/
	public function getAll() : Dynamic 
	{
		return dataStore.data;
	}
	
	/**
	* This happens if the user is prompted about saving locally
	*/
	private function onFlushStatus(event:NetStatusEvent) 
	{
		log("User closed permission dialog...");
		switch (event.info.code) {
			case "SharedObject.Flush.Success":
				log("User granted permission -- value saved.");
			case "SharedObject.Flush.Failed":
				log("User denied permission -- value not saved.");
		}

		dataStore.removeEventListener(NetStatusEvent.NET_STATUS, onFlushStatus);
	}
}