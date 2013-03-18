package com.precog.api;

import com.precog.api.Request.ContentType;
import com.precog.api.dto.PrecogServiceConfig;
import com.precog.api.options.IngestOptions;
import com.precog.json.ToJson;

import java.io.IOException;


/**
 * A simple client for storing arbitrary records in the Precog database.
 *
 * @author knuttycombe
 */
public class Client {
    private final Service service;
    private final String apiKey;

    private final Rest rest;

    public static final int API_VERSION = 1;

    private static class Paths {
        public static Path FS = new Path("/fs");
    }

    private static class Services {
        public static String ANALYTICS = "/analytics";
        public static String ACCOUNTS = "/accounts";
        public static String INGEST = "/ingest";
    }

    /**
     * Factory method to create a Precog client from a Heroku addon token
     * @param precogToken Heroku precog addon token
     * @return Precog client
     */
    public static Client fromHeroku(String precogToken) {
        return new Client(PrecogServiceConfig.fromToken(precogToken));
    }

    /**
     * A convenience constructor that uses the default production API.
     * Note: during the Precog beta period, you must use the two-argment constructor
     * and provide the specific Service instance for the storage server URL provided
     * with your integration instructions.
     *
     * @param apiKey The string token that permits storage of records at or below the
     *               virtual filesystem path to be used
     */
    public Client(String apiKey) {
        this.service = Service.ProductionHttps;
        this.apiKey = apiKey;
        this.rest = new Rest(service, apiKey);
    }

    /**
     * Builds a new client to connect to precog services based on an PrecogServiceConfig
     * @param ac account token
     */
    public Client(PrecogServiceConfig ac){
        this.service=ServiceBuilder.service(ac.getHost());
        this.apiKey=ac.getApiKey();
        this.rest= new Rest(service,apiKey);
    }

    /**
     * Builds a new client to connect to precog services
     *
     * @param service service to connect
     * @param apiKey  api key to use
     */
    public Client(Service service, String apiKey) {
        this.service = service;
        this.apiKey = apiKey;
        this.rest = new Rest(service, apiKey);
    }

    /**
     * Get the Api Key used by this client to store data.
     *
     * @return the value of apiKey
     */
    public String getApiKey() {
        return apiKey;
    }


    /**
     * Builds a path given a service and path, using the current api version
     *
     * @param service the name of the API service to access (eg. account, ingest,etc)
     * @param path    The path corresponding to the action to be performed
     * @return Path of the form /$service/v$version/$path
     */
    public Path actionPath(String service, Path path) {
        return new Path(service + "/v" + API_VERSION).append(path);
    }

    /**
     * Creates a new account ID, accessible by the specified email address and password, or returns the existing account ID.
     *
     * @param email    user's email
     * @param password user's password
     * @return Json string with the account Id
     * @throws IOException
     */
    public String createAccount(String email, String password) throws IOException {
        Request r = new Request();
        r.setBody("{ \"email\": \"" + email + "\", \"password\": \"" + password + "\" }");
        return rest.request(Rest.Method.POST, actionPath(Services.ACCOUNTS, new Path("accounts/")).getPath(), r);
    }


    /**
     * Retrieves the details about a particular account. This call is the primary mechanism by which you can retrieve your master API key.
     *
     * @param email     user's email
     * @param password  user's password
     * @param accountId account's id number
     * @return account info
     * @throws IOException
     */
    public String describeAccount(String email, String password, String accountId) throws IOException {
        Request r = new Request();
        Rest.addBaseAuth(r.getHeader(), email, password);
        return rest.request(Rest.Method.GET, actionPath(Services.ACCOUNTS, new Path("accounts/" + accountId)).getPath(), r);
    }


    /**
     * Store the specified record.
     *
     * @param <T>        The type of the record object. This type must be serializable to JSON using a ToJson instance
     *                   for some supertype of the specified type.
     * @param path       The path at which the record should be placed in the virtual file system.
     * @param record     The record being storeed.
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
        IngestOptions options = new IngestOptions(ContentType.JSON);
        ingest(path, recordJson, options);
    }

    /**
     * Builds the async/sync data storage path
     *
     * @param async boolean, true to do an async storage call
     * @param path  The path at which the record should be placed in the virtual file system.
     * @return full path
     */
    public Path buildStoragePath(boolean async, Path path) {
        return new Path(async ? "async" : "sync").append(Paths.FS).append(path);
    }

    /**
     * Builds a sync data storage path
     *
     * @param path The path at which the record should be placed in the virtual file system.
     * @return full path
     */
    public Path buildStoragePath(Path path) {
        return buildStoragePath(false, path);
    }


    /**
     * Ingest data in the specified path
     * Ingest behavior is controlled by the ingest options
     * <p/>
     * If Async is true,  Asynchronously uploads data to the specified path and file name. The method will return almost immediately with an HTTP ACCEPTED response.
     * If Async is false, Synchronously uploads data to the specified path and file name. The method will not return until the data has been committed to the transaction log. Queries may or may not reflect data committed to the transaction log.
     * The optional owner account ID parameter can be used to disambiguate the account that owns the data, if the API key has multiple write grants to the path with different owners.
     *
     * @param path    The path at which the record should be placed in the virtual file system.
     * @param content content to be ingested
     * @param options Ingestion options
     * @return ingest result
     * @throws IOException
     */
    public String ingest(Path path, String content, IngestOptions options) throws IOException {
        if (content == null || content.equals("")) {
            throw new IllegalArgumentException("argument 'content' must contain a non empty value formatted as described by type");
        }
        Request request = new Request();
        request.getHeader().putAll(options.asMap());
        request.setBody(content);
        request.setContentType(options.getDataType());
        return rest.request(Rest.Method.POST, actionPath(Services.INGEST, buildStoragePath(options.isAsync(), path)).getPath(), request);
    }

    /**
     * Deletes the specified path
     *
     * @param path
     * @return
     * @throws IOException
     */
    public String delete(Path path) throws IOException {
        Request request = new Request();
        return rest.request(Rest.Method.DELETE, actionPath(Services.INGEST, buildStoragePath(path)).getPath(), request);
    }

    /**
     * Executes a synchronous query relative to the specified base path. The HTTP connection will remain open for as long as the query is evaluating (potentially minutes).
     * Not recommended for long-running queries, because if the connection is interrupted, there will be no way to retrieve the results of the query.
     *
     * @param path relative storage path to query
     * @param q    quirrel query to excecute
     * @return result as Json string
     * @throws IOException
     */
    public String query(Path path, String q) throws IOException {
        if (!path.getPrefix().equals(Paths.FS)) {
            path = Paths.FS.append(path);
        }
        Request request = new Request();
        request.getParams().put("q", q);
        return rest.request(Rest.Method.GET, actionPath(Services.ANALYTICS, path).getPath(), request);

    }
}
