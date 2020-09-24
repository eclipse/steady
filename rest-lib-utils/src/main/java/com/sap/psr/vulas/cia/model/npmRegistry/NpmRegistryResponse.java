package com.sap.psr.vulas.cia.model.npmRegistry;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>PypiResponse class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmRegistryResponse {
	
	LinkedHashMap<String, NpmRegistryVersion> versions; 


	/**
	 * <p>Getter for the field <code>versions</code>.</p>
	 *
	 * @return a {@link java.util.LinkedHashMap} object.
	 */
	public LinkedHashMap<String, NpmRegistryVersion> getVersions() {
		return versions;
	}

	/**
	 * <p>Setter for the field <code>versions</code>.</p>
	 *
	 * @param releases a {@link java.util.LinkedHashMap} object.
	 */
	public void setVersions(LinkedHashMap<String, NpmRegistryVersion> versions) {
		this.versions = versions;
	}
	

}
