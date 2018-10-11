package com.sap.psr.vulas.cia.model.pypi;

import java.util.ArrayList;
import java.util.LinkedHashMap;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class PypiResponse {
	
	PypiInfo info;
	
	LinkedHashMap<String, ArrayList<PypiRelease>> releases; 

	public PypiInfo getInfo() {
		return info;
	}

	public void setInfo(PypiInfo info) {
		this.info = info;
	}

	public LinkedHashMap<String, ArrayList<PypiRelease>> getReleases() {
		return releases;
	}

	public void setReleases(LinkedHashMap<String, ArrayList<PypiRelease>> releases) {
		this.releases = releases;
	}
	

}
