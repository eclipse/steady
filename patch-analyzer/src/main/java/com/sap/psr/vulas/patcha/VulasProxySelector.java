package com.sap.psr.vulas.patcha;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.ProxySelector;
import java.net.SocketAddress;
import java.net.URI;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.apache.commons.configuration.Configuration;
import org.apache.commons.configuration.ConfigurationException;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.sap.psr.vulas.shared.util.StringList;
import com.sap.psr.vulas.shared.util.StringList.CaseSensitivity;
import com.sap.psr.vulas.shared.util.StringList.ComparisonMode;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

public class VulasProxySelector extends ProxySelector {

	private static final Log log = LogFactory.getLog(VulasProxySelector.class);

	private static ProxySelector instance = new VulasProxySelector();

	public static void registerAsDefault() {
		ProxySelector.setDefault(VulasProxySelector.instance);
	}

	private Proxy httpProxy = null;

	private Configuration cfg = null;

	//private Set<String> noProxyHosts = new HashSet<String>();
	
	private StringList noProxyHosts = new StringList();

	ProxySelector def = null;

	public VulasProxySelector() {

		// Remember current default (fallback solution)
		this.def = ProxySelector.getDefault();

		// Read configuration
		this.cfg = VulasConfiguration.getGlobal().getConfiguration();

		// No proxy for the following hosts
		noProxyHosts.addAll(this.cfg.getString("http.nonProxyHosts", ""), "\\|", true);
		//String[] no_proxy_hosts = this.cfg.getString("http.nonProxyHosts", "").split("\\|");
		//for(int i=0; i<no_proxy_hosts.length; i++) this.noProxyHosts.add(no_proxy_hosts[i]);

		// Create HTTP proxy
		if( this.cfg.getString("http.proxyHost")!=null && !this.cfg.getString("http.proxyHost").equals("") &&
				this.cfg.getString("http.proxyPort")!=null && !this.cfg.getString("http.proxyPort").equals("") ) {
			this.httpProxy = new Proxy( Proxy.Type.HTTP,
					new InetSocketAddress( this.cfg.getString("http.proxyHost"), this.cfg.getInt("http.proxyPort") ) );
			VulasProxySelector.log.info("Proxy selector configuration: [" + this.cfg.getString("http.proxyHost") + ":" + this.cfg.getInt("http.proxyPort") + ", non proxy hosts: "+this.noProxyHosts.toString()+"]");
		} else {
			VulasProxySelector.log.info("Proxy selector configuration: None");
		}
	}

	@Override
	public List<Proxy> select( URI uri ) {
		List<Proxy> l = null;
		if(this.noProxyHosts.contains(uri.getHost(), ComparisonMode.PATTERN, CaseSensitivity.CASE_INSENSITIVE)) {
			VulasProxySelector.log.info("No proxy for URL [" + uri + "]");
			l = Arrays.asList(Proxy.NO_PROXY);
		}
		else {
			if(this.httpProxy!=null && (uri.getScheme().equalsIgnoreCase("http") || uri.getScheme().equalsIgnoreCase("https"))) {
				VulasProxySelector.log.info("Using proxy [" + this.httpProxy + "] for URL [" + uri + "]");
				l = Arrays.asList( this.httpProxy );
			}
			else {
				l = this.def.select(uri);
			}
		}
		return l;
	}

	@Override
	public void connectFailed( URI uri, SocketAddress sa, IOException ioe ) {
		if ( uri == null || sa == null || ioe == null ) {
			throw new IllegalArgumentException( "Arguments can not be null." );
		}
	}
}


