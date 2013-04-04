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

    public static String RECEIPT = "receipt";
    public static String MODE="mode";
    public static String BATCH = "batch";
    public static String STREAMING="streaming";

    private ContentType dataType;
    private String ownerAccountId;
    private boolean batch=true;
    private boolean receipt=true;

    public boolean isBatch() {
        return batch;
    }

    public void setBatch(boolean batch) {
        this.batch = batch;
    }

    public boolean isReceipt() {
        return receipt;
    }

    public void setReceipt(boolean receipt) {
        this.receipt = receipt;
    }

    public IngestOptions(ContentType dataType) {
        this.dataType = dataType;
    }

    public Map<String, String> asMap() {
        Map<String, String> map = new HashMap<String, String>();
        if (ownerAccountId != null) {
            map.put(OWNER_ACCOUNT_ID, ownerAccountId);
        }
        map.put(BATCH,Boolean.toString(batch));
        if(batch){
            map.put(MODE,BATCH);
            map.put(RECEIPT,Boolean.toString(receipt));
        } else {
            map.put(MODE,STREAMING);
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

}
