package rg.js;

/**
 * ...
 * @author Franco Ponticelli
 */

extern class ReportGrid
{
	/** Constants */
	public static var Minute(default, null) : String;
	public static var Hour(default, null) : String;
	public static var Day(default, null) : String;
	public static var Week(default, null) : String;
	public static var Month(default, null) : String;
	public static var Year(default, null) : String;
	public static var Eternity(default, null) : String;
	/** Periodicity constants. */
	public static var Periodicity(default, null) : { Minute:String, Hour:String, Day:String, Week:String, Month:String, Year:String, Eternity:String, Periodicity:String };
	/** Time constants. */
	public static var Time(default, null) : { Zero : Int, Inf : Int };
	
	/** Tracks an event. If no timestamp is specified, the current time is used.
	*
	* The options.count and options.timestamp are optional, and default to 1 and
	* the current time, respectively.
	*
	* ReportGrid.track("/merchants/Starbucks/locations/USA_CO_Boulder/1/", {
	*   "event": {
	*     "purchase": {
	*       "item": "Americano",
	*       "size": "Grande"
	*     }
	*   }
	* });
	*/
	public static function track(path : String, ?options : { }, ?success : Void -> Void, ?failure : String -> Void) : Void;
	
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
	*                  ("path" or "property") and an optional
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
	public static function children(path : String, options : { }, ?success : Array<String> -> Void, ?failure : String -> Void) : Void;
	
	/**
	* Retrieves total counts of how often the specified property appeared in events
	* of the specified type.
	*
	* ReportGrid.propertyCount("/account/jdoe/emails/", {property: "delivery.status"});
	* > 2392
	*/
	public static function propertyCount(path : String, options : { property : String }, ?success : Int -> Void, ?failure : String -> Void) : Void;

	/**
	* Retrieves time series counts of how often the specified property appeared
	* in events of the specified type.
	*
	* ReportGrid.propertySeries("/atm-events/", {property: "transaction", periodicity: "hour"});
	* > {"hour":{"4512239238":2323}}
	*/
	public static function propertySeries(path : String, options : { }, ?success : Dynamic -> Void, ?failure : String -> Void) : Void;
	
	/**
	* Retrieves all values of the specified property throughout all time.
	*
	* ReportGrid.propertyValues("/customers/jdoe/blog-posts/1/", {property: "click.gender"});
	* > ["male", "female", "unknown"]
	*/
	public static function propertyValues(path : String, options : { property : String }, ?success : Array<String> -> Void, ?failure : String -> Void) : Void;

	/**
	* Retrieves the total number of times the property was equal to the specified
	* value.
	*
	* ReportGrid.propertyValueCount("/customers/jdoe/blog-posts/1/", {property: "click.gender", value: "male"});
	* > 12329
	*/
	public static function propertyValueCount(path : String, options : { }, ?success : Int -> Void, ?failure : String -> Void) : Void;

	/**
	* Retrieves the time series count of when the property was equal to the
	* specified value.
	*
	* ReportGrid.propertyValueSeries("/transactions/", {property: "withdrawal", periodicity: "hour"});
	* > {"hour":{"1239232323":293}}
	*/
	public static function propertyValueSeries(path : String, options : { }, ?success : Dynamic -> Void, ?failure : String -> Void) : Void;
	
	/**
	* Searches across a range of conditions to retrieve a total count.
	*
	* ReportGrid.searchCount("/advertisers/Nike", {where: {"impression.carrier": "AT&T"}});
	* > 10
	*/
	public static function searchCount(path : String, options : { }, ?success : Int -> Void, ?failure : String -> Void) : Void;

	/**
	* Searches time series for events that meet the specified constraint. Note
	* that constraints may involve at most one event.
	*
	* Options:
	*
	*  * periodicity
	*  * start
	*  * end
	*
	*
	* ReportGrid.searchSeries("/advertisers/Nike", {periodicity: "hour", where: {".impression.carrier": "AT&T"}});
	* > {"hour":{"1239232323":293}}
	*/
	public static function searchSeries(path : String, options : { }, ?success : Dynamic -> Void, ?failure : String -> Void) : Void;

	/**
	* Lists all tokens.
	*/
	public static function tokens(?success : Array<String> -> Void, ?failure : String -> Void) : Void;
	
	/**
	* Creates a new token.
	*/
	public static function newToken(newToken : String, ?success : Array<String> -> Void, ?failure : String -> Void) : Void;
	
	/**
	* Deletes the token with the specified id.
	*/
	public static function deleteToken(tokenId : String, ?success : Array<String> -> Void, ?failure : String -> Void) : Void;
	
	static function __init__() : Void
	{
		untyped __js__("var rg = rg || {}; rg.js = rg.js || {}; rg.js.ReportGrid = window.ReportGrid");
	}
}