package com.reportgrid.api;

import com.reportgrid.api.json.gson.GsonToJson;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import com.google.gson.Gson;
import com.reportgrid.api.json.ToJson;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Unit test for simple tracking client.
 */
public class ReportGridClientTest extends TestCase {

		private static class TestData {
			private final int testInt = 42;	
			private final String testStr = "Hello World";
		}

		public static final Service Local = new Service() {
			@Override public URL serviceUrl() {
				try {
					return new URL("http", "localhost", 8888, "/");
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

    /**
     * Rigorous Test :-)
     */
    public void testClient() throws IOException {
			ToJson<Object> toJson = new GsonToJson();
			TrackingClient testClient = new TrackingClient(Local, TrackingClient.TEST_TOKEN);

			Event<TestData> testEvent = new Event<TestData>(new Date(), "test", new TestData(), 1);
			testClient.track(new Path("/test"), testEvent, toJson);
    }
}
