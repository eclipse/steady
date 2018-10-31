package com.sap.psr.vulas.backend.model;

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

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.sap.psr.vulas.shared.enums.ExportConfiguration;

@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown=true, value = { "createdAt", "lastModified" }, allowGetters=true) // On allowGetters: https://github.com/FasterXML/jackson-databind/issues/95
@Entity
@Table( name="Space", uniqueConstraints=@UniqueConstraint( columnNames = {"spaceToken" } ) )
public class Space {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy = GenerationType.AUTO)
	@JsonIgnore
	private Long id;

	@ManyToOne(optional = false)
	@JoinColumn(name = "tenant", referencedColumnName = "id")
	@JsonBackReference	 // Required in order to omit the tenant property when de-serializing JSON
	private Tenant tenant= null;
	
	@Column(nullable = false, length = 64)
	private String spaceToken = null;
	
	@Column(nullable = false, length = 1024)
	private String spaceName = null;
	
	@Column(nullable = false)
	private String spaceDescription = null;

	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
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
	@Column
	private boolean isPublic = true;
	
	@Column
	private boolean isDefault = false;
	
	// The default value is used whenever the values is not provided in the request body
	// This implies that modifying an existing space without sending 'bugFilter' will
	// will cause this field to go back to the default value (1)
	@Column
	private int bugFilter = 1; // Only bugs with origin PUBLIC
	
	@ElementCollection(fetch = FetchType.EAGER)
	@CollectionTable(name="SpaceOwners")
	private Set<String> spaceOwners;
	
	/**
	 * Used to store additional space properties, e.g., software identifiers used in other management tools.
	 */
	@ManyToMany(cascade = {}, fetch = FetchType.EAGER)
	private Collection<Property> properties;
	
	public Long getId() { return id; }
	public void setId(Long id) { this.id = id; }
	
	public Tenant getTenant() { return tenant; }
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	
	public String getSpaceToken() { return spaceToken; }
	public void setSpaceToken(String token) { this.spaceToken = token; }

	public String getSpaceName() { return spaceName; }
	public void setSpaceName(String spaceName) { this.spaceName = spaceName; }

	public String getSpaceDescription() { return spaceDescription; }
	public void setSpaceDescription(String spaceDescription) { this.spaceDescription = spaceDescription; }
	
	public boolean hasNameAndDescription() {
		return this.spaceName!=null && this.spaceDescription!=null && !this.spaceName.equals("") && !this.spaceDescription.equals("");
	}
	
	public java.util.Calendar getCreatedAt() { return createdAt; }
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	public java.util.Calendar getLastModified() { return lastModified; }
	public void setLastModified(java.util.Calendar lastModified) { this.lastModified = lastModified; }

	public ExportConfiguration getExportConfiguration() { return exportConfiguration; }
	public void setExportConfiguration(ExportConfiguration exportConfiguration) { this.exportConfiguration = exportConfiguration; }

	public boolean isPublic() { return isPublic; }
	public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
	
	public boolean isDefault() { return isDefault; }
	public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
	
	public int getBugFilter() { return bugFilter; }
	public void setBugFilter(int bugFilter) { this.bugFilter = bugFilter; }
		
	public Set<String> getSpaceOwners() { return spaceOwners; }
	public void setSpaceOwners(Set<String> spaceOwners) { this.spaceOwners = spaceOwners; }
	
	public Collection<Property> getProperties() { return properties; }
	public void setProperties(Collection<Property> properties) { this.properties = properties; }
	
	/**
	 * Returns the value of the {@link Property} with the given name, or null if no such property exists.
	 * 
	 * @param _name
	 * @return
	 */
	@JsonIgnore
	public String getPropertyValue(@NotNull String _name) {
		if(this.getProperties()==null)
			return null;
		for(Property p: this.getProperties()) {
			if(p.getName().equalsIgnoreCase(_name))
				return p.getPropertyValue();
		}
		return null;
	}
	
	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null)
			this.setCreatedAt(Calendar.getInstance());
		this.setLastModified(Calendar.getInstance());
	}
	
	@PreUpdate
	public void preUpdate() {
		this.setLastModified(Calendar.getInstance());
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((spaceToken == null) ? 0 : spaceToken.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Space other = (Space) obj;
		if (spaceToken == null) {
			if (other.spaceToken != null)
				return false;
		} else if (!spaceToken.equals(other.spaceToken))
			return false;
		return true;
	}

	@Override
	public String toString() {
		return "space [token=" + spaceToken + ", name=" + spaceName + ", isTransient=" + this.isTransient() + "]";
	}
	
	@JsonIgnore
	public boolean isTransient() {
		return this.id==null;
	}
}
