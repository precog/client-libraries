using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using Precog.Client;

namespace Precog.Client
{
	[TestFixture()]
	public class TestStore
	{
		const string TEST_TOKEN_ID = "2D36035A-62F6-465E-A64A-0E37BCC5257E";
		const string PATH = "/unit_test/beta/";
		static Uri SERVICE = new Uri("http://beta2012v1.precog.io/v1/");

		PrecogClient client;
		[SetUp()]
		public void Setup()
		{
			client = ServiceStack.CreatePrecogClient(SERVICE, TEST_TOKEN_ID);
		}
		
		[Test()]
		public void TestSimpleStore ()
		{
			var path = Path();
			Assert.IsTrue(client.Store(path, "test"));
		}
		
		[Test()]
		public void TestStoreAndQuery ()
		{
			var path = Path();
			var query = String.Format("count(load(\"{0}\"))", path);
			int count = (int) client.Query<List<float>>(query)[0];

			client.Store(path, "test");

			var retry = 0;
			var success = false;
			while(retry++ < RETRIES)
			{
				var newcount = (int) client.Query<List<float>>(query)[0];
				if(newcount > count)
				{
					success = true;
					break;
				}
				System.Threading.Thread.Sleep(DELAY);
			}
			Assert.IsTrue(success);
		}

		const int DELAY = 500;
		const int RETRIES = 40;
		
		const string BASE_PATH = "/unit_test/beta/dotnet/";
	    static string Path()
		{
			return BASE_PATH + Guid.NewGuid();
		}
	}
}

