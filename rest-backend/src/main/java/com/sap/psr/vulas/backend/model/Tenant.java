package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import java.util.Calendar;
import java.util.Collection;
import java.util.HashSet;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

/** Tenant class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"createdAt", "lastModified"},
    allowGetters = true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
@Entity
@Table(name = "Tenant", uniqueConstraints = @UniqueConstraint(columnNames = {"tenantToken"}))
public class Tenant {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @Column(nullable = false, length = 64)
  private String tenantToken = null;

  @Column(nullable = false, length = 1024)
  private String tenantName = null;

  @Column private boolean isDefault = false;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar createdAt;

  @Temporal(TemporalType.TIMESTAMP)
  @JsonFormat(
      shape = JsonFormat.Shape.STRING,
      pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ",
      timezone = "GMT")
  private java.util.Calendar lastModified;

  @OneToMany(
      cascade = {CascadeType.REMOVE},
      fetch = FetchType.EAGER,
      mappedBy = "tenant",
      orphanRemoval = true)
  @JsonManagedReference
  @JsonView(Views.Never.class)
  private Collection<Space> spaces;

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
   * hasTenantName.
   *
   * @return a boolean.
   */
  public boolean hasTenantName() {
    return this.tenantName != null && !this.tenantName.equals("");
  }

  /**
   * Getter for the field <code>createdAt</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getCreatedAt() {
    return createdAt;
  }
  /**
   * Setter for the field <code>createdAt</code>.
   *
   * @param createdAt a {@link java.util.Calendar} object.
   */
  public void setCreatedAt(java.util.Calendar createdAt) {
    this.createdAt = createdAt;
  }

  /**
   * Getter for the field <code>lastModified</code>.
   *
   * @return a {@link java.util.Calendar} object.
   */
  public java.util.Calendar getLastModified() {
    return lastModified;
  }
  /**
   * Setter for the field <code>lastModified</code>.
   *
   * @param lastModified a {@link java.util.Calendar} object.
   */
  public void setLastModified(java.util.Calendar lastModified) {
    this.lastModified = lastModified;
  }

  /**
   * Getter for the field <code>spaces</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Space> getSpaces() {
    return spaces;
  }
  /**
   * Setter for the field <code>spaces</code>.
   *
   * @param spaces a {@link java.util.Collection} object.
   */
  public void setSpaces(Collection<Space> spaces) {
    this.spaces = spaces;
  }
  /**
   * addSpace.
   *
   * @param _space a {@link com.sap.psr.vulas.backend.model.Space} object.
   */
  public void addSpace(Space _space) {
    if (this.getSpaces() == null) this.spaces = new HashSet<Space>();
    this.spaces.add(_space);
  }

  /** prePersist. */
  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) this.setCreatedAt(Calendar.getInstance());
    this.setLastModified(Calendar.getInstance());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((tenantToken == null) ? 0 : tenantToken.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Tenant other = (Tenant) obj;
    if (tenantToken == null) {
      if (other.tenantToken != null) return false;
    } else if (!tenantToken.equals(other.tenantToken)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "tenant [token=" + tenantToken + ", name=" + tenantName + "]";
  }

  /**
   * isTransient.
   *
   * @return a boolean.
   */
  @JsonIgnore
  public boolean isTransient() {
    return this.id == null;
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
