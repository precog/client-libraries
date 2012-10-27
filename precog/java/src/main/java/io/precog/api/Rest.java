package io.precog.api;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

import static java.net.URLEncoder.encode;
import static javax.xml.bind.DatatypeConverter.printBase64Binary;


/**
 * Class to handle the rest actions
 * User: gabriel
 * Date: 10/25/12
 */
class Rest {

    public static enum Method {
        GET, POST, DELETE, PUT
    }


    private Service service;
    private String apiKey;

    /**
     * Constructor  (only visible to the package)
     *
     * @param service service to connect
     * @param apiKey  api key to use
     */
    Rest(Service service, String apiKey) {
        this.service = service;
        this.apiKey = apiKey;
    }

    /**
     * Creates a parameter string for use in url, in the form $key=$value UTF-8 encoded
     *
     * @param key
     * @param value
     * @return a single parameter string
     * @throws UnsupportedEncodingException
     */
    private String urlParameter(String key, String value) throws UnsupportedEncodingException {
        return key + "=" + encode(value, "UTF-8");
    }

    /**
     * Adds base authentication to a header map
     *
     * @param headers  map with header parameters
     * @param user     user id
     * @param password user password
     */
    public static void addBaseAuth(Map<String, String> headers, String user, String password) {
        headers.put("Authorization", "Basic " + printBase64Binary((user + ":" + password).getBytes()));
    }

    //url = already composed service + v + action

    /**
     * Sends a http request and parses the result
     *
     * @param method  request HTTP method ( GET, POST, DELETE,...)
     * @param path    full path for the request (i.e. /$service/v$version/$action )
     * @param request request configuration
     * @return Server response as string
     * @throws IOException
     */
    public String request(Method method, String path, Request request) throws IOException {

        //add parameters
        if (apiKey != null) {
            request.getParams().put("apiKey", apiKey);
        }
        char prefix = '?';
        for (Map.Entry<String, String> param : request.getParams().entrySet()) {
            path = path + prefix + urlParameter(param.getKey(), param.getValue());
            prefix = '&';
        }

        URL serviceURL = new URL(service.serviceUrl(), path);
        HttpURLConnection conn = (HttpURLConnection) serviceURL.openConnection();

        conn.setRequestMethod(method.toString());

        //add headers
        for (Map.Entry<String, String> e : request.getHeader().entrySet()) {
            conn.setRequestProperty(e.getKey(), e.getValue());
        }
        conn.setRequestProperty("Content-Type", request.getContentType().getType());

        if (request.getBody().length() > 0) {
            byte[] bodyBytes = request.getBody().getBytes("UTF-8");
            conn.setRequestProperty("Content-Length", "" + bodyBytes.length);
            conn.setDoOutput(true);
            DataOutputStream out = new DataOutputStream(conn.getOutputStream());
            try {
                out.write(bodyBytes, 0, bodyBytes.length);
            } finally {
                out.flush();
                out.close();
            }
        }

        if (conn.getResponseCode() != HttpURLConnection.HTTP_OK && conn.getResponseCode() != HttpURLConnection.HTTP_ACCEPTED) {
            throw new IOException(
                    "Unexpected response from server: " + conn.getResponseCode() + ": " + conn.getResponseMessage() +
                            " ; service url " + serviceURL +
                            " ; " + (request.getBody().length() > 0 ? "record body '" + request.getBody() + "'" : " no body"));
        }
        String result;
        StringBuilder sb = new StringBuilder();
        BufferedReader buff = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        try {
            while ((inputLine = buff.readLine()) != null) {
                sb.append(inputLine);
            }
        } finally {
            buff.close();
        }
        result = sb.toString();
        return result;
    }
}
