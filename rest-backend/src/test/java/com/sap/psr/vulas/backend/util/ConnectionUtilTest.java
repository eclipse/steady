package com.sap.psr.vulas.backend.util;

import org.apache.http.client.config.RequestConfig;
import org.junit.Test;
import static org.junit.Assert.assertEquals;

public class ConnectionUtilTest {

	@Test
	public void testGetProxy() {
		
		String host = System.getProperty("http.proxyHost");
		String port = System.getProperty("http.proxyPort");
		
		RequestConfig g = ConnectionUtil.getProxyConfig();
		
		System.setProperty("http.proxyHost", "proxy");
		System.setProperty("http.proxyPort", "80");
		g = ConnectionUtil.getProxyConfig();
		assertEquals(g.getProxy().toString(), "http://proxy:80");
			
		System.setProperty("http.proxyPort", "");
		g = ConnectionUtil.getProxyConfig();
		assertEquals(g,null);
		//System.out.println(g.getProxy());
	
		if(host!=null)
			System.setProperty("http.proxyHost", host);
		if(port!=null)
			System.setProperty("http.proxyPort", port);
	}
}
