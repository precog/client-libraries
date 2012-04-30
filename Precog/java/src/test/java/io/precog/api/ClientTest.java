package io.precog.api;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;

import io.precog.json.FromJson;
import io.precog.json.ToJson;
import io.precog.json.RawStringToJson;
import io.precog.json.gson.GsonFromJson;
import io.precog.json.gson.GsonToJson;
import io.precog.json.gson.RawJson;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.junit.Test;
import org.junit.BeforeClass;
import static org.junit.Assert.*;

/**
 * Unit test for basic client.
 */
public class ClientTest {
  private static String testId = null;
  private static Path testPath = null;

  private static class TestData {
      public final int testInt;
      public final String testStr;
      @SerializedName("~raw")
      public final RawJson testRaw;

      public TestData(int testInt, String testStr, RawJson testRaw) {
        this.testInt = testInt;
        this.testStr = testStr;
        this.testRaw = testRaw;
      }
    }

		public static final Service Local = new Service() {
			public URL serviceUrl() {
				try {
					return new URL("http", "beta2012v1.precog.io", 80, "/v1/");
				} catch (MalformedURLException ex) {
          throw new RuntimeException("Invalid client URL", ex);
				}
      }
    };

    @BeforeClass
    public static void beforeAll() throws Exception {
      testId = "" + Double.valueOf(java.lang.Math.random() * 10000).intValue();
      testPath = Client.TEST_ROOT.append(new Path("/test" + testId));

      /*
      ToJson<Object> toJson = new GsonToJson();
      Client testClient = new Client(Local, Client.TEST_TOKEN);

      for (int i = 0; i < 30; i++) {
        RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
        TestData testData = new TestData(i, "Hello\" World", testJson);
        Record<TestData> testRecord = new Record<TestData>(new Date(), "test", testData, 1);
        testClient.store(new Path("/test" + testId), testRecord, 0, toJson);
      }

      testClient.store(rollupChildPath, "{ \"testvalue\" : \"here\" }", true);

      Thread.sleep(30000);
      */
    }

    @Test
    public void testStore() throws IOException {
      ToJson<Object> toJson = new GsonToJson();
      Client testClient = new Client(Local, Client.TEST_TOKEN);

      RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
      TestData testData = new TestData(42, "Hello\" World", testJson);

      Record<TestData> testRecord = new Record<TestData>(testData);
      testClient.store(testPath, testRecord, toJson);
    }
    
    @Test
    public void testStoreStrToJson() throws IOException {
      ToJson<String> toJson = new RawStringToJson();
      Client testClient = new Client(Local, Client.TEST_TOKEN);

      Record<String> testRecord = new Record<String>("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
      testClient.store(testPath, testRecord, toJson);
    }

    @Test
    public void testStoreRawString() throws IOException {
      Client testClient = new Client(Local, Client.TEST_TOKEN);

      String rawJson = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
      testClient.store(testPath, rawJson);
    }

    @Test
    public void testStoreRawUTF8() throws IOException {
      Client testClient = new Client(Local, Client.TEST_TOKEN);

      String rawJson = "{\"test\":[{\"ดีลลิเชียส\": 1}, {\"v\": 2}]}";
      testClient.store(testPath, rawJson);
    }

    @Test
    public void testRawJson() throws IOException {
      ToJson<Object> toJson = new GsonToJson();

      String testString = "{\"test\":[{\"v\":1},{\"v\":2}]}";
      RawJson testJson = new RawJson(testString);
      TestData testData = new TestData(42, "Hello\" World", testJson);

      Record<TestData> testRecord = new Record<TestData>(testData);

      String expected = new StringBuilder("{")
          .append("\"testInt\":").append(42).append(",")
          .append("\"testStr\":\"Hello\\\" World\",")
          .append("\"~raw\":").append(testString)
        .append("}")
        .toString();

      assertEquals(expected, testRecord.toJson(toJson));
    }

    @Test
    public void testOddCharacters() throws IOException {
      ToJson<Object> toJson = new GsonToJson();
      TestData testData = new TestData(1, "™", new RawJson(""));

      Record<TestData> testRecord = new Record<TestData>(testData);

      String expected = new StringBuilder("{")
          .append("\"testInt\":").append(1).append(",")
          .append("\"testStr\":\"™\"")
        .append("}")
        .toString();

      String result = testRecord.toJson(toJson);

      assertEquals(expected, result);
    }
}
