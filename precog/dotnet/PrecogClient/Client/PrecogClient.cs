using System;
using System.IO;
using System.Net;
using System.Text;
using System.Web;
using Precog.Client.Options;
using Precog.Client.Dto;

namespace Precog.Client
{
	public class PrecogClient
	{
		const string QUERY_PARAMETER_TOKEN_ID = "tokenId";
		const string QUERY_PARAMETER_QUERY = "q";
		static Uri HTTP = new Uri("http://api.precog.io");
		static Uri HTTPS = new Uri("https://api.precog.io:443");

		static int API_VERSION=1;

		private static class Paths {
        	public static string FS = "/fs";
	    }

	    private static class Services {
	        public static string ANALYTICS = "/analytics";
	        public static string ACCOUNTS = "/accounts";
	        public static string INGEST = "/ingest";
	    }


		public IJson Json { get; private set; }

		private Rest rest;

		public static PrecogClient Create(Uri service, string apiKey, IJson json)
		{
			return new PrecogClient(service, apiKey, json);
		}

		public static PrecogClient Create(string apiKey, IJson json)
		{
			return Create(HTTP, apiKey, json);
		}

		public static PrecogClient CreateSecure(string apiKey, IJson json)
		{
			return Create(HTTPS, apiKey, json);
		}

		private PrecogClient (Uri service, string apiKey, IJson json)
		{
			if(null == json)
				throw new ArgumentNullException("json");
			if(null == service)
				throw new ArgumentNullException("service");

			Json = json;
			this.rest = new Rest(service,apiKey);
		}

		/// <summary>
		/// Builds a path given a service and path, using the current api version
		/// </summary>
		/// <returns>
		/// Path of the form /$service/v$version/$path
		/// </returns>
		/// <param name='service'>
		/// service the name of the API service to access (eg. account, ingest,etc)
		/// </param>
		/// <param name='path'>
		/// the path corresponding to the action to be performed
		/// </param>
	    static public string ActionPath(string service, string path)
		{
	        return service+"/v" + API_VERSION+"/"+path;
	    }

		/// <summary>
		/// Creates a new account, accessible by the specified email address and password, or returns the existing account ID.
		/// </summary>
		/// <returns>
		/// Account info with the account Id populated
		/// </returns>
		/// <param name='email'>
		/// user's email
		/// </param>
		/// <param name='password'>
		/// user's password
		/// </param>
	    public AccountInfo CreateAccount(String email, String password)
		{
			Request r= new Request();
	        r.Body="{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }";
	        string response= rest.Request(Method.POST, ActionPath(Services.ACCOUNTS, "accounts/"),r);
			return Json.Decode<AccountInfo>(response);
	    }
	

		/// <summary>
		/// Retrieves the details about a particular account. This call is the primary mechanism by which you can retrieve your master API key.
		/// </summary>
		/// <returns>
		/// account info
		/// </returns>
		/// <param name='email'>
		/// user's email
		/// </param>
		/// <param name='password'>
		/// user's password
		/// </param>
		/// <param name='accountId'>
		/// account's id number
		/// </param>
	    public AccountInfo DescribeAccount(string email, string password, string accountId)
		{
	        string response= rest.Request(Method.GET, ActionPath(Services.ACCOUNTS, "accounts/" + accountId), credentials: rest.credentials(email,password));
			return Json.Decode<AccountInfo>(response);
	    }


		/// <summary>
		/// Store the specified record in the path
		/// </summary>
		/// <param name='path'>
		/// Storage path.
		/// </param>
		/// <param name='record'>
		/// Record.
		/// </param>
		/// <typeparam name='T'>
		/// Type of the record
		/// </typeparam>
	    public IngestResult Store<T>(string path, T record)
		{
	        return Store(path, Json.Encode(record));
	    }

		/// <summary>
		/// Store a raw JSON string at the specified path.
		/// </summary>
		/// <param name='path'>
		/// storage path.
		/// </param>
		/// <param name='recordJson'>
		/// Record as json string.
		/// </param>
	    public IngestResult Store(string path, string recordJson)
		{
	        IngestOptions options = new IngestOptions(ContentType.JSON);
	        return Ingest(path, recordJson, options);
	    }

		/// <summary>
		/// Builds the async/sync data storage path
		/// </summary>
		/// <returns>
		/// full storage path.
		/// </returns>
		/// <param name='async'>
		/// true to do an async storage call
		/// </param>
		/// <param name='path'>
		/// The path at which the record should be placed in the virtual file system.
		/// </param>
	    public string BuildStoragePath(bool async, string path)
		{
	        return (async ? "async" : "sync")+Paths.FS+"/"+path;
	    }

		/// <summary>
		/// Builds a sync data storage path
		/// </summary>
		/// <returns>
		/// The path at which the record should be placed in the virtual file system.
		/// </returns>
		/// <param name='path'>
		/// full storage path
		/// </param>
	    public string BuildSyncStoragePath(string path)
		{
	        return BuildStoragePath(false, path);
	    }

		/// <summary>
		/// Ingest data in the specified path
	    /// Ingest behavior is controlled by the ingest options
	    /// <p/>
	    /// If Async is true,  Asynchronously uploads data to the specified path and file name. The method will return almost immediately with an HTTP ACCEPTED response.
	    /// If Async is false, Synchronously uploads data to the specified path and file name. The method will not return until the data has been committed to the transaction log. Queries may or may not reflect data committed to the transaction log.
	    /// The optional owner account ID parameter can be used to disambiguate the account that owns the data, if the API key has multiple write grants to the path with different owners.
		/// </summary>
		/// <param name='path'>
		/// The path at which the record should be placed in the virtual file system.
		/// </param>
		/// <param name='content'>
		/// content to be ingested
		/// </param>
		/// <param name='options'>
		/// Ingestion options.
		/// </param>
		/// <exception cref='ArgumentNullException'>
		/// Is thrown when an argument passed to a method is invalid because it is <see langword="null" /> .
		/// </exception>
	    public IngestResult Ingest(string path, string content, IngestOptions options)
		{
	        if (content == null || content=="") {
	            throw new ArgumentNullException("argument 'content' must contain a non empty value formatted as described by type");
	        }
	        Request request = new Request();
	        request.Header= options.asMap();
	        request.Body=content;
	        request.ContentType=options.DataType;
	        string result= rest.Request(Method.POST, ActionPath(Services.INGEST, BuildStoragePath(options.Async, path)), request);
			IngestResult ingestResult;
			if (options.Async)
			{
				ingestResult= new IngestResult(false);
			} else {
				ingestResult= Json.Decode<IngestResult>(result);
			}
			return ingestResult;
	    }

		/// <summary>
		/// Deletes the specified path.
		/// </summary>
		/// <param name='path'>
		/// Path.
		/// </param>
	    public string Delete(string path)
		{
	        return rest.Request(Method.DELETE, ActionPath(Services.INGEST, BuildSyncStoragePath(path)));
	    }

		/// <summary>
		/// Executes a synchronous query relative to the specified base path. The HTTP connection will remain open for as long as the query is evaluating (potentially minutes).
	    /// Not recommended for long-running queries, because if the connection is interrupted, there will be no way to retrieve the results of the query.
		/// </summary>
		/// <param name='path'>
		/// relative storage path to query
		/// </param>
		/// <param name='q'>
		/// quirrel query to excecute
		/// </param>
		/// <typeparam name='T'>
		/// Type of the result object
		/// </typeparam>
	    public T Query<T>(string path, String q)
		{
	        path=addFS (path);
	        Request request = new Request();
	        request.Parameters.Add("q", q);
	        string response= rest.Request(Method.GET, ActionPath(Services.ANALYTICS, path), request);
			return Json.Decode<T>(response);
	    }

		/// <summary>
		/// Adds the FS prefix to the path if not present.
		/// </summary>
		/// <returns>
		/// The FS+ path
		/// </returns>
		/// <param name='path'>
		/// storage path.
		/// </param>
		private string addFS(string path)
		{
			if (!path.StartsWith(Paths.FS))
			{
	            path = Paths.FS+"/"+path;
	        }
			return path;
		}
	}
}