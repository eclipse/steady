/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
package com.sap.psr.vulas.backend.util;

import java.util.ArrayList;
import java.util.Calendar;
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

import com.sap.psr.vulas.backend.model.Library;
import com.sap.psr.vulas.backend.model.LibraryId;
import com.sap.psr.vulas.shared.enums.DigestAlgorithm;
import com.sap.psr.vulas.shared.enums.ProgrammingLanguage;
import com.sap.psr.vulas.shared.json.JacksonUtil;
import com.sap.psr.vulas.shared.json.model.mavenCentral.MavenVersionsSearch;
import com.sap.psr.vulas.shared.json.model.mavenCentral.ResponseDoc;

/**
 * <p>MavenCentralVerifier class.</p>
 *
 */
public class MavenCentralVerifier implements DigestVerifier {

	private static Logger log = LoggerFactory.getLogger(MavenCentralVerifier.class);
	
	private static Set<ProgrammingLanguage> SUPP_LANG = new HashSet<ProgrammingLanguage>();

	private static Set<DigestAlgorithm> SUPP_ALG = new HashSet<DigestAlgorithm>();
	
	static {
		SUPP_LANG.add(ProgrammingLanguage.JAVA);
		SUPP_ALG.add(DigestAlgorithm.SHA1);
	}
	
	private String url = null;
	
	/** Release timestamp of the given digest (null if unknown). */
	private java.util.Calendar timestamp;
	
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
	public List<LibraryId> verify(final Library _lib) throws VerificationException {
		if(_lib==null || _lib.getDigest()==null)
			throw new IllegalArgumentException("No library or digest provided: [" + _lib + "]");

		this.url = new String("http://search.maven.org/solrsearch/select?q=1:<SHA1>&rows=20&wt=json").replaceAll("<SHA1>", _lib.getDigest());

		String mvnResponse = null;
		List<LibraryId> verified_lids = null;
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
					MavenVersionsSearch json_response = (MavenVersionsSearch) JacksonUtil.asObject(mvnResponse, MavenVersionsSearch.class);
					long num_found = json_response.getResponse().getNumFound();
					
					verified_lids = new ArrayList<LibraryId>();
					if(num_found>1) {
						log.warn("The lookup of SHA1 digest [" + _lib.getDigest() + "] in Maven Central returned [" + num_found + "] artifacts");
					}
					if(num_found>=1) {
						
						for(ResponseDoc d: json_response.getResponse().getSortedDocs()) {
							LibraryId current = new LibraryId(d.getG(),d.getA(),d.getV());
							verified_lids.add(current);
							// take timestamp of the artifact corresponding to the provided libraryId or the
							// first one as that's the one we will use in case the provided one is not among
							// the list of verified 
							if(this.timestamp==null || current.equals(_lib.getLibraryId())) {
								final long ms = d.getTimestamp();;
								this.timestamp = Calendar.getInstance(TimeZone.getTimeZone("GMT"));
								this.timestamp.setTimeInMillis(ms);						
							}
						}	
					}
				}
			} finally {
				response.close();
			}
		} catch (Exception e) {
			throw new VerificationException(_lib, this.url, e);
		}
		return verified_lids;
	}
}
