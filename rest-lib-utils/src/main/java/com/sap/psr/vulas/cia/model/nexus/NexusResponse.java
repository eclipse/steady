package com.sap.psr.vulas.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusResponse {

	private Collection<NexusSearch> searches;
	
	public NexusResponse() {}

	public Collection<NexusSearch> getSearches() {
		return searches;
	}

	public void setSearches(Collection<NexusSearch> searches) {
		this.searches = searches;
	}

	
}
