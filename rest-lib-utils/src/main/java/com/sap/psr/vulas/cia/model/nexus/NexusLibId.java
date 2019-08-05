package com.sap.psr.vulas.cia.model.nexus;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sap.psr.vulas.shared.json.model.LibraryId;

/**
 * <p>NexusLibId class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NexusLibId {

	private String groupId;
	private String artifactId;
	private String version;
	
	private String repository;
	
	/**
	 * <p>Constructor for NexusLibId.</p>
	 */
	public NexusLibId(){}
	
	/**
	 * <p>Constructor for NexusLibId.</p>
	 *
	 * @param g a {@link java.lang.String} object.
	 * @param a a {@link java.lang.String} object.
	 * @param v a {@link java.lang.String} object.
	 */
	public NexusLibId(String g, String a, String v){
		this.groupId=g;
		this.artifactId=a;
		this.version=v;
		
	}
	
	/**
	 * <p>Getter for the field <code>groupId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getGroupId() {
		return groupId;
	}
	/**
	 * <p>Setter for the field <code>groupId</code>.</p>
	 *
	 * @param groupId a {@link java.lang.String} object.
	 */
	public void setGroupId(String groupId) {
		this.groupId = groupId;
	}
	/**
	 * <p>Getter for the field <code>artifactId</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getArtifactId() {
		return artifactId;
	}
	/**
	 * <p>Setter for the field <code>artifactId</code>.</p>
	 *
	 * @param artifactId a {@link java.lang.String} object.
	 */
	public void setArtifactId(String artifactId) {
		this.artifactId = artifactId;
	}
	/**
	 * <p>Getter for the field <code>version</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getVersion() {
		return version;
	}
	/**
	 * <p>Setter for the field <code>version</code>.</p>
	 *
	 * @param version a {@link java.lang.String} object.
	 */
	public void setVersion(String version) {
		this.version = version;
	}
	

	
	/**
	 * <p>Getter for the field <code>repository</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getRepository() {
		return repository;
	}

	/**
	 * <p>Setter for the field <code>repository</code>.</p>
	 *
	 * @param repository a {@link java.lang.String} object.
	 */
	public void setRepository(String repository) {
		this.repository = repository;
	}

	
}
