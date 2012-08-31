/**
 * ...
 * @author Franco Ponticelli
 */

extern class SwfCookie 
{
	/**
	* SwfCookie constructor - creates a new SwfCookie object and embeds the .swf into the web page.
	*
	* usage: 
	* var mySwfCookie = new SwfCookie(config);
	*
	* @param config {
	* swf_url : String [swfcookie.swf] - Url to swfcookie.swf. Must be an absolute url (with http:// and all) to work cross-domain
	* onready : Void -> Void - Callback function that is fired when the SwfCookie is loaded. Recommended.
	* onerror : Void -> Void - Callback function that is fired if the SwfCookie fails to load. Recommended.
	* namespace : String ["swfcookie"] The namespace to use in both JS and the SWF. Allows a page to have more than one instance of SwfCookie.
	* timeout : Int [10] The number of seconds to wait before assuming the user does not have flash.
	* debug : Bool [false] Is debug mode enabled? If so, mesages will be logged to the console and the .swf will be rendered on the page (although it will be an empty white box unless it cannot communicate with JS. Then it will log errors to the .swf)
	* batch_requests : Bool [true]
	* batch_delay : Int [500] time in ms
	* }
	*/
	public function new(?config : { } ) : Void;
	public var version : String;
	public function set(key : String, value : Dynamic) : Void;
	public function get(key : String) : Dynamic;
	public function getAll() : Dynamic;
	public function remove(key : String) : Dynamic;
	public function clear() : Dynamic;
	public function setObject(ob : Dynamic) : Void;
#if !noEmbedJS
	private static function __init__() : Void untyped {
		haxe.macro.Tools.includeFile("js/swfcookie.js");
	}
#end
}