package com.precog.api;

import com.google.gson.annotations.SerializedName;
import com.google.gson.reflect.TypeToken;
import com.precog.api.Request.ContentType;
import com.precog.api.dto.AccountInfo;
import com.precog.api.dto.IngestResult;
import com.precog.api.options.CSVIngestOptions;
import com.precog.api.options.IngestOptions;
import com.precog.json.RawStringToJson;
import com.precog.json.ToJson;
import com.precog.json.gson.GsonFromJson;
import com.precog.json.gson.GsonToJson;
import com.precog.json.gson.RawJson;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.Ignore;

import java.io.IOException;import java.util.Map;

import static org.junit.Assert.*;

/**
 * Unit test for basic client.
 */
public class ClientTest {

    private static String testId = null;
    private static Path testPath = null;

    public static String testAccountId;
    public static String testApiKey;
    public static Client testClient;

    private static class TestData {
        public final int testInt;
        public final String testStr;
        @SerializedName("~raw")
        public final RawJson testRaw;


        public TestData(int testInt, String testStr, RawJson testRaw) {
            this.testInt = testInt;
            this.testStr = testStr;
            this.testRaw = testRaw;
        }
    }

    @BeforeClass
    public static void beforeAll() throws Exception {
        testId = "" + Double.valueOf(java.lang.Math.random() * 10000).intValue();

        String host=System.getProperty("host");
        Service svc;
        if (host == null){
            svc=Service.DevPrecogHttps;
        } else {
            svc= ServiceBuilder.service(host);
        }

        Client noApiKeyClient = new Client(svc, null);
        String result = noApiKeyClient.createAccount("java-test@precog.com", "password");
        AccountInfo res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        testAccountId = res.getAccountId();
        result = noApiKeyClient.describeAccount("java-test@precog.com", "password", testAccountId);
        res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        testApiKey = res.getApiKey();
        testPath = new Path(testAccountId).append(new Path("/test" + testId));
        testClient =new Client(svc, testApiKey);
    }

    @Test
    public void testStore() throws IOException {
        ToJson<Object> toJson = new GsonToJson();


        RawJson testJson = new RawJson("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
        TestData testData = new TestData(42, "Hello\" World", testJson);

        Record<TestData> testRecord = new Record<TestData>(testData);
        testClient.store(testPath, testRecord, toJson);
    }

    @Test
    public void testStoreStrToJson() throws IOException {
        ToJson<String> toJson = new RawStringToJson();


        Record<String> testRecord = new Record<String>("{\"test\":[{\"v\": 1}, {\"v\": 2}]}");
        testClient.store(testPath, testRecord, toJson);
    }

    @Test
    public void testStoreRawString() throws IOException {


        String rawJson = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
        testClient.store(testPath, rawJson);
    }

    @Test
    public void testStoreRawUTF8() throws IOException {

        String rawJson = "{\"test\":[{\"ดีลลิเชียส\": 1}, {\"v\": 2}]}";
        testClient.store(testPath, rawJson);
    }

    @Test
    public void testIngestCSV() throws IOException {

        IngestOptions options = new CSVIngestOptions();
        String response = testClient.ingest(testPath, "blah,\n\n", options);
        IngestResult result = GsonFromJson.of(new TypeToken<IngestResult>() {
        }).deserialize(response);
        assertEquals(1, result.getIngested());
    }

    @Test
    public void testIngestJSON() throws IOException {

        IngestOptions options = new IngestOptions(ContentType.JSON);
        String rawJson = "{\"test\":[{\"v\": 1}, {\"v\": 2}]}";
        String response = testClient.ingest(testPath, rawJson, options);
        IngestResult result = GsonFromJson.of(new TypeToken<IngestResult>() {
        }).deserialize(response);
        assertEquals(1, result.getIngested());
    }

    @Test
    public void testIngestCsvWithOptions() throws IOException {

        CSVIngestOptions options = new CSVIngestOptions();
        options.setDelimiter(",");
        options.setQuote("'");
        options.setEscape("\\");
        String response = testClient.ingest(testPath, "blah\n\n", options);
        IngestResult result = GsonFromJson.of(new TypeToken<IngestResult>() {
        }).deserialize(response);
        assertEquals(1, result.getIngested());
    }

    @Test
    public void testIngestAsync() throws IOException {

        IngestOptions options = new CSVIngestOptions();
        options.setAsync(true);
        String response = testClient.ingest(testPath, "blah,\n\n", options);
        //is async, so we don't expect results
        assertEquals("", response);
    }

    @Test
    public void testRawJson() throws IOException {
        ToJson<Object> toJson = new GsonToJson();

        String testString = "{\"test\":[{\"v\":1},{\"v\":2}]}";
        RawJson testJson = new RawJson(testString);
        TestData testData = new TestData(42, "Hello\" World", testJson);

        Record<TestData> testRecord = new Record<TestData>(testData);

        String expected = new StringBuilder("{")
                .append("\"testInt\":").append(42).append(",")
                .append("\"testStr\":\"Hello\\\" World\",")
                .append("\"~raw\":").append(testString)
                .append("}")
                .toString();

        assertEquals(expected, testRecord.toJson(toJson));
    }

    @Test
    public void testOddCharacters() throws IOException {
        ToJson<Object> toJson = new GsonToJson();
        TestData testData = new TestData(1, "™", new RawJson(""));

        Record<TestData> testRecord = new Record<TestData>(testData);

        String expected = new StringBuilder("{")
                .append("\"testInt\":").append(1).append(",")
                .append("\"testStr\":\"™\"")
                .append("}")
                .toString();

        String result = testRecord.toJson(toJson);

        assertEquals(expected, result);
    }


    @Test
    public void testCreateAccount() throws IOException {
        String result = testClient.createAccount("java-test@precog.com", "password");
        assertNotNull(result);
        AccountInfo res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        String accountId = res.getAccountId();
        assertNotNull(accountId);
        assertNotSame("", accountId);
        assertEquals(testAccountId, accountId);
    }

    @Test
    public void testDescribeAccount() throws IOException {
        String result = testClient.describeAccount("java-test@precog.com", "password", testAccountId);
        assertNotNull(result);
        AccountInfo res = GsonFromJson.of(new TypeToken<AccountInfo>() {
        }).deserialize(result);
        assertEquals(testAccountId, res.getAccountId());
    }

    @Test
    @Ignore
    public void testQuery() throws IOException {
        //just test the query was sent and executed successfully

        String result = testClient.query(new Path(testAccountId), "count(//" + testAccountId + ")");
        assertNotNull(result);
        String[] res = GsonFromJson.of(String[].class).deserialize(result);
        assertEquals("0", res[0]);
    }

}
