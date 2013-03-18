package com.precog.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * User: gabriel
 * Date: 12/10/12
 */
public class ServiceBuilder {
    public static Service service(String url){
        final String fUrl=url;
        return new Service() {
            @Override public URL serviceUrl() {
                try {
                    return new URL("https", fUrl, 443, "/v1/");
                } catch (MalformedURLException ex) {
                    Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Invalid client URL", ex);
                }

                return null;
            }
        };
    }
}
