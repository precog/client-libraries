using System;
using System.IO;
using System.Net;
using System.Text;
using System.Web;

namespace Precog.Client
{
	public class PrecogClient
	{
		const string QUERY_PARAMETER_TOKEN_ID = "tokenId";
		const string QUERY_PARAMETER_QUERY = "q";
		static Uri HTTP = new Uri("http://api.precog.io/v1/");
		static Uri HTTPS = new Uri("https://api.precog.io:443/v1/");

		public IJson Json { get; private set; }
		public string TokenId { get; private set; }
		public Uri Service { get; private set; }

		string content;

		public static PrecogClient Create(Uri service, string tokenId, IJson json)
		{
			return new PrecogClient(service, tokenId, json);
		}

		public static PrecogClient Create(string tokenId, IJson json)
		{
			return Create(HTTP, tokenId, json);
		}

		public static PrecogClient CreateSecure(string tokenId, IJson json)
		{
			return Create(HTTPS, tokenId, json);
		}

		private PrecogClient (Uri service, string tokenId, IJson json)
		{
			if(null == json)
				throw new ArgumentNullException("json");
			if(null == tokenId)
				throw new ArgumentNullException("tokenId");
			if(null == service)
				throw new ArgumentNullException("service");

			Json = json;
			TokenId = tokenId;
			Service = service;
		}
		
		public bool Store<T>(string path, T value)
		{
			var uri = new Uri(Service, "./vfs/" + path.TrimStart('/'));
			var serialized = Json.Encode(value);
			return executeStore(uri, serialized);
		}

		public string LastError { get; private set; }

		public T Query<T>(string query)
		{
			var uri = new Uri(Service, "./vfs/");
			if(executeQuery(uri, query))
			{
				return Json.Decode<T>(content);
			} else {
				throw new ApplicationException(content);
			}
		}

		bool executeQuery(Uri path, string query)
		{
			path = AddQueryParameter(path, QUERY_PARAMETER_TOKEN_ID, TokenId);
			path = AddQueryParameter(path, QUERY_PARAMETER_QUERY, query);

			HttpWebRequest request = (HttpWebRequest) WebRequest.Create(path);
			request.Method = "GET";
			request.ContentType = "application/json";

			try
			{
				using(HttpWebResponse response = (HttpWebResponse) request.GetResponse())
				{
					using(Stream responseStream = response.GetResponseStream())
					{
						var bytes = new byte[responseStream.Length];
						responseStream.Read(bytes, 0, bytes.Length);
						content = Encoding.UTF8.GetString(bytes);
					}
					switch(response.StatusCode)
					{
						case HttpStatusCode.OK:
							return true;
						default:
							return false;
					}
				}
			}
			catch(WebException ex)
			{
				LastError = ex.Message;
				return false;
			}
		}

		bool executeStore(Uri path, string value)
		{
			var data = Encoding.UTF8.GetBytes(value);

			path = AddQueryParameter(path, QUERY_PARAMETER_TOKEN_ID, TokenId);

			HttpWebRequest request = (HttpWebRequest) WebRequest.Create(path);
			request.Method = "POST";
			request.ContentType = "application/json";
			request.ContentLength = data.Length;

			using(Stream requestStream = request.GetRequestStream())
			{
				requestStream.Write(data, 0, data.Length);
			}

			try
			{
				using(HttpWebResponse response = (HttpWebResponse) request.GetResponse())
				{
					switch(response.StatusCode)
					{
						case HttpStatusCode.OK:
							return true;
						default:
							LastError = response.StatusDescription;
							return false;
					}
				}
			}
			catch(WebException ex)
			{
				LastError = ex.Message;
				return false;
			}
		}

		static Uri AddQueryParameter(Uri uri, string name, string value)
		{
			var query = uri.Query;
			if(String.IsNullOrEmpty(query))
				query = name + "=" + HttpUtility.UrlEncode(value);
			else
				query += "&" + name + "=" + HttpUtility.UrlEncode(value);
			var builder = new UriBuilder(uri);
			builder.Query = query.TrimStart('?');
			return builder.Uri;
		}
	}
}

