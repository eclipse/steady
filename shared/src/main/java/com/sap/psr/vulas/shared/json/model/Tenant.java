package com.sap.psr.vulas.shared.json.model;

import java.io.Serializable;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tenant implements Serializable {

	private String tenantToken = null;
	
	private String tenantName = null;
	
	private boolean isDefault =false;

	public Tenant() { this(null, null); }
	
	public Tenant(String _id) { this(_id, null); }
	
	public Tenant(String _token, String _name) {
		this.tenantToken = _token;
		this.tenantName = _name;
	}
	
	public String getTenantToken() { return this.tenantToken; }
	public void setTenantToken(String tenantToken) { this.tenantToken = tenantToken; }
	
	public String getTenantName() { return tenantName; }
	public void setTenantName(String tenantName) { this.tenantName = tenantName; }
	
	public String toString() {
		return this.tenantToken;
	}
	
	public boolean isDefault() {
		return isDefault;
	}

	public void setDefault(boolean isDefault) {
		this.isDefault = isDefault;
	}
}
