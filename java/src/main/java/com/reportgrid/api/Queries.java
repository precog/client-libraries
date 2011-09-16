package com.reportgrid.api;

import com.reportgrid.api.json.FromJson;
import java.util.List;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import static java.net.URLEncoder.encode;

/**
 *
 * @author knuttycombe
 */
public class Queries {
	/**
	 * An interface for functions that parse a value of a specified type from
	 * an input stream.
	 * @param <T> The type to return from parsing the stream.
	 */
	public interface StreamHandler<T> {
		public T readFrom(InputStream stream) throws IOException ;
	}


	/**
	 * A simple stream handler that reads the full stream as a string and returns it.
	 */
	public static class StringStreamHandler implements StreamHandler<String> {
		/**
		 * Execute this query against the provided service with the given token 
		 * @param service
		 * @param tokenId
		 * @return the result data as a JSON-encoded string.
		 */
		public String readFrom(InputStream stream) throws IOException {
			StringBuilder builder = new StringBuilder();
			BufferedReader in = new BufferedReader(new InputStreamReader(stream));
			String line = in.readLine();
			while(line != null) {
				builder.append(line);
				line = in.readLine();
			}
			return builder.toString();
		}
	}

	/** 
	 * A handler for streams of JSON data that can parse based upon a FromJson instance. 
	 */
	public static class JsonStreamHandler<T> implements StreamHandler<T> {
		private final FromJson<T> fromJson;
		public JsonStreamHandler(FromJson<T> fromJson) {
			this.fromJson = fromJson;
		}

		public T readFrom(InputStream stream) throws IOException {
			return fromJson.deserialize(new StringStreamHandler().readFrom(stream));	
		}
	}

	/** 
	 * The base interface for queries that depend upon a service.
	 * @param <A> 
	 */
	public interface Query<A> {
		public A query(Service service, String tokenId) throws IOException;
	}


	/**
	 * A literate builder for queries that can be used to return the values of properties
	 * at specified paths from the ReportGridService.
	 * @param <T> 
	 */
	public static class ValuesOf<T> {
		private final Property property;
		private final StreamHandler<T> reader;
		public ValuesOf(Property property, StreamHandler<T> reader) {
			this.property = property;
			this.reader = reader;
		}

		public Query<T> from(final Path path) {
			return new Query<T>() {
				public T query(Service service, String tokenId) throws IOException {
					return _query(
						service, 
						vfsUrl(service, path.append(property), tokenId),
						null,
						reader
					);
				}
			};
		}
	}

	/**
	 * A literate builder for queries that return selections of data over a range
	 * of dates.
	 * @param <T> 
	 */
	public static class Selection<T> {
		protected final DateRange dateRange;

		public Selection(DateRange dateRange) {
			this.dateRange = dateRange;
		}

		/**
		 * Get the value of dateRange
		 *
		 * @return the value of dateRange
		 */
		public DateRange getDateRange() {
			return dateRange;
		}

	}
	
	private static <T> T _query(Service service, URL url, String body, StreamHandler<T> reader) throws IOException {
		HttpURLConnection conn = (HttpURLConnection) url.openConnection();
		if (body != null) {
			conn.setRequestMethod("POST");
			conn.setRequestProperty("Content-Type", "application/json");
			conn.setRequestProperty("Content-Length", "" + body.length());
			DataOutputStream out = new DataOutputStream(conn.getOutputStream());
			try {
				out.writeBytes(body);
			} finally {
				out.flush();
				out.close();
			}
		}

    if (conn.getResponseCode() != HttpURLConnection.HTTP_OK) {
			throw new IOException("Unexpected response from server: " + conn.getResponseCode() + ": " + conn.getResponseMessage());
		} else {
			InputStream in = conn.getInputStream();
			try {
				return reader.readFrom(in);
			} finally {
				in.close();
			}
		}
	}

	private static URL vfsUrl(Service service, Path path, String tokenId) throws IOException {
		return new URL(
						service.serviceUrl(), 
						"vfs/" + path.relativize() + "?tokenId=" + encode(tokenId, "UTF-8"));
	}

	/**
	 * 
	 * @param path 
	 * @param property The property to query for, may be null.
	 * @param handler
	 * @return 
	 */
	public static Query<List<String>> list(final Path path, final Property property, final StreamHandler<List<String>> handler) {
		return new Query<List<String>>() {
			@Override
			public List<String> query(Service service, String tokenId) throws IOException {
				URL url = vfsUrl(service, path.append(property), tokenId);
				return _query(service, url, null, handler);
			}
		};
	}
}
