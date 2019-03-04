package com.sap.psr.vulas.backend.cve;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.regex.Pattern;

import org.apache.http.HttpEntity;
import org.apache.http.client.ClientProtocolException;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.Configuration;
import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Option;
import com.sap.psr.vulas.backend.util.ConnectionUtil;
import com.sap.psr.vulas.shared.cache.Cache;
import com.sap.psr.vulas.shared.cache.CacheException;
import com.sap.psr.vulas.shared.cache.ObjectFetcher;
import com.sap.psr.vulas.shared.util.VulasConfiguration;

/**
 * Reads {@link CVE} from a service configured with "vulas.backend.cveCache.serviceUrl".
 */
public class CveReader2 implements ObjectFetcher<String, Cve> {
	
	private static final String CVE_SERVICE_URL = "vulas.backend.cveCache.serviceUrl";
	
	private static Logger log = LoggerFactory.getLogger(CveReader2.class);
	
	/**
	 * Cache entries are invalidated after one day (1440 min) in order to not miss any changes.
	 */
	private static Cache<String, Cve> CVE_CACHE= new Cache<String, Cve>(new CveReader2(), 1440);
	
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
		
		// URL to read CVE information from
		final String url = VulasConfiguration.getGlobal().getConfiguration().getString(CVE_SERVICE_URL, null);
		if(url==null)
			throw new CacheException("Configuration parameter [" + CVE_SERVICE_URL + "] not set");
		
		try {
			final CloseableHttpClient httpclient = HttpClients.createDefault();
			uri = new String(url).replaceAll("<ID>", _key);
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
			       	cve = buildFromJson(_key, result);
			    }
			} catch (ParseException e) {
				log.error("HTTP GET [uri=" + uri + "] caused an exception: " + e.getMessage());
				throw new CacheException(_key, e);
			} finally {
				response.close();
			}
			log.info("Fetched " + cve + " for key [" + _key + "]");
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
	
	final static SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm");
	
	static Cve buildFromJson(String _id, String _json) throws ParseException {
		final Cve cve = new Cve();
		cve.setId(_id);
		
		final Configuration conf = Configuration.defaultConfiguration();
		//conf.addOptions(Option.DEFAULT_PATH_LEAF_TO_NULL);		
		final Object document = conf.jsonProvider().parse(_json);
		
		cve.setSummary((String)JsonPath.read(document, "$.cve.description.description_data[0].value"));
		
		final String published = JsonPath.read(document, "$.publishedDate");
		final Calendar publ = new GregorianCalendar();
		publ.setTime(format.parse(published));
		cve.setPublished(publ);
		
		final String modified = JsonPath.read(document, "$.lastModifiedDate");
		final Calendar modi = new GregorianCalendar();
		modi.setTime(format.parse(modified));
		cve.setModified(modi);

		String cvss3_version = null;
		Double cvss3_score = null;
		String cvss3_vector = null;
		try {
			cvss3_version = JsonPath.read(document, "$.impact.baseMetricV3.cvssV3.version");
			cvss3_score = JsonPath.read(document, "$.impact.baseMetricV3.cvssV3.baseScore");
			cvss3_vector = JsonPath.read(document, "$.impact.baseMetricV3.cvssV3.vectorString");
		} catch (Exception e) {
			log.warn("Exception when reading CVSS v3 information for CVE [" + _id + "]: " + e.getMessage());
		}
		
		String cvss2_version = null;
		Double cvss2_score = null;
		String cvss2_vector = null;
		try {
			cvss2_version = JsonPath.read(document, "$.impact.baseMetricV2.cvssV2.version");
			cvss2_score = JsonPath.read(document, "$.impact.baseMetricV2.cvssV2.baseScore");
			cvss2_vector = JsonPath.read(document, "$.impact.baseMetricV2.cvssV2.vectorString");
		} catch (Exception e) {
			log.warn("Exception when reading CVSS v2 information for CVE [" + _id + "]: " + e.getMessage());
		}
		
		if(cvss3_version!=null && cvss3_score!=null && cvss3_vector!=null) {
			cve.setCvssScore(cvss3_score.floatValue());
			cve.setCvssVector(cvss3_vector);
			cve.setCvssVersion(cvss3_version);
		}
		else if(cvss2_version!=null && cvss2_score!=null && cvss2_vector!=null) {
			cve.setCvssScore(cvss2_score.floatValue());
			cve.setCvssVector(cvss2_vector);
			cve.setCvssVersion(cvss2_version);
		}
		else {
			return null;
		}
		
		return cve;
	}
}
