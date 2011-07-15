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
  public <T> void track(Path path, Event<T> event, boolean rollup, ToJson<? super T> serializer) throws IOException {
    List<Path> paths = new ArrayList<Path>();
    paths.add(path);

    if (rollup) {
      path = path.getPrefix();
      while (path != null) {
        paths.add(path);
        path = path.getPrefix();
      }
    }

    for (Path p : paths) {
      URL trackingUrl = new URL(service.serviceUrl(), "vfs/" + p.relativize() + "?tokenId="+encode(tokenId, "UTF-8"));
      HttpURLConnection conn = (HttpURLConnection) trackingUrl.openConnection();
      conn.setDoOutput(true);
      conn.setRequestMethod("POST");
      conn.setRequestProperty("Content-Type", "application/json");

      String body = new StringBuilder("{")
        .append("\"timestamp\":").append(event.getTimestamp().getTime()).append(",")
        .append("\"events\":{")
          .append("\"").append(event.getEventName()).append("\":").append(serializer.serialize(event.getEventData()))
        .append("},")
        .append("\"count\":").append(event.getCount())
        .append("}")
        .toString();

      System.out.println(body);
      
      conn.setRequestProperty("Content-Length", "" + body.length());

      DataOutputStream out = new DataOutputStream(conn.getOutputStream());
      try {
        out.writeBytes(body);
      } finally {
        out.flush();
        out.close();
      }

      if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
        throw new IOException(
                "Unexpected response from server: " + conn.getResponseCode() + ": " + conn.getResponseMessage() + 
                "; tracking url " + trackingUrl); 
      }
    }
  }
}
