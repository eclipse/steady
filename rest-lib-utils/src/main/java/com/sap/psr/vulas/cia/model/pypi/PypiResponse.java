package com.sap.psr.vulas.cia.model.pypi;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>PypiResponse class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiResponse {
	
	PypiInfo info;
	
	LinkedHashMap<String, ArrayList<PypiRelease>> releases; 

	/**
	 * <p>Getter for the field <code>info</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.cia.model.pypi.PypiInfo} object.
	 */
	public PypiInfo getInfo() {
		return info;
	}

	/**
	 * <p>Setter for the field <code>info</code>.</p>
	 *
	 * @param info a {@link com.sap.psr.vulas.cia.model.pypi.PypiInfo} object.
	 */
	public void setInfo(PypiInfo info) {
		this.info = info;
	}

	/**
	 * <p>Getter for the field <code>releases</code>.</p>
	 *
	 * @return a {@link java.util.LinkedHashMap} object.
	 */
	public LinkedHashMap<String, ArrayList<PypiRelease>> getReleases() {
		return releases;
	}

	/**
	 * <p>Setter for the field <code>releases</code>.</p>
	 *
	 * @param releases a {@link java.util.LinkedHashMap} object.
	 */
	public void setReleases(LinkedHashMap<String, ArrayList<PypiRelease>> releases) {
		this.releases = releases;
	}
	

}
