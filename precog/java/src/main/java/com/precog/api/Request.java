package com.precog.api;

import java.util.HashMap;
import java.util.Map;

/**
 * Handles the http request configuration
 * User: gabriel
 * Date: 10/26/12
 */
public class Request {

    public enum ContentType {

        XZIP("application/x-gzip"),
        ZIP("application/zip"),
        JSON("application/json"),
        CSV("text/csv");

        private String type;

        ContentType(String s) {
            type = s;
        }

        public String getType() {
            return type;
        }

    }

    private Map<String, String> params;
    private Map<String, String> header;
    private String body = "";
    private ContentType contentType = ContentType.JSON;

    public Request() {
        this.params = new HashMap<String, String>();
        this.header = new HashMap<String, String>();
    }

    public Map<String, String> getParams() {
        return params;
    }

    public Map<String, String> getHeader() {
        return header;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public ContentType getContentType() {
        return contentType;
    }

    public void setContentType(ContentType contentType) {
        this.contentType = contentType;
    }
}
