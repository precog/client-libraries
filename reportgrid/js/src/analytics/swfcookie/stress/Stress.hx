import haxe.Timer;
class Stress 
{
	static var cookie : SwfCookie;
	public static function main() 
	{
		cookie = new SwfCookie({ onready : run, batch_requests : true });
	}
	
	static function run()
	{
		var counter = cookie.get("key9");
		var setter = new Timer(10);
		setter.run = function()
		{
			for (i in 0...10)
				cookie.set("key" + i, ++counter);
		};
		
		var getter = new Timer(3000);
		getter.run = function()
		{
			trace(cookie.getAll());
		};
	}
}