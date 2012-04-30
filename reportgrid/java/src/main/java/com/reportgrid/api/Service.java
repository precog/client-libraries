package com.reportgrid.api;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * An interface wrapping a URL for a ReportGrid service.
 *
 * @author knuttycombe
 */
public interface Service {
  public URL serviceUrl();

	/**
	 * The default production http service.
	 */
	public static final Service ProductionHttp = new Service() {
		@Override public URL serviceUrl() {
			try {
				return new URL("http", "api.reportgrid.com", 80, "/services/analytics/v1/");
			} catch (MalformedURLException ex) {
				Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Invalid client URL", ex);
			}

			return null;
		}
	};

	/**
	 * The default production https service.
	 */
	public static final Service ProductionHttps = new Service() {
		@Override public URL serviceUrl() {
			try {
				return new URL("https", "api.reportgrid.com", 443, "/services/analytics/v1/");
			} catch (MalformedURLException ex) {
				Logger.getLogger(Service.class.getName()).log(Level.SEVERE, "Invalid client URL", ex);
			}

			return null;
		}
	};
}
