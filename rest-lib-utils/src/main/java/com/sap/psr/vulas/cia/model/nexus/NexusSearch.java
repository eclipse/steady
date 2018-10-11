package com.sap.psr.vulas.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusSearch {

	private Collection<NexusLibId> data;

	public Collection<NexusLibId> getData() {
		return data;
	}

	public void setData(Collection<NexusLibId> data) {
		this.data = data;
	}
}
