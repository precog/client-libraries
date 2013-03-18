package com.precog.api.dto;

import javax.xml.bind.DatatypeConverter;
import java.io.UnsupportedEncodingException;

/**
 * User: gabriel
 * Date: 3/17/13
 *
 * All the information needed to connect to precog
 * Intended to build form the token provided from Heroku addon accounts
 */
public class PrecogServiceConfig {

    private String user;
    private String password;
    private String host;
    private String accountId;
    private String apiKey;
    private String rootPath;

    public PrecogServiceConfig(String user, String password, String host, String accountId, String apiKey, String rootPath) {
        this.user = user;
        this.password = password;
        this.host = host;
        this.accountId = accountId;
        this.apiKey = apiKey;
        this.rootPath = rootPath;
    }

    public static PrecogServiceConfig fromToken(String token){
        byte[] data=DatatypeConverter.parseBase64Binary(token);
        String decoded= new String(data);
        String[] values=decoded.split(":");
        return new PrecogServiceConfig(values[0],values[1],values[2],values[3],values[4],values[5]);
    }

    public String toToken() throws UnsupportedEncodingException {
        return DatatypeConverter.printBase64Binary((user+":"+password+":"+host+":"+accountId+":"+apiKey+":"+ rootPath).getBytes("UTF-8"));
    }

    public String getUser() {
        return user;
    }

    public String getPassword() {
        return password;
    }

    public String getHost() {
        return host;
    }

    public String getAccountId() {
        return accountId;
    }

    public String getRootPath() {
        return rootPath;
    }

    public String getApiKey() {

        return apiKey;
    }
}
