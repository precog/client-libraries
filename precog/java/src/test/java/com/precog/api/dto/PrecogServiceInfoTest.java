package com.precog.api.dto;

import org.junit.Test;
import static org.junit.Assert.*;
import javax.xml.bind.DatatypeConverter;

/**
 * User: gabriel
 * Date: 3/18/13
 */
public class PrecogServiceInfoTest {

    static public String user="user";
    static public String password="password";
    static public String host= "beta.host.com";
    static public String accountId="12345";
    static public String apiKey="AAAAA-BBBBB-CCCCCC-DDDDD";
    static public String rootPath ="/00001234/";

    @Test
    public void testFormToken() throws Exception {
        String values=user+":"+password+":"+host+":"+accountId+":"+apiKey+":"+ rootPath;
        String token= DatatypeConverter.printBase64Binary(values.getBytes("UTF-8"));
        PrecogServiceInfo psi=PrecogServiceInfo.fromToken(token);

        assertEquals(psi.getUser(),user);
        assertEquals(psi.getPassword(), password);
        assertEquals(psi.getHost(), host);
        assertEquals(psi.getAccountId(), accountId);
        assertEquals(psi.getApiKey(), apiKey);
        assertEquals(psi.getRootPath(), rootPath);
    }

    @Test
    public void testToToken() throws Exception {
        PrecogServiceInfo psi=new PrecogServiceInfo(user,password,host,accountId,apiKey, rootPath);
        String token=psi.toToken();
        assertEquals(DatatypeConverter.printBase64Binary((user+":"+password+":"+host+":"+accountId+":"+apiKey+":"+ rootPath).getBytes("UTF-8")),token);
    }
}
