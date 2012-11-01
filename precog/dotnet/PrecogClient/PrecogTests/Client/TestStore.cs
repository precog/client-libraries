using System;
using System.Collections.Generic;
using System.Text;
using NUnit.Framework;
using Precog.Client;
using Precog.Client.Dto;
using Precog.Client.Options;
using Precog.Client.Json;

namespace Precog.Client
{
	 class TestData
	{
        public int testInt {get; set; }
        public string testStr;
        public string rawJson;

        public TestData(int testInt, string testStr, string rawJson) {
            this.testInt = testInt;
            this.testStr = testStr;
            this.rawJson = rawJson;
        }
    }

	[TestFixture()]
	public class TestStore
	{
		static Uri SERVICE = new Uri("http://beta.precog.com");

		private string testId;
	    private string testPath;
		private string storePath;

	    public string testAccountId;
	    public string testApiKey;

		public IJson Json= new JsonServiceStack();

		[SetUp()]
		public void Setup()
		{
			PrecogClient noKeyClient = ServiceStack.CreatePrecogClient(SERVICE, null);

			testId = ""+ new Random().Next (0,10000);

	        AccountInfo res = noKeyClient.CreateAccount("java-test@precog.com", "password");
	        testAccountId = res.accountId;
	        res = noKeyClient.DescribeAccount("java-test@precog.com", "password", testAccountId);
	        testApiKey = res.ApiKey;

	        testPath = "/test" + testId;
			storePath = testAccountId+testPath;
		}

		[Test()]
		public void TestStoreAndQuery ()
		{
			PrecogClient client = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
			var query = String.Format("count(load(\"{0}\"))", testPath);

			int count = (int) client.Query<List<float>>(testAccountId,query)[0];

			client.Store(storePath,  "{\"test\":[{\"v\": 1}, {\"v\": 2}]}" );

			var retry = 0;
			var success = false;
			while(retry++ < RETRIES)
			{
				var newcount = (int) client.Query<List<float>>(testAccountId,query)[0];
				if(newcount > count)
				{
					success = true;
					break;
				}
				System.Threading.Thread.Sleep(DELAY);
			}
			Assert.IsTrue(success);
		}

		[Test()]
		public void testCreateAccount()
		{
	        PrecogClient noKeyClient = ServiceStack.CreatePrecogClient(SERVICE, null);
	        AccountInfo res = noKeyClient.CreateAccount("java-test@precog.com", "password");
	        string accountId = res.accountId;
	        Assert.IsNotNullOrEmpty(accountId);
	        Assert.AreEqual(testAccountId, accountId);
    	}

	    [Test()]
	    public void testDescribeAccount()
		{
	        PrecogClient noKeyClient = ServiceStack.CreatePrecogClient(SERVICE, null);
	        AccountInfo res = noKeyClient.DescribeAccount("java-test@precog.com", "password", testAccountId);
	        Assert.AreEqual(testAccountId, res.accountId);
	    }

	    [Test()]
	    public void testQuery()
		{
	        //just test the query was sent and executed successfully
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
	        string[] result = testClient.Query<string[]>(testAccountId+"/", "count(//" + testAccountId + ")");
	        Assert.IsNotNull(result);
	        Assert.AreEqual("0", result[0]);
	    }

		const int DELAY = 500;
		const int RETRIES = 40;

		[Test()]
    	public void testStore()
		{
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);

	        TestData testData = new TestData(42, "Hello\" World", "{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
	        IngestResult response =testClient.Store(storePath, testData);
			Assert.AreEqual(1, response.Ingested);
	    }

	    [Test()]
	    public void testStoreRawString()
		{
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
	        IngestResult response =testClient.Store(storePath, "{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
			Assert.AreEqual(1, response.Ingested);
	    }

	    [Test()]
	    public void testStoreRawUTF8()  {
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
	        string rawJson = "{\"test\":[{\"ดีลลิเชียส\": 1}, {\"v\": 2}]}";
	        IngestResult response =testClient.Store(storePath, rawJson);
			Assert.AreEqual(1, response.Ingested);
	    }


	    [Test()]
	    public void testIngestCSV()  {
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
	        IngestOptions options = new CSVIngestOptions();
	        IngestResult response = testClient.Ingest(storePath, "blah,\n\n", options);
	        Assert.AreEqual(1, response.Ingested);
	    }

	    [Test()]
	    public void testIngestJSON()  {
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
	        IngestOptions options = new IngestOptions(ContentType.JSON);
	        string rawJson = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
	        IngestResult response = testClient.Ingest(storePath, rawJson, options);
	        Assert.AreEqual(1, response.Ingested);
	    }

	    [Test()]
	    public void testIngestCsvWithOptions()  {
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
	        CSVIngestOptions options = new CSVIngestOptions();
	        options.delimiter=",";
	        options.quote="'";
	        options.escape="\\";
	        IngestResult response = testClient.Ingest(storePath, "blah,\n\n", options);
	        Assert.AreEqual(1, response.Ingested);
	    }

	    [Test()]
	    public void testIngestAsync()
		{
	        PrecogClient testClient = ServiceStack.CreatePrecogClient(SERVICE, testApiKey);
	        IngestOptions options = new CSVIngestOptions();
	        options.Async=true;
	        IngestResult result = testClient.Ingest(storePath, "blah,\n\n", options);
	        //is async, so we don't expect results
	        Assert.IsFalse(result.Completed);
	    }

		[Test()]
		public void testFromJson()
		{	string json = "{\"total\":1.0,\"Ingested\":1.0,\"Failed\":0.0,\"Skipped\":0.0,\"Errors\":[]}";
			IngestResult result=Json.Decode<IngestResult>(json);
			Assert.AreEqual(1,result.Total);
			Assert.AreEqual(1,result.Ingested);
			Assert.AreEqual(0,result.Failed);
		}

	}

}

