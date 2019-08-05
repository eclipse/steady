package com.sap.psr.vulas.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * <p>NexusSearch class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusSearch {

	private Collection<NexusLibId> data;

	/**
	 * <p>Getter for the field <code>data</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<NexusLibId> getData() {
		return data;
	}

	/**
	 * <p>Setter for the field <code>data</code>.</p>
	 *
	 * @param data a {@link java.util.Collection} object.
	 */
	public void setData(Collection<NexusLibId> data) {
		this.data = data;
	}
}
