using System;
using NUnit.Framework;
using System.Collections.Generic;

namespace Precog.Client.Json
{
	[TestFixture()]
	public class TestJsonServiceStack
	{
		IJson Json;

		[SetUp()]
		public void Setup()
		{
			Json = new JsonServiceStack();
		}
		
		[Test()]
		public void TestInt()
		{
			var val = 1;
			var str = "1";

			AssertEncode(str, val);
			AssertDecode(val, str);
		}

		[Test()]
		public void TestString()
		{
			var val = "a";
			var str = "\"a\"";

			AssertEncode(str, val);
			AssertDecode(val, str);
		}

		[Test()]
		public void TestArray()
		{
			var val = new int[] {1,2,3};
			var str = "[1,2,3]";

			AssertEncode(str, val);
			AssertDecode(val, str);
		}

		[Test()]
		public void TestDictionary()
		{
			var val = new Dictionary<String,Int32>() {{"a", 1}};
			var str = "{\"a\":1}";

			AssertEncode(str, val);
			AssertDecode(val, str);
		}
		
		public void AssertEncode<T>(string test, T value)
		{
			Assert.AreEqual(test, Json.Encode(value), "Encoding " + test + " to " + value);	
		}

		public void AssertDecode<T>(T test, string value)
		{
			Assert.AreEqual(test, Json.Decode<T>(value), "Decoding " + value + " to " + test);
		}
	}
}

