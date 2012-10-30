package com.precog.api.options;

import com.precog.api.Request.ContentType;

import java.util.HashMap;
import java.util.Map;

/**
 * Optional parameters for ingest
 * User: gabriel
 * Date: 10/26/12
 */
public class IngestOptions {

    public static String OWNER_ACCOUNT_ID = "ownerAccountId";

    private ContentType dataType;
    private String ownerAccountId;
    private boolean async;

    public IngestOptions(ContentType dataType) {
        this.dataType = dataType;
    }

    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (ownerAccountId != null) {
            map.put(OWNER_ACCOUNT_ID, ownerAccountId);
        }
        return map;
    }

    public ContentType getDataType() {
        return dataType;
    }

    public String getOwnerAccountId() {
        return ownerAccountId;
    }

    public void setOwnerAccountId(String ownerAccountId) {
        this.ownerAccountId = ownerAccountId;
    }

    public boolean isAsync() {
        return async;
    }

    public void setAsync(boolean async) {
        this.async = async;
    }
}
