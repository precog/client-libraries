package com.reportgrid.api;

import com.reportgrid.api.Queries.JsonStreamHandler;
import com.reportgrid.api.Queries.ValuesOf;
import com.reportgrid.api.json.FromJson;
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

/**
 * A client for simple
 *
 * @author knuttycombe
 */
public class QueryClient {
  private final Service service;
  private final String tokenId;

  /** The default test token. */
  public static final String TEST_TOKEN = "A3BC1539-E8A9-4207-BB41-3036EC2C6E6D";

  public QueryClient(String tokenId, Service service) {
    this.service = service;
    this.tokenId = tokenId;
  }

  public QueryClient(String tokenId) {
    this.service = Service.ProductionHttp;
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
	 * Query for and return the properties that are children of the specified
	 * path and property.
	 * @param path The path to retrieve data for, such as /videos/1
	 * @param property The property to query for, such as .engagement.duration
	 * @param fromJson The FromJson converter to use to deserialize the result. This
	 * converter should be able to transform a JSON string into a 
	 * {@link java.util.List<String>}
	 * @return
	 * @throws IOException 
	 */
	public List<Property> listChildProperties(Path path, Property property, FromJson<List<String>> fromJson) throws IOException {
		return filterProperties(
			Queries.list(path, property, new JsonStreamHandler<List<String>>(fromJson)).query(service, tokenId)
		);			
	}

	public List<Path> listChildPaths(Path path, FromJson<List<String>> fromJson) throws IOException {
		return filterPaths(
			Queries.list(path, null, new JsonStreamHandler<List<String>>(fromJson)).query(service, tokenId)
		);			
	}

	/**
	 * Returns a builder for queries that can be used to return
	 * @param property
	 * @param fromJson
	 * @return 
	 */
	public ValuesOf<List<String>> valuesOf(Property property, FromJson<List<String>> fromJson) {
		return new ValuesOf<List<String>>(property, new JsonStreamHandler<List<String>>((fromJson)));
	}

	private static List<Path> filterPaths(List<String> stringResults) {
		List<Path> results = new ArrayList<Path>();
		for (String str : stringResults) {
			//if (str.endsWith("/")) 
				results.add(new Path(str));
		}
		return results;
	}

	private static List<Property> filterProperties(List<String> stringResults) {
		List<Property> results = new ArrayList<Property>();
		for (String str : stringResults) {
			if (str.startsWith(".")) results.add(new Property(str));
		}
		return results;
	}
}
