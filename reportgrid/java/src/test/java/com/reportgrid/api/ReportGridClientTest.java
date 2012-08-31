package com.reportgrid.api;

import com.reportgrid.api.json.gson.GsonToJson;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.reportgrid.api.json.FromJson;
import com.reportgrid.api.json.ToJson;
import com.reportgrid.api.json.RawStringToJson;
import com.reportgrid.api.json.gson.GsonFromJson;
import com.reportgrid.api.json.gson.RawJson;
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
 * Unit test for simple tracking client.
 */
public class ReportGridClientTest {
    private static String testId = null;
  private static Path rollupBasePath;
  private static Path rollupChildPath;

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
					return new URL("http", "devapi.reportgrid.com", 80, "/services/analytics/v1/");
				} catch (MalformedURLException ex) {
					Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Invalid client URL", ex);
				}

        return null;
      }
    };

    @BeforeClass
    public static void beforeAll() throws Exception {
      testId = "" + Double.valueOf(java.lang.Math.random() * 10000).intValue();
      rollupBasePath = new Path("/test" + testId + "/rollup");
      rollupChildPath = rollupBasePath.append(new Path("child"));
      ToJson<Object> toJson = new GsonToJson();
      TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);

      for (int i = 0; i < 30; i++) {
        RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
        TestData testData = new TestData(i, "Hello\" World", testJson);
        Event<TestData> testEvent = new Event<TestData>(new Date(), "test", testData, 1);
        testClient.track(new Path("/test" + testId), testEvent, 0, toJson);
      }

      // Rollup tracking
      testClient.track(rollupChildPath, "{ \"testvalue\" : \"here\" }", true);

      Thread.sleep(30000);
    }

    @Test
    public void testTracking() throws IOException {
      ToJson<Object> toJson = new GsonToJson();
      TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);

      RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
      TestData testData = new TestData(42, "Hello\" World", testJson);
      Event<TestData> testEvent = new Event<TestData>(new Date(), "test", testData, 1);
      testClient.track(new Path("/test" + testId), testEvent, 0, toJson);
    }
    
    @Test
    public void testRollup() throws IOException {
    TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);



    FromJson<Integer> fromJson = GsonFromJson.of(new TypeToken<Integer>(){});
    QueryClient queryClient = new QueryClient(TrackingClient.TEST_TOKEN, Local);
    Integer childCount = queryClient.countOf(new Property("testvalue"), fromJson).on(rollupChildPath).query(Local, TrackingClient.TEST_TOKEN);
    assertEquals(childCount, queryClient.countOf(new Property("testvalue"), fromJson).on(rollupBasePath).query(Local, TrackingClient.TEST_TOKEN));
    }

    @Test
    public void testTrackingStrToJson() throws IOException {
      ToJson<String> toJson = new RawStringToJson();
      TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);

      Event<String> testEvent = new Event<String>(new Date(), "test", "{\"test\":[{\"v\": 1}, {\"v\": 2}]}", 1);
      testClient.track(new Path("/test" + testId), testEvent, 0, toJson);
    }

    @Test
    public void testTrackingRawString() throws IOException {
      TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);

      String rawJson = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
      testClient.track(new Path("/test" + testId), rawJson, 0);
    }

    @Test
    public void testTrackingRawUTF8() throws IOException {
      TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);

      String rawJson = "{\"test\":[{\"ดีลลิเชียส\": 1}, {\"v\": 2}]}";
      testClient.track(new Path("/test" + testId), rawJson, 0);
    }

    @Test
    public void testRawJson() throws IOException {
      ToJson<Object> toJson = new GsonToJson();

      String testString = "{\"test\":[{\"v\":1},{\"v\":2}]}";
      RawJson testJson = new RawJson(testString);
      TestData testData = new TestData(42, "Hello\" World", testJson);

      Event<TestData> testEvent = new Event<TestData>(new Date(), "test", testData, 1);

      String expected = new StringBuilder("{")
          .append("\"").append(testEvent.getEventName()).append("\":{")
            .append("\"testInt\":").append(42).append(",")
            .append("\"testStr\":\"Hello\\\" World\",")
            .append("\"~raw\":").append(testString).append(",")
            .append("\"#timestamp\":").append(testEvent.getTimestamp().getTime())
          .append("}")
        .append("}")
        .toString();

      assertEquals(expected, testEvent.buildRequestBody(toJson));
    }

    @Test
    public void testOddCharacters() throws IOException {
      ToJson<Object> toJson = new GsonToJson();
      TestData testData = new TestData(1, "™", new RawJson(""));

      Event<TestData> testEvent = new Event<TestData>(new Date(), "test", testData, 1);

      String expected = new StringBuilder("{")
          .append("\"").append(testEvent.getEventName()).append("\":{")
            .append("\"testInt\":").append(1).append(",")
            .append("\"testStr\":\"™\",")
            .append("\"#timestamp\":").append(testEvent.getTimestamp().getTime())
          .append("}")
        .append("}")
        .toString();

      String result = testEvent.buildRequestBody(toJson);

      assertEquals(expected, result);
    }

    @Test
    public void testListChildPaths() throws IOException {
      FromJson<List<String>> fromJson = GsonFromJson.of(new TypeToken<List<String>>(){});

      QueryClient client = new QueryClient(QueryClient.TEST_TOKEN, Local);

      List<Path> paths = client.listChildPaths(new Path("/test" + testId), fromJson);
      assertFalse(paths.isEmpty()); 
    }

    @Test
    public void testListChildProperties() throws IOException {
      FromJson<List<String>> fromJson = GsonFromJson.of(new TypeToken<List<String>>(){});

      QueryClient client = new QueryClient(QueryClient.TEST_TOKEN, Local);

      List<Property> properties = client.listChildProperties(new Path("/test" + testId), new Property("test"), fromJson);
      assertFalse(properties.isEmpty());  
    }
}
