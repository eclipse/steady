package com.sap.psr.vulas.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusData {
	private String instance;
	
	private String repositoryId;
	
	private Collection<NexusLibId> gavs;

	public String getInstance() {
		return instance;
	}

	public void setInstance(String instance) {
		this.instance = instance;
	}

	public String getRepositoryId() {
		return repositoryId;
	}

	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	public Collection<NexusLibId> getGavs() {
		return gavs;
	}

	public void setGavs(Collection<NexusLibId> gavs) {
		this.gavs = gavs;
	}
}
