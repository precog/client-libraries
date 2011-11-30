package com.reportgrid.api;

import java.io.DataOutputStream;
import com.reportgrid.api.json.ToJson;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.ArrayList;
import static java.net.URLEncoder.encode;

/**
 * A simple client for tracking arbitrary events in ReportGrid.
 * @author knuttycombe
 */
public class TrackingClient {
  private final Service service;
  private final String tokenId;

  /** The default test token. */
  public static final String TEST_TOKEN = "A3BC1539-E8A9-4207-BB41-3036EC2C6E6D";

	/**
	 * @param tokenId
	 */
  public TrackingClient(String tokenId) {
    this.service = Service.ProductionHttp;
    this.tokenId = tokenId;
  }

  public TrackingClient(Service service, String tokenId) {
    this.service = service;
    this.tokenId = tokenId;
  }

  /**
   * Get the value of tokenId
   *
   * @return the value of tokenId
   */
  public String getTokenId() {
    return tokenId;
  } 

	/**
	 * Track the specified event.
	 * 
	 * @param <T> The type of the domain object that specifies the properties
	 * for events. This type must be serializable to JSON using a ToJson instance
	 * for some supertype of the specified type.
	 * @param path The path at which the event should be placed in the virtual file system.
	 * @param event The event being tracked.
	 * @param serializer The function used to serialize the event to a JSON string.
	 * @throws IOException 
	 */
  public <T> void track(Path path, Event<T> event, int rollup, ToJson<? super T> serializer) throws IOException {
    track(path, event.buildRequestBody(serializer), String.valueOf(rollup));
  }

  public <T> void track(Path path, Event<T> event, boolean rollup, ToJson<? super T> serializer) throws IOException {
    track(path, event.buildRequestBody(serializer), String.valueOf(rollup));
  }

  public void track(Path path, String eventBody, int rollup) throws IOException {
    track(path, eventBody, String.valueOf(rollup));
  }

  public void track(Path path, String eventBody, boolean rollup) throws IOException {
    track(path, eventBody, String.valueOf(rollup));
  }

  /**
   * Call the tracking API with a raw JSON string. 
   */
  private void track(Path path, String eventBody, String rollup) throws IOException {
    String servicePath = "vfs/" + path.relativize() + "?tokenId="+encode(tokenId, "UTF-8");
    if (rollup != "0") servicePath = servicePath + "&rollup="+rollup;

    URL trackingUrl = new URL(service.serviceUrl(), servicePath);
    HttpURLConnection conn = (HttpURLConnection) trackingUrl.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Content-Length", "" + eventBody.length());

    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
    try {
      byte[] eventBytes = eventBody.getBytes("UTF-8"); 
      out.write(eventBytes, 0, eventBytes.length);
    } finally {
      out.flush();
      out.close();
    }

    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new IOException(
              "Unexpected response from server: " + conn.getResponseCode() + ": " + conn.getResponseMessage() + 
              "; tracking url " + trackingUrl +
              "; event body " + eventBody); 
    }
  }
}
