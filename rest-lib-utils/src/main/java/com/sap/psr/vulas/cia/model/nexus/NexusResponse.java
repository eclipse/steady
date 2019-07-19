package com.sap.psr.vulas.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>NexusResponse class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusResponse {

	private Collection<NexusSearch> searches;
	
	/**
	 * <p>Constructor for NexusResponse.</p>
	 */
	public NexusResponse() {}

	/**
	 * <p>Getter for the field <code>searches</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<NexusSearch> getSearches() {
		return searches;
	}

	/**
	 * <p>Setter for the field <code>searches</code>.</p>
	 *
	 * @param searches a {@link java.util.Collection} object.
	 */
	public void setSearches(Collection<NexusSearch> searches) {
		this.searches = searches;
	}

	
}
