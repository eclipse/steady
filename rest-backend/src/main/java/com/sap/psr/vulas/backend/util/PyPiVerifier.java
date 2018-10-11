package com.sap.psr.vulas.backend.util;

import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.http.HttpEntity;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.CloseableHttpResponse;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.CloseableHttpClient;
import org.apache.http.impl.client.HttpClients;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.jayway.jsonpath.Criteria;
import com.jayway.jsonpath.Filter;
import com.jayway.jsonpath.JsonPath;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

public class PyPiVerifier implements DigestVerifier {
	
	private static Logger log = LoggerFactory.getLogger(PyPiVerifier.class);

	private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();

	private static Set<DigestAlgorithm> SUPP_ALG = new HashSet<DigestAlgorithm>();
	
	static {
		SUPP_LANG.add(ProgrammingLanguage.PY);
		SUPP_ALG.add(DigestAlgorithm.MD5);
	}
	
	private String url = null;
	
	@Override
	public Set<ProgrammingLanguage> getSupportedLanguages() {
		return SUPP_LANG;
	}

	@Override
	public Set<DigestAlgorithm> getSupportedDigestAlgorithms() {
		return SUPP_ALG;
	}

	@Override
	public String getVerificationUrl() { return url; }
	
	@Override
	public Boolean verify(final Library _lib) throws VerificationException {
		if(_lib==null || _lib.getDigest()==null)
			throw new IllegalArgumentException("No library or digest provided: [" + _lib + "]");
		
		if(_lib.getLibraryId()==null || _lib.getLibraryId().getMvnGroup()==null || _lib.getLibraryId().getVersion()==null)
			return null;

		this.url = new String("https://pypi.python.org/pypi/<name>/<version>/json").replaceAll("<name>", _lib.getLibraryId().getMvnGroup()).replaceAll("<version>", _lib.getLibraryId().getVersion());

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
					verified = PyPiVerifier.containsMD5(response_body, _lib.getDigest());

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
	
	public static boolean containsMD5(String _body, final String _digest) {
		/*final Predicate md5_predicate = new Predicate() {
		    public boolean apply(PredicateContext context) {
		        String md5 = context.item(Map.class).get("md5_digest").toString();
		        return md5.equals(_digest);
		    }
		};
		final List<Map<String, Object>> releases = JsonPath.parse(_body)
		  .read("$['releases'][?][?]", md5_predicate);*/
		
		Filter md5_filter = Filter.filter(Criteria.where("[*]['md5_digest']").eq(_digest));
		List<Map<String, Object>> releases = JsonPath.parse(_body).read("$['releases'][*]", md5_filter);
		
		return !releases.isEmpty();
	}
}
