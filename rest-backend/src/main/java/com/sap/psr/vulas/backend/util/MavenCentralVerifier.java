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

import com.jayway.jsonpath.JsonPath;
import com.jayway.jsonpath.Predicate;
import com.jayway.jsonpath.Predicate.PredicateContext;
import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;

public class MavenCentralVerifier implements DigestVerifier {

	private static Logger log = LoggerFactory.getLogger(MavenCentralVerifier.class);
	
	private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();

	private static Set<DigestAlgorithm> SUPP_ALG = new HashSet<DigestAlgorithm>();
	
	static {
		SUPP_LANG.add(ProgrammingLanguage.JAVA);
		SUPP_ALG.add(DigestAlgorithm.SHA1);
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

		this.url = new String("http://search.maven.org/solrsearch/select?q=1:<SHA1>&rows=20&wt=json").replaceAll("<SHA1>", _lib.getDigest());

		String mvnResponse = null;
		Boolean verified = null;
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
					mvnResponse = ConnectionUtil.readInputStream(entity.getContent());
					int num_found = ((Integer)JsonPath.read(mvnResponse, "$.response.numFound")).intValue();
					verified = num_found > 0;
					
					if(num_found==1){
						// Check whether given and returned libid correspond
						final LibraryId returned_libid = new LibraryId((String)JsonPath.read(mvnResponse, "$.response.docs[0].g"),(String)JsonPath.read(mvnResponse, "$.response.docs[0].a"),(String)JsonPath.read(mvnResponse, "$.response.docs[0].v"));
						if(_lib.getLibraryId()!=null && !_lib.getLibraryId().equals(returned_libid))
							log.warn("Given and returned library identifiers do not match: Given [" + _lib.getLibraryId() + "], returned [" + returned_libid + "]");
					}
					else if (num_found>1){
						log.warn("The lookup of the given SHA1 in maven central returned [" + num_found + "] artifacts");
					}
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			throw new VerificationException(_lib, this.url, e);
		}
		return verified;
	}
}
