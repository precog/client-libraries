package io.precog.api.options;

import io.precog.api.Request.ContentType;

import java.util.Map;

/**
 * Ingest options specific for CSV
 * User: gabriel
 * Date: 10/26/12
 */
public class CSVIngestOptions extends IngestOptions {

    public static String QUOTE = "quote";
    public static String ESCAPE = "escape";
    public static String DELIMITER = "delimiter";

    private String delimiter;
    private String quote;
    private String escape;

    public CSVIngestOptions() {
        super(ContentType.CSV);
    }

    @Override
    public Map<String, String> asMap() {
        Map<String, String> map = super.asMap();
        if (quote != null) {
            map.put(QUOTE, quote);
        }
        if (delimiter != null) {
            map.put(DELIMITER, delimiter);
        }
        if (escape != null) {
            map.put(ESCAPE, escape);
        }
        return map;
    }

    public String getDelimiter() {
        return delimiter;
    }

    public void setDelimiter(String delimiter) {
        this.delimiter = delimiter;
    }

    public String getQuote() {
        return quote;
    }

    public void setQuote(String quote) {
        this.quote = quote;
    }

    public String getEscape() {
        return escape;
    }

    public void setEscape(String escape) {
        this.escape = escape;
    }
}
