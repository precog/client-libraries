package com.reportgrid.api;

import com.reportgrid.api.json.gson.GsonToJson;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import junit.framework.Assert.*;
import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.reportgrid.api.json.FromJson;
import com.reportgrid.api.json.ToJson;
import com.reportgrid.api.json.gson.GsonFromJson;
import com.reportgrid.api.json.gson.RawJson;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Unit test for simple tracking client.
 */
public class ReportGridClientTest extends TestCase {

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
			@Override public URL serviceUrl() {
				try {
					return new URL("http", "api.reportgrid.com", 80, "/services/analytics/v1/");
				} catch (MalformedURLException ex) {
					Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Invalid client URL", ex);
				}

				return null;
			}
		};

    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public ReportGridClientTest( String testName ) {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite() {
        return new TestSuite( ReportGridClientTest.class );
    }

    public void testTracking() throws IOException {
			ToJson<Object> toJson = new GsonToJson();
			TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);

      RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
			Event<TestData> testEvent = new Event<TestData>(new Date(), "test", new TestData(42, "Hello World", testJson), 1);
			testClient.track(new Path("/test"), testEvent, false, toJson);
    }

    public void testRawJson() throws IOException {
			ToJson<Object> toJson = new GsonToJson();

      String testString = "{\"test\":[{\"v\":1},{\"v\":2}]}";
      RawJson testJson = new RawJson(testString);
      TestData testData = new TestData(42, "Hello World", testJson);

			Event<TestData> testEvent = new Event<TestData>(new Date(), "test", testData, 1);

      String expected = new StringBuilder("{")
          .append("\"").append(testEvent.getEventName()).append("\":{")
            .append("\"testInt\":").append(42).append(",")
            .append("\"testStr\":\"Hello World\",")
            .append("\"~raw\":").append(testString).append(",")
            .append("\"#timestamp\":").append(testEvent.getTimestamp().getTime())
          .append("}")
        .append("}")
        .toString();

			System.out.println(testEvent.buildRequestBody(toJson));

      assertEquals(expected, testEvent.buildRequestBody(toJson));
    }

		public void testListChildPaths() throws IOException {
			FromJson<List<String>> fromJson = GsonFromJson.of(new TypeToken<List<String>>(){});

			QueryClient client = new QueryClient(QueryClient.TEST_TOKEN);

			List<Path> paths = client.listChildPaths(new Path("/test"), fromJson);
			assertFalse(paths.isEmpty());	
		}

		public void testListChildProperties() throws IOException {
			FromJson<List<String>> fromJson = GsonFromJson.of(new TypeToken<List<String>>(){});

			QueryClient client = new QueryClient(QueryClient.TEST_TOKEN);

			List<Property> properties = client.listChildProperties(new Path("/test"), new Property("test"), fromJson);
			System.out.println("properties: " + properties);
			assertFalse(properties.isEmpty());	
		}
}
