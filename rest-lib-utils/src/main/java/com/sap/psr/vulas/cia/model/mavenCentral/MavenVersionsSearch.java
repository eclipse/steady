package com.sap.psr.vulas.cia.model.mavenCentral;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * Corresponds to the JSON object structure returned by the RESTful search of the Maven Central.
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class MavenVersionsSearch {

	private MavenSearchResponse response;
	
	/**
	 * <p>Constructor for MavenVersionsSearch.</p>
	 */
	public MavenVersionsSearch() {}

	/**
	 * <p>Getter for the field <code>response</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.cia.model.mavenCentral.MavenSearchResponse} object.
	 */
	public MavenSearchResponse getResponse() { return response; }
	/**
	 * <p>Setter for the field <code>response</code>.</p>
	 *
	 * @param response a {@link com.sap.psr.vulas.cia.model.mavenCentral.MavenSearchResponse} object.
	 */
	public void setResponse(MavenSearchResponse response) { this.response = response; }	
}
