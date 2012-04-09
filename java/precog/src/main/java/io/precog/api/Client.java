package io.precog.api;

import io.precog.json.ToJson;
import java.io.DataOutputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.net.URLEncoder.encode;

/**
 * A simple client for storeing arbitrary records in the Precog database.
 * @author knuttycombe
 */
public class Client {
  private final Service service;
  private final String tokenId;

  /** The default test token. */
  public static final String TEST_TOKEN = "2D36035A-62F6-465E-A64A-0E37BCC5257E";
  public static final Path TEST_ROOT = new Path("/unit_test/beta");

	/**
   * A convenience constructor that uses the default production API.
   * Note: during the Precog beta period, you must use the two-argment constructor
   * and provide the specific Service instance for the storage server URL provided
   * with your integration instructions.
   *
	 * @param tokenId The string token that permits storage of records at or below the
   * virtual filesystem path to be used 
	 */
  public Client(String tokenId) {
    this.service = Service.ProductionHttp;
    this.tokenId = tokenId;
  }

  /**
   *
   */
  public Client(Service service, String tokenId) {
    this.service = service;
    this.tokenId = tokenId;
  }

  /**
   * Get the token ID used by this client to store data.
   *
   * @return the value of tokenId
   */
  public String getTokenId() {
    return tokenId;
  } 

	/**
	 * Store the specified record.
	 * 
	 * @param <T> The type of the record object. This type must be serializable to JSON using a ToJson instance
	 * for some supertype of the specified type.
	 * @param path The path at which the record should be placed in the virtual file system.
	 * @param record The record being storeed.
	 * @param serializer The function used to serialize the record to a JSON string.
	 * @throws IOException 
	 */
  public <T> void store(Path path, Record<T> record, ToJson<? super T> serializer) throws IOException {
    store(path, record.toJson(serializer));
  }

  /**
   * Store a raw JSON string at the sep. 
   */
  public void store(Path path, String recordJson) throws IOException {
    byte[] recordBytes = recordJson.getBytes("UTF-8"); 
    String servicePath = "vfs/" + path.relativize() + "?tokenId="+encode(tokenId, "UTF-8");

    URL serviceURL = new URL(service.serviceUrl(), servicePath);
    HttpURLConnection conn = (HttpURLConnection) serviceURL.openConnection();
    conn.setDoOutput(true);
    conn.setRequestMethod("POST");
    conn.setRequestProperty("Content-Type", "application/json");
    conn.setRequestProperty("Content-Length", "" + recordBytes.length);

    DataOutputStream out = new DataOutputStream(conn.getOutputStream());
    try {
      out.write(recordBytes, 0, recordBytes.length);
    } finally {
      out.flush();
      out.close();
    }

    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
      throw new IOException(
              "Unexpected response from server: " + conn.getResponseCode() + ": " + conn.getResponseMessage() + 
              "; service url " + serviceURL +
              "; record body " + recordJson); 
    }
  }
}
