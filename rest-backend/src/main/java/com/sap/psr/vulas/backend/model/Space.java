package com.sap.psr.vulas.backend.model;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;
import java.util.Calendar;
import java.util.Collection;
import java.util.Set;
import javax.persistence.CollectionTable;
import javax.persistence.Column;
import javax.persistence.ElementCollection;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToMany;
import javax.persistence.ManyToOne;
import javax.persistence.PrePersist;
import javax.persistence.PreUpdate;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotNull;

/** Space class. */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(
    ignoreUnknown = true,
    value = {"createdAt", "lastModified"},
    allowGetters = true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
@Entity
@Table(name = "Space", uniqueConstraints = @UniqueConstraint(columnNames = {"spaceToken"}))
public class Space {

  private static final long serialVersionUID = 1L;

  @Id
  @GeneratedValue(strategy = GenerationType.AUTO)
  @JsonIgnore
  private Long id;

  @ManyToOne(optional = false)
  @JoinColumn(name = "tenant", referencedColumnName = "id")
  @JsonBackReference // Required in order to omit the tenant property when de-serializing JSON
  private Tenant tenant = null;

  @Column(nullable = false, length = 64)
  private String spaceToken = null;

  @Column(nullable = false, length = 1024)
  private String spaceName = null;

  @Column(nullable = false)
  private String spaceDescription = null;

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

  // The default value is used whenever the values is not provided in the request body
  // This implies that modifying an existing space without sending 'exportConfiguration' will
  // will cause this field to go back to the default value (AGGREGATED)
  @Column
  @Enumerated(EnumType.STRING)
  private ExportConfiguration exportConfiguration = ExportConfiguration.AGGREGATED;

  // The default value is used whenever the values is not provided in the request body
  // This implies that modifying an existing space without sending 'isPublic' will
  // will cause this field to go back to the default value (true)
  @Column private boolean isPublic = true;

  @Column private boolean isDefault = false;

  @Column private boolean isReadOnly = false;

  // The default value is used whenever the values is not provided in the request body
  // This implies that modifying an existing space without sending 'bugFilter' will
  // will cause this field to go back to the default value (1)
  @Column private int bugFilter = 1; // Only bugs with origin PUBLIC

  @ElementCollection(fetch = FetchType.EAGER)
  @CollectionTable(name = "SpaceOwners")
  private Set<String> spaceOwners;

  /**
   * Used to store additional space properties, e.g., software identifiers used in other management
   * tools.
   */
  @ManyToMany(
      cascade = {},
      fetch = FetchType.EAGER)
  private Collection<Property> properties;

  /**
   * Getter for the field <code>id</code>.
   *
   * @return a {@link java.lang.Long} object.
   */
  public Long getId() {
    return id;
  }
  /**
   * Setter for the field <code>id</code>.
   *
   * @param id a {@link java.lang.Long} object.
   */
  public void setId(Long id) {
    this.id = id;
  }

  /**
   * Getter for the field <code>tenant</code>.
   *
   * @return a {@link com.sap.psr.vulas.backend.model.Tenant} object.
   */
  public Tenant getTenant() {
    return tenant;
  }
  /**
   * Setter for the field <code>tenant</code>.
   *
   * @param tenant a {@link com.sap.psr.vulas.backend.model.Tenant} object.
   */
  public void setTenant(Tenant tenant) {
    this.tenant = tenant;
  }

  /**
   * Getter for the field <code>spaceToken</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceToken() {
    return spaceToken;
  }
  /**
   * Setter for the field <code>spaceToken</code>.
   *
   * @param token a {@link java.lang.String} object.
   */
  public void setSpaceToken(String token) {
    this.spaceToken = token;
  }

  /**
   * Getter for the field <code>spaceName</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceName() {
    return spaceName;
  }
  /**
   * Setter for the field <code>spaceName</code>.
   *
   * @param spaceName a {@link java.lang.String} object.
   */
  public void setSpaceName(String spaceName) {
    this.spaceName = spaceName;
  }

  /**
   * Getter for the field <code>spaceDescription</code>.
   *
   * @return a {@link java.lang.String} object.
   */
  public String getSpaceDescription() {
    return spaceDescription;
  }
  /**
   * Setter for the field <code>spaceDescription</code>.
   *
   * @param spaceDescription a {@link java.lang.String} object.
   */
  public void setSpaceDescription(String spaceDescription) {
    this.spaceDescription = spaceDescription;
  }

  /**
   * hasNameAndDescription.
   *
   * @return a boolean.
   */
  public boolean hasNameAndDescription() {
    return this.spaceName != null
        && this.spaceDescription != null
        && !this.spaceName.equals("")
        && !this.spaceDescription.equals("");
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
   * Getter for the field <code>exportConfiguration</code>.
   *
   * @return a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
   */
  public ExportConfiguration getExportConfiguration() {
    return exportConfiguration;
  }
  /**
   * Setter for the field <code>exportConfiguration</code>.
   *
   * @param exportConfiguration a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
   */
  public void setExportConfiguration(ExportConfiguration exportConfiguration) {
    this.exportConfiguration = exportConfiguration;
  }

  /**
   * isPublic.
   *
   * @return a boolean.
   */
  public boolean isPublic() {
    return isPublic;
  }
  /**
   * setPublic.
   *
   * @param isPublic a boolean.
   */
  public void setPublic(boolean isPublic) {
    this.isPublic = isPublic;
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

  /**
   * isReadOnly.
   *
   * @return a boolean.
   */
  public boolean isReadOnly() {
    return this.isReadOnly;
  }
  /**
   * setReadOnly.
   *
   * @param readOnly a boolean.
   */
  public void setReadOnly(boolean readOnly) {
    this.isReadOnly = readOnly;
  }

  /**
   * Getter for the field <code>bugFilter</code>.
   *
   * @return a int.
   */
  public int getBugFilter() {
    return bugFilter;
  }
  /**
   * Setter for the field <code>bugFilter</code>.
   *
   * @param bugFilter a int.
   */
  public void setBugFilter(int bugFilter) {
    this.bugFilter = bugFilter;
  }

  /**
   * Getter for the field <code>spaceOwners</code>.
   *
   * @return a {@link java.util.Set} object.
   */
  public Set<String> getSpaceOwners() {
    return spaceOwners;
  }
  /**
   * Setter for the field <code>spaceOwners</code>.
   *
   * @param spaceOwners a {@link java.util.Set} object.
   */
  public void setSpaceOwners(Set<String> spaceOwners) {
    this.spaceOwners = spaceOwners;
  }

  /**
   * Getter for the field <code>properties</code>.
   *
   * @return a {@link java.util.Collection} object.
   */
  public Collection<Property> getProperties() {
    return properties;
  }
  /**
   * Setter for the field <code>properties</code>.
   *
   * @param properties a {@link java.util.Collection} object.
   */
  public void setProperties(Collection<Property> properties) {
    this.properties = properties;
  }

  /**
   * Returns the value of the {@link Property} with the given name, or null if no such property
   * exists.
   *
   * @param _name a {@link java.lang.String} object.
   * @return a {@link java.lang.String} object.
   */
  @JsonIgnore
  public String getPropertyValue(@NotNull String _name) {
    if (this.getProperties() == null) return null;
    for (Property p : this.getProperties()) {
      if (p.getName().equalsIgnoreCase(_name)) return p.getPropertyValue();
    }
    return null;
  }

  /** prePersist. */
  @PrePersist
  public void prePersist() {
    if (this.getCreatedAt() == null) this.setCreatedAt(Calendar.getInstance());
    this.setLastModified(Calendar.getInstance());
  }

  /** preUpdate. */
  @PreUpdate
  public void preUpdate() {
    this.setLastModified(Calendar.getInstance());
  }

  /** {@inheritDoc} */
  @Override
  public int hashCode() {
    final int prime = 31;
    int result = 1;
    result = prime * result + ((spaceToken == null) ? 0 : spaceToken.hashCode());
    return result;
  }

  /** {@inheritDoc} */
  @Override
  public boolean equals(Object obj) {
    if (this == obj) return true;
    if (obj == null) return false;
    if (getClass() != obj.getClass()) return false;
    Space other = (Space) obj;
    if (spaceToken == null) {
      if (other.spaceToken != null) return false;
    } else if (!spaceToken.equals(other.spaceToken)) return false;
    return true;
  }

  /** {@inheritDoc} */
  @Override
  public String toString() {
    return "space [token="
        + spaceToken
        + ", name="
        + spaceName
        + ", isTransient="
        + this.isTransient()
        + "]";
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
}
