using System;
using System.Collections.Generic;
using System.Web;
using System.Net;
using System.IO;

namespace Precog.Client
{
	public enum Method
	{
	     GET, POST, DELETE, PUT
	};

	public class Rest
	{
		Uri uri;
    	string apiKey;

		/// <summary>
		/// Initializes a new instance of the <see cref="Precog.Client.Rest"/> class.
		/// </summary>
		/// <param name='uri'>
		/// URI.
		/// </param>
		/// <param name='apiKey'>
		/// API key.
		/// </param>
		///
	    internal Rest(Uri uri, string apiKey) {
	        this.uri = uri;
	        this.apiKey = apiKey;
	    }

		/// <summary>
		/// Creates a parameter string for use in url, in the form $key=$value UTF-8 encoded
		/// </summary>
		/// <returns>
		/// Single parameter string
		/// </returns>
		/// <param name='key'>
		/// Key.
		/// </param>
		/// <param name='value'>
		/// Value.
		/// </param>
	    private string UrlParameter(string key, string value)
		{
	        return key + "=" + HttpUtility.UrlEncode(value);
	    }

		/// <summary>
		/// Encodes a string in base64.
		/// </summary>
		/// <returns>
		/// The input string encoded in base64.
		/// </returns>
		/// <param name='toEncode'>
		/// String to encode
		/// </param>
		///
		public static string EncodeTo64(string toEncode)
		{
		    byte[] toEncodeAsBytes = System.Text.Encoding.Unicode.GetBytes(toEncode);
		    string returnValue = System.Convert.ToBase64String(toEncodeAsBytes);
			return returnValue;
		}

		/*
		/// <summary>
		/// Adds base authentication to a header map
		/// </summary>
		/// <param name='headers'>
		/// Headers map
		/// </param>
		/// <param name='user'>
		/// User Id
		/// </param>
		/// <param name='password'>
		/// Password
		/// </param>
	    public static void addBaseAuth(Dictionary<string, string> headers, string user, string password)
		{
	        headers.Add("Authorization", "Basic " + EncodeTo64(user + ":" + password));
	    }
	    */

		/// <summary>
		/// Gets the full name of a content type.
		/// </summary>
		/// <returns>
		/// The content type.
		/// </returns>
		/// <param name='c'>
		/// C.
		/// </param>
		///
		private string GetContentType( ContentType c)
		{
			switch (c){
				case ContentType.XZIP:
					return ("application/x-gzip");
				case ContentType.ZIP:
			        return ("application/zip");
				case ContentType.JSON:
	        		return("application/json");
				case ContentType.CSV:
					return ("text/csv");
				default:
					throw new ArgumentException("Unrecognized content type");
			}
        }

		public CredentialCache credentials(string user, string password)
		{
         	CredentialCache wrCache = new CredentialCache();
          	wrCache.Add(uri,"Basic", new NetworkCredential(user,password));
         	return wrCache;
        }

		/// <summary>
		/// Sends a http request and parses the result
		/// </summary>
		/// <returns>
		/// Server response as string
		/// </returns>
		/// <param name='method'>
		/// Request HTTP method ( GET, POST, DELETE,...)
		/// </param>
		/// <param name='path'>
		/// Full path for the request (i.e. /$service/v$version/$action )
		/// </param>
		/// <param name='request'>
		/// Request configuration
		/// </param>
    	public string Request(Method method, string reqPath, Request request=null, CredentialCache credentials = null)
		{
			string path = reqPath.Replace ("//","/");

			if (request == null)
			{
				request = new Request();
			}

	        //add parameters
	        if (apiKey != null)
			{
	            request.Parameters.Add("apiKey", apiKey);
	        }
	        char prefix = '?';
	        foreach (var param in request.Parameters)
			{
	            path = path + prefix + UrlParameter(param.Key, param.Value);
	            prefix = '&';
	        }

			Uri fullUri= new Uri(uri,path);
			Console.WriteLine("path: "+ fullUri);
	        HttpWebRequest webRequest = (HttpWebRequest)HttpWebRequest.Create(fullUri);

	        webRequest.Method=Enum.GetName(typeof(Method),method);

	        //add headers
	        foreach (var e in request.Header)
			{
	            webRequest.Headers.Add(e.Key, e.Value);
	        }
	        webRequest.ContentType=GetContentType(request.ContentType);
			if (credentials != null)
			{
				webRequest.Credentials = credentials;
			}

			//write body (if present
	        if (request.Body.Length > 0)
			{
	            byte[] bodyBytes = System.Text.Encoding.UTF8.GetBytes(request.Body);
	            webRequest.ContentLength= bodyBytes.Length;

	            using(Stream sout = webRequest.GetRequestStream())
				{
					sout.Write(bodyBytes, 0, bodyBytes.Length);
				}
	        }

			///get the result
			string result;
			try
			{
				using(HttpWebResponse webResponse = (HttpWebResponse) webRequest.GetResponse())
				{
			        if (webResponse.StatusCode != HttpStatusCode.OK && webResponse.StatusCode != HttpStatusCode.Accepted) {
						string errorMsg = "Unexpected response from server: " + webResponse.StatusCode + ": " + webResponse.StatusDescription;
			            throw new WebException(errorMsg);
			        }

					using(Stream responseStream = webResponse.GetResponseStream())
					{
						var streamReader = new StreamReader(responseStream);
          				result = streamReader.ReadToEnd();
					}
				}
			}
			catch(WebException ex)
			{
				string errorMsg = "Exception caught executing web request; message: "+ex.Message+ ", service url " + fullUri +
			                            " ; " + (request.Body.Length > 0 ? "record body '" + request.Body + "'" : " no body");
			            throw new IOException(errorMsg);
			}

	        return result;
	    }

	}

}

