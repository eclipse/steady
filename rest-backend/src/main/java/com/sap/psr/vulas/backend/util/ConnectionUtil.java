package com.sap.psr.vulas.backend.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

import javax.validation.constraints.NotNull;

import org.apache.http.HttpHost;
import org.apache.http.client.config.RequestConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ConnectionUtil {
	
	private static Logger log = LoggerFactory.getLogger(ConnectionUtil.class);

	public static RequestConfig getProxyConfig() {
		RequestConfig config = null;
		final String phost = System.getProperty("http.proxyHost", null);
		final int pport = new Integer(System.getProperty("http.proxyPort", "-1")).intValue();
		if(phost!=null && pport!=-1) {
			final HttpHost http_proxy = new HttpHost(phost, pport, "http");
	        config = RequestConfig.custom().setProxy(http_proxy).build();
		}
		return config;
	}
	
	public static String readInputStream(@NotNull InputStream _is) throws IOException {
		final StringBuilder result_builder = new StringBuilder();
        try {
        	final BufferedReader rd = new BufferedReader(new InputStreamReader(_is));
			String line;
			while((line = rd.readLine()) != null) {
				result_builder.append(line).append('\r');
			}
			rd.close();
        } finally {
            try {
				_is.close();
			} catch (IOException e) {
				log.error("Error closing input stream: " + e.getMessage());
			}
        }
        return result_builder.toString().trim();
	}
}
