package com.sap.psr.vulas.cia.model.npmRegistry;


import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

/**
 * <p>PypiRelease class.</p>
 *
 */
@JsonIgnoreProperties(ignoreUnknown = true)
public class NpmRegistryVersion {
	
	String _id;
	String version;
	String name;
	
	/**
	 * <p>Getter for the field <code>_id</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getId() {
		return _id;
	}
	/**
	 * <p>Setter for the field <code>_id</code>.</p>
	 *
	 * @param _id a {@link java.lang.String} object.
	 */
	public void setId(String _id) {
		this._id = _id;
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
	public void setPython_version(String version) {
		this.version = version;
	}
	/**
	 * <p>Getter for the field <code>name</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getName() {
		return name;
	}
	/**
	 * <p>Setter for the field <code>name</code>.</p>
	 *
	 * @param name a {@link java.lang.String} object.
	 */
	public void setName(String name) {
		this.name = name;
	}
}
