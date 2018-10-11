package com.sap.psr.vulas.backend.cve;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.sap.psr.vulas.backend.util.ConnectionUtil;
import com.sap.psr.vulas.shared.cache.Cache;
import com.sap.psr.vulas.shared.cache.CacheException;
import com.sap.psr.vulas.shared.cache.ObjectFetcher;

/**
 * Scrapes {@link CVE} information from CVE Details, e.g., http://www.cvedetails.com/cve-details.php?t=1&cve_id=CVE-2014-0050.
 * Other options:
 * - REST service offered by CIRCL (https://www.circl.lu/services/cve-search/).
 * - https://github.com/cve-search/cve-search
 * 
 *
 */
public class CveReader implements ObjectFetcher<String, Cve> {
	
	private static Logger log = LoggerFactory.getLogger(CveReader.class);
	
	/**
	 * Cache entries are invalidated after one day (1440 min) in order to not miss any changes.
	 */
	private static Cache<String, Cve> CVE_CACHE= new Cache<String, Cve>(new CveReader(), 1440);
	
	public static Cve read(String _key) throws CacheException {
		return CVE_CACHE.get(_key);
	}
	
	public static Cve read(String _key, boolean _force_fetch) throws CacheException {
		return CVE_CACHE.get(_key, _force_fetch);
	}

	/**
	 * Fetches the CVE with the given key from the (remote) backing store.
	 * 
	 */
	public Cve fetch(String _key) throws CacheException {
		Cve cve = null;
		int sc = -1;
		String result = null;
		String uri = null;
		try {
			final CloseableHttpClient httpclient = HttpClients.createDefault();
			//uri = new String("http://cve.circl.lu/api/cve/<ID>").replaceAll("<ID>", _id);
			uri = new String("http://www.cvedetails.com/cve-details.php?t=1&cve_id=<ID>").replaceAll("<ID>", _key);
			log.info("Query details of CVE [" + _key + "] at [" + uri + "]");
			final HttpGet method = new HttpGet(uri);
			if(ConnectionUtil.getProxyConfig()!=null)
				method.setConfig(ConnectionUtil.getProxyConfig());
			final CloseableHttpResponse response = httpclient.execute(method);
			try {
				sc = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
			    if (sc==org.apache.http.HttpStatus.SC_OK && entity != null) {
			       	result = ConnectionUtil.readInputStream(entity.getContent());
			       	//cve = (Cve)JacksonUtil.asObject(result, Cve.class);
			       	cve = scrape(_key, result);
			    }
			} finally {
				response.close();
			}
			log.info("Fetched [" + cve + "] for key [" + _key + "]");
		} catch (ClientProtocolException e) {
			log.error("HTTP GET [uri=" + uri + "] caused an exception: " + e.getMessage());
			throw new CacheException(_key, e);
		} catch (IOException e) {
			log.error("HTTP GET [uri=" + uri + "] caused an exception: " + e.getMessage());
			log.error("Error: " + e.getMessage(), e);
			throw new CacheException(_key, e);
		}
		return cve;
	}
	
	final static Pattern summary_pattern = Pattern.compile(".*cvedetailssummary\">([^<]*)<br>.*", Pattern.DOTALL);
	final static Pattern cvss_pattern = Pattern.compile(".*cvssbox[^>]*>(\\d*\\.\\d*)</div>.*", Pattern.DOTALL);
	final static Pattern date_pattern = Pattern.compile(".*Publish Date : (\\d{4}-\\d{2}-\\d{2})\\s*Last Update Date : (\\d{4}-\\d{2}-\\d{2}).*", Pattern.DOTALL);
	final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd");
	
	private static Cve scrape(String _id, String _html) {
		Cve cve = null;
		Matcher summary_matcher = summary_pattern.matcher(_html);
		Matcher cvss_matcher = cvss_pattern.matcher(_html);
		Matcher date_matcher = date_pattern.matcher(_html);
		if(summary_matcher.matches() &&
		   cvss_matcher.matches() &&
		   date_matcher.matches()) {
			String cvss = null;
			String published = null;
			String modified = null;
			try {
				cve = new Cve();
				cve.setId(_id);
				cve.setSummary(summary_matcher.group(1).trim());
				
				// CVSS
				cvss = cvss_matcher.group(1);
				if(cvss==null || Float.parseFloat(cvss)==0F) {
					cve.setCvssScore(null);
					cve.setCvssVersion(null);
					cve.setCvssVector(null);
				} else {
					cve.setCvssScore(Float.parseFloat(cvss));
					cve.setCvssVersion("2.0"); // CVE Details only shows CVSS v2, no matter whether NVD maintains a v3 score or not
					cve.setCvssVector(null);
				}
				
				// Dates
				published = date_matcher.group(1);
				final Calendar publ = new GregorianCalendar();
				publ.setTime(format.parse(published));
				cve.setPublished(publ);
				modified = date_matcher.group(2);
				final Calendar modi = new GregorianCalendar();
				modi.setTime(format.parse(modified));
				cve.setModified(modi);
			} catch (NumberFormatException e) {
				log.error("Could not read CVSS from [" + cvss + "]");
			} catch (ParseException e) {
				log.error("Could not read dates from [" + published + "] and/or [" + modified + "]");
			}
		}
		return cve;
	}
}
