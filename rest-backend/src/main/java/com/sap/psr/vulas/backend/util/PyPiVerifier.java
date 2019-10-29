package com.sap.psr.vulas.backend.util;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.TimeZone;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.JsonPath;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

/**
 * <p>PyPiVerifier class.</p>
 *
 */
public class PyPiVerifier implements DigestVerifier {

	private static Logger log = LoggerFactory.getLogger(PyPiVerifier.class);

	private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();

	private static Set<DigestAlgorithm> SUPP_ALG = new HashSet<DigestAlgorithm>();

	static {
		SUPP_LANG.add(ProgrammingLanguage.PY);
		SUPP_ALG.add(DigestAlgorithm.MD5);
	}

	private String url = null;

	/** Release timestamp of the given digest (null if unknown). */
	private java.util.Calendar timestamp;

	private SimpleDateFormat dateFormat = null;

	/**
	 * <p>Constructor for PyPiVerifier.</p>
	 */
	public PyPiVerifier() {
		dateFormat = new SimpleDateFormat("yyyy-MM-dd'T'hh:mm:ss");
		dateFormat.setTimeZone(TimeZone.getTimeZone("UTC"));
	}

	/** {@inheritDoc} */
	@Override
	public Set<ProgrammingLanguage> getSupportedLanguages() {
		return SUPP_LANG;
	}

	/** {@inheritDoc} */
	@Override
	public Set<DigestAlgorithm> getSupportedDigestAlgorithms() {
		return SUPP_ALG;
	}

	/** {@inheritDoc} */
	@Override
	public String getVerificationUrl() { return url; }

	/** {@inheritDoc} */
	@Override
	public java.util.Calendar getReleaseTimestamp() { return this.timestamp; }

	/** {@inheritDoc} */
	@Override
	public Boolean verify(final Library _lib) throws VerificationException {
		if(_lib==null || _lib.getDigest()==null)
			throw new IllegalArgumentException("No library or digest provided: [" + _lib + "]");

		if(_lib.getLibraryId()==null || _lib.getLibraryId().getMvnGroup()==null || _lib.getLibraryId().getVersion()==null)
			return null;

		this.url = "https://pypi.python.org/pypi/<name>/<version>/json".replaceAll("<name>", _lib.getLibraryId().getMvnGroup()).replaceAll("<version>", _lib.getLibraryId().getVersion());

		String response_body = null;
		Boolean verified = false;
		int sc = -1;
		try {
			final CloseableHttpClient httpclient = HttpClients.createDefault();
			final HttpGet method = new HttpGet(this.url);
			if(ConnectionUtil.getProxyConfig()!=null)
				method.setConfig(ConnectionUtil.getProxyConfig());
			final CloseableHttpResponse response = httpclient.execute(method);
			try {
				sc = response.getStatusLine().getStatusCode();
				HttpEntity entity = response.getEntity();
				if (sc==HttpStatus.SC_OK && entity != null) {
					response_body = ConnectionUtil.readInputStream(entity.getContent());
					verified = this.containsMD5(response_body, _lib.getDigest());

					// Check whether given and returned libid correspond
					//final LibraryId returned_libid = new LibraryId((String)JsonPath.read(response_body, "$.response.docs[0].g"),(String)JsonPath.read(response_body, "$.response.docs[0].a"),(String)JsonPath.read(response_body, "$.response.docs[0].v"));
					//if(_lib.getLibraryId()!=null && !_lib.getLibraryId().equals(returned_libid))
					//	log.warn("Given and returned library identifiers do not match: Given [" + _lib.getLibraryId() + "], returned [" + returned_libid + "]");
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			throw new VerificationException(_lib, this.url, e);
		}
		return verified;
	}

	/**
	 * Returns true if the given JSON (produced by PyPi) contains a release having the given MD5 digest, false otherwise.
	 * Example PyPi response for the Python library called requests: https://pypi.org/pypi/requests/2.18.4/json
	 *
	 * @param _json
	 * @param _md5
	 * @return
	 */
	boolean containsMD5(String _json, final String _md5) {
		final List<String> releases = JsonPath.read(_json, "$.releases..[?(@.md5_digest == \"" + _md5.toLowerCase() + "\")].upload_time");

		// One result, take the release's timestamp
		if(releases.size()==1) {
			final String upload_time = releases.get(0);
			try {
				final Date parsedDate = dateFormat.parse(upload_time);
				this.timestamp = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
				this.timestamp.setTimeInMillis(parsedDate.getTime());
			} catch (ParseException e) {
				log.error("Error when parsing the timestamp [" + upload_time + "] of PyPi package with MD5 digest [" + _md5 + "]");
			}
		}

		// More than 1 result, don't take any timestamp
		else if(releases.size()>1) {
			log.warn("The lookup of MD5 digest [" + _md5 + "] in PyPi returned [" + releases.size() + "] artifacts");
		}

		return !releases.isEmpty();
	}
}
