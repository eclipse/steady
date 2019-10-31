package com.sap.psr.vulas.shared.json.model;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import java.io.Serializable;

/** Tenant class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true)
public class Tenant implements Serializable {

  private String tenantToken = null;

  private String tenantName = null;

  private boolean isDefault = false;

  /** Constructor for Tenant. */
  public Tenant() {
    this(null, null);
  }

  /**
   * Constructor for Tenant.
   *
   * @param _id a {@link java.lang.String} object.
   */
  public Tenant(String _id) {
    this(_id, null);
  }

  /**
   * Constructor for Tenant.
   *
   * @param _token a {@link java.lang.String} object.
   * @param _name a {@link java.lang.String} object.
   */
  public Tenant(String _token, String _name) {
    this.tenantToken = _token;
    this.tenantName = _name;
  }

  /**
   * Getter for the field <code>tenantToken</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getTenantToken() {
    return this.tenantToken;
  }
  /**
   * Setter for the field <code>tenantToken</code>.
   *
   * @param tenantToken a {@link java.lang.String} object.
   */
  public void setTenantToken(String tenantToken) {
    this.tenantToken = tenantToken;
  }

  /**
   * Getter for the field <code>tenantName</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getTenantName() {
    return tenantName;
  }
  /**
   * Setter for the field <code>tenantName</code>.
   *
   * @param tenantName a {@link java.lang.String} object.
   */
  public void setTenantName(String tenantName) {
    this.tenantName = tenantName;
  }

  /**
   * toString.
   *
   * @return a {@link java.lang.String} object.
   */
  public String toString() {
    return this.tenantToken;
  }

  /**
   * isDefault.
   *
   * @return a boolean.
   */
  public boolean isDefault() {
    return isDefault;
  }

  /**
   * setDefault.
   *
   * @param isDefault a boolean.
   */
  public void setDefault(boolean isDefault) {
    this.isDefault = isDefault;
  }
}
