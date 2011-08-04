import haxe.Timer;
import utest.Runner;
import utest.ui.Report;
import utest.Assert;

class TestSwfCookie 
{
	static var key = "key";
	static var value = "value";
	
	
	static var cookie : SwfCookie;
	public function teardown() cookie.clear()
	
	public function testGetSet()
	{
		Assert.isNull(cookie.get(key));
		cookie.set(key, value);
		Assert.equals(value, cookie.get(key));
	}
	
	public function testClear()
	{
		cookie.set(key, value);
		cookie.clear();
		Assert.isNull(cookie.get(key));
	}
	
	public function testRemove()
	{
		cookie.set(key, value);
		cookie.remove(key);
		Assert.isNull(cookie.get(key));
	}
	
	public function testDelayedGet()
	{
		cookie.set(key, value);
		Assert.equals(value, cookie.get(key));
		
		var async = Assert.createAsync(function() {
			Assert.equals(value, cookie.get(key));
		}, 1000);
		Timer.delay(async, 100);
	}
	
	public function testSetObject()
	{
		cookie.set("string", "A");
		cookie.setObject({
			string : "B",
			int    : 1,
			float1 : Math.NaN,
			float2 : 0.1,
			array  : [1, 2, 3],
			bool   : true
		});
		
		Assert.same({ 
			string : "B",
			int    : 1,
			float1 : Math.NaN,
			float2 : 0.1,
			array  : [1, 2, 3],
			bool   : true
		}, cookie.getAll());
	}
	
	public function testGetAll()
	{
		cookie.set("string", "A");
		cookie.set("int", 1);
		cookie.set("float1", Math.NaN);
		cookie.set("float2", 0.1);
		cookie.set("array", [1, 2, 3]);
		cookie.set("bool", true);
		
		Assert.same( { 
			string : "A",
			int    : 1,
			float1 : Math.NaN,
			float2 : 0.1,
			array  : [1, 2, 3],
			bool   : true
		}, cookie.getAll());
	}
	
	public function new() { }
	
	public static function main()
	{
#if js
		cookie = new SwfCookie({ 
			timeout : 2,
			debug : false,
			onready : run,
			onerror : function() { trace("error loading SwfCookie"); },
			batch_requests : true,
			batch_delay : 50
		});
#else
		cookie = new SwfCookie();
		run();
#end
	}
	
	static function run()
	{
		var runner = new Runner();
		runner.addCase(new TestSwfCookie());
		Report.create(runner);
		runner.run();
	}
}