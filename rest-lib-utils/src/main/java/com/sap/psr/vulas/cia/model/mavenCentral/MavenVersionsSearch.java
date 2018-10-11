package com.sap.psr.vulas.cia.model.mavenCentral;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corresponds to the JSON object structure returned by the RESTful search of the Maven Central.
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenVersionsSearch {

	private MavenSearchResponse response;
	
	public MavenVersionsSearch() {}

	public MavenSearchResponse getResponse() { return response; }
	public void setResponse(MavenSearchResponse response) { this.response = response; }	
}
