package com.precog.api.dto;

/**
 * Result of data ingestion
 * User: gabriel
 * Date: 10/26/12
 */
public class IngestResult {

    private int total;
    private int ingested;
    private int failed;
    private int skipped;
    private String[] errors;

    public int getTotal() {
        return total;
    }

    public void setTotal(int total) {
        this.total = total;
    }

    public int getIngested() {
        return ingested;
    }

    public void setIngested(int ingested) {
        this.ingested = ingested;
    }

    public int getFailed() {
        return failed;
    }

    public void setFailed(int failed) {
        this.failed = failed;
    }

    public int getSkipped() {
        return skipped;
    }

    public void setSkipped(int skipped) {
        this.skipped = skipped;
    }

    public String[] getErrors() {
        return errors;
    }

    public void setErrors(String[] errors) {
        this.errors = errors;
    }
}
