/**
 * This file is part of Eclipse Steady.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 * SPDX-License-Identifier: Apache-2.0
 *
 * Copyright (c) 2018 SAP SE or an SAP affiliate company. All rights reserved.
 */
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

/**
 * <p>Space class.</p>
 *
 */
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
	
	@Column
	private boolean isReadOnly = false;
	
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
	
	/**
	 * <p>Getter for the field <code>id</code>.</p>
	 *
	 * @return a {@link java.lang.Long} object.
	 */
	public Long getId() { return id; }
	/**
	 * <p>Setter for the field <code>id</code>.</p>
	 *
	 * @param id a {@link java.lang.Long} object.
	 */
	public void setId(Long id) { this.id = id; }
	
	/**
	 * <p>Getter for the field <code>tenant</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Tenant} object.
	 */
	public Tenant getTenant() { return tenant; }
	/**
	 * <p>Setter for the field <code>tenant</code>.</p>
	 *
	 * @param tenant a {@link com.sap.psr.vulas.backend.model.Tenant} object.
	 */
	public void setTenant(Tenant tenant) { this.tenant = tenant; }
	
	/**
	 * <p>Getter for the field <code>spaceToken</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSpaceToken() { return spaceToken; }
	/**
	 * <p>Setter for the field <code>spaceToken</code>.</p>
	 *
	 * @param token a {@link java.lang.String} object.
	 */
	public void setSpaceToken(String token) { this.spaceToken = token; }

	/**
	 * <p>Getter for the field <code>spaceName</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSpaceName() { return spaceName; }
	/**
	 * <p>Setter for the field <code>spaceName</code>.</p>
	 *
	 * @param spaceName a {@link java.lang.String} object.
	 */
	public void setSpaceName(String spaceName) { this.spaceName = spaceName; }

	/**
	 * <p>Getter for the field <code>spaceDescription</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getSpaceDescription() { return spaceDescription; }
	/**
	 * <p>Setter for the field <code>spaceDescription</code>.</p>
	 *
	 * @param spaceDescription a {@link java.lang.String} object.
	 */
	public void setSpaceDescription(String spaceDescription) { this.spaceDescription = spaceDescription; }
	
	/**
	 * <p>hasNameAndDescription.</p>
	 *
	 * @return a boolean.
	 */
	public boolean hasNameAndDescription() {
		return this.spaceName!=null && this.spaceDescription!=null && !this.spaceName.equals("") && !this.spaceDescription.equals("");
	}
	
	/**
	 * <p>Getter for the field <code>createdAt</code>.</p>
	 *
	 * @return a {@link java.util.Calendar} object.
	 */
	public java.util.Calendar getCreatedAt() { return createdAt; }
	/**
	 * <p>Setter for the field <code>createdAt</code>.</p>
	 *
	 * @param createdAt a {@link java.util.Calendar} object.
	 */
	public void setCreatedAt(java.util.Calendar createdAt) { this.createdAt = createdAt; }

	/**
	 * <p>Getter for the field <code>lastModified</code>.</p>
	 *
	 * @return a {@link java.util.Calendar} object.
	 */
	public java.util.Calendar getLastModified() { return lastModified; }
	/**
	 * <p>Setter for the field <code>lastModified</code>.</p>
	 *
	 * @param lastModified a {@link java.util.Calendar} object.
	 */
	public void setLastModified(java.util.Calendar lastModified) { this.lastModified = lastModified; }

	/**
	 * <p>Getter for the field <code>exportConfiguration</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
	 */
	public ExportConfiguration getExportConfiguration() { return exportConfiguration; }
	/**
	 * <p>Setter for the field <code>exportConfiguration</code>.</p>
	 *
	 * @param exportConfiguration a {@link com.sap.psr.vulas.shared.enums.ExportConfiguration} object.
	 */
	public void setExportConfiguration(ExportConfiguration exportConfiguration) { this.exportConfiguration = exportConfiguration; }

	/**
	 * <p>isPublic.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isPublic() { return isPublic; }
	/**
	 * <p>setPublic.</p>
	 *
	 * @param isPublic a boolean.
	 */
	public void setPublic(boolean isPublic) { this.isPublic = isPublic; }
	
	/**
	 * <p>isDefault.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isDefault() { return isDefault; }
	/**
	 * <p>setDefault.</p>
	 *
	 * @param isDefault a boolean.
	 */
	public void setDefault(boolean isDefault) { this.isDefault = isDefault; }
	
	/**
	 * <p>isReadOnly.</p>
	 *
	 * @return a boolean.
	 */
	public boolean isReadOnly() { return this.isReadOnly; }
	/**
	 * <p>setReadOnly.</p>
	 *
	 * @param readOnly a boolean.
	 */
	public void setReadOnly(boolean readOnly) { this.isReadOnly = readOnly; }
	
	/**
	 * <p>Getter for the field <code>bugFilter</code>.</p>
	 *
	 * @return a int.
	 */
	public int getBugFilter() { return bugFilter; }
	/**
	 * <p>Setter for the field <code>bugFilter</code>.</p>
	 *
	 * @param bugFilter a int.
	 */
	public void setBugFilter(int bugFilter) { this.bugFilter = bugFilter; }
		
	/**
	 * <p>Getter for the field <code>spaceOwners</code>.</p>
	 *
	 * @return a {@link java.util.Set} object.
	 */
	public Set<String> getSpaceOwners() { return spaceOwners; }
	/**
	 * <p>Setter for the field <code>spaceOwners</code>.</p>
	 *
	 * @param spaceOwners a {@link java.util.Set} object.
	 */
	public void setSpaceOwners(Set<String> spaceOwners) { this.spaceOwners = spaceOwners; }
	
	/**
	 * <p>Getter for the field <code>properties</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<Property> getProperties() { return properties; }
	/**
	 * <p>Setter for the field <code>properties</code>.</p>
	 *
	 * @param properties a {@link java.util.Collection} object.
	 */
	public void setProperties(Collection<Property> properties) { this.properties = properties; }
	
	/**
	 * Returns the value of the {@link Property} with the given name, or null if no such property exists.
	 *
	 * @param _name a {@link java.lang.String} object.
	 * @return a {@link java.lang.String} object.
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
	
	/**
	 * <p>prePersist.</p>
	 */
	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null)
			this.setCreatedAt(Calendar.getInstance());
		this.setLastModified(Calendar.getInstance());
	}
	
	/**
	 * <p>preUpdate.</p>
	 */
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

	/** {@inheritDoc} */
	@Override
	public String toString() {
		return "[token=" + spaceToken + ", name=" + spaceName + ", isTransient=" + this.isTransient() + "]";
	}
	
	/**
	 * <p>isTransient.</p>
	 *
	 * @return a boolean.
	 */
	@JsonIgnore
	public boolean isTransient() {
		return this.id==null;
	}
}
