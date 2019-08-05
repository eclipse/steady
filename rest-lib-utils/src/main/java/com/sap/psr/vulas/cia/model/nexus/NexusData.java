package com.sap.psr.vulas.cia.model.nexus;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;


/**
 * <p>NexusData class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusData {
	private String instance;
	
	private String repositoryId;
	
	private Collection<NexusLibId> gavs;

	/**
	 * <p>Getter for the field <code>instance</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getInstance() {
		return instance;
	}

	/**
	 * <p>Setter for the field <code>instance</code>.</p>
	 *
	 * @param instance a {@link java.lang.String} object.
	 */
	public void setInstance(String instance) {
		this.instance = instance;
	}

	/**
	 * <p>Getter for the field <code>repositoryId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRepositoryId() {
		return repositoryId;
	}

	/**
	 * <p>Setter for the field <code>repositoryId</code>.</p>
	 *
	 * @param repositoryId a {@link java.lang.String} object.
	 */
	public void setRepositoryId(String repositoryId) {
		this.repositoryId = repositoryId;
	}

	/**
	 * <p>Getter for the field <code>gavs</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<NexusLibId> getGavs() {
		return gavs;
	}

	/**
	 * <p>Setter for the field <code>gavs</code>.</p>
	 *
	 * @param gavs a {@link java.util.Collection} object.
	 */
	public void setGavs(Collection<NexusLibId> gavs) {
		this.gavs = gavs;
	}
}
