package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * <p>Tenant class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tenant implements Serializable {

	private String tenantToken = null;
	
	private String tenantName = null;
	
	private boolean isDefault =false;

	/**
	 * <p>Constructor for Tenant.</p>
	 */
	public Tenant() { this(null, null); }
	
	/**
	 * <p>Constructor for Tenant.</p>
	 *
	 * @param _id a {@link java.lang.String} object.
	 */
	public Tenant(String _id) { this(_id, null); }
	
	/**
	 * <p>Constructor for Tenant.</p>
	 *
	 * @param _token a {@link java.lang.String} object.
	 * @param _name a {@link java.lang.String} object.
	 */
	public Tenant(String _token, String _name) {
		this.tenantToken = _token;
		this.tenantName = _name;
	}
	
	/**
	 * <p>Getter for the field <code>tenantToken</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTenantToken() { return this.tenantToken; }
	/**
	 * <p>Setter for the field <code>tenantToken</code>.</p>
	 *
	 * @param tenantToken a {@link java.lang.String} object.
	 */
	public void setTenantToken(String tenantToken) { this.tenantToken = tenantToken; }
	
	/**
	 * <p>Getter for the field <code>tenantName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getTenantName() { return tenantName; }
	/**
	 * <p>Setter for the field <code>tenantName</code>.</p>
	 *
	 * @param tenantName a {@link java.lang.String} object.
	 */
	public void setTenantName(String tenantName) { this.tenantName = tenantName; }
	
	/**
	 * <p>toString.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String toString() {
		return this.tenantToken;
	}
	
	/**
	 * <p>isDefault.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDefault() {
		return isDefault;
	}

	/**
	 * <p>setDefault.</p>
	 *
	 * @param isDefault a boolean.
	 */
	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
