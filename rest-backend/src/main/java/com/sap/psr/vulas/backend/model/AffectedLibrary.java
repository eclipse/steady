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

import java.io.Serializable;
import java.util.Calendar;
import java.util.Collection;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.EnumType;
import javax.persistence.Enumerated;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.PrePersist;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.fasterxml.jackson.annotation.JsonView;
import com.sap.psr.vulas.backend.model.view.Views;
import com.sap.psr.vulas.shared.enums.AffectedVersionSource;

/**
 * <p>AffectedLibrary class.</p>
 *
 */
@JsonInclude(JsonInclude.Include.ALWAYS)
@JsonIgnoreProperties(ignoreUnknown = true, value = { "createdAt", "createdBy" })
@Entity
@Table( name="BugAffectedLibrary", 
uniqueConstraints={@UniqueConstraint( columnNames = { "bugId", "libraryId", "source" } ),@UniqueConstraint( columnNames = { "bugId", "lib", "source" })} ) 
public class AffectedLibrary implements Serializable {

	private static final long serialVersionUID = 1L;

	@Id
	@GeneratedValue(strategy=GenerationType.AUTO)
	@JsonIgnore
	private Long id;
	
	@ManyToOne(optional = false, cascade = {})
	@JoinColumn(name = "bugId", referencedColumnName = "bugId") // Required for the unique constraint
	@JsonView(Views.LibraryIdDetails.class)
	//@JsonBackReference
	private Bug bugId;

	
	@ManyToOne(optional = true, cascade = {})
	@JoinColumn(name = "libraryId", referencedColumnName = "id") // Required for the unique constraint
	@JsonView(Views.BugAffLibs.class)
//	@JsonManagedReference
	private LibraryId libraryId;
	
	@ManyToOne(optional = true, cascade = {})
	@JoinColumn(name = "lib", referencedColumnName = "digest") // Required for the unique constraint
	private Library lib;
	
	@OneToMany( cascade = { CascadeType.ALL }, mappedBy = "affectedLib", fetch = FetchType.LAZY,  orphanRemoval=true)
	@JsonManagedReference
	@JsonView(Views.BugAffLibsDetails.class)
	private Collection<AffectedConstructChange> affectedcc;
	
	
	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private AffectedVersionSource source;
	
	@Column
	private Boolean affected;
	
	@Column
	private Boolean sourcesAvailable;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar createdAt;
	
	@Temporal(TemporalType.TIMESTAMP)
	@JsonFormat(shape=JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss.SSSZ", timezone="GMT")
	private java.util.Calendar modifiedAt;
	
	@Column
	private String createdBy;
	
	@Column(nullable=true,columnDefinition = "text")
	//@Lob
	private String explanation;
	
	@Column
	private String overallConfidence;
	
	@Column
	private String pathConfidence;
	


	@Column
	private String lastVulnerable;
	
	@Column
	private String firstFixed;
	
	@Column
	private String fromIntersection;
	
	@Column
	private String toIntersection;
	
	@Column
	private String ADFixed;
	
	@Column
	private String ADPathFixed;
	
	
	/**
	 * <p>Constructor for AffectedLibrary.</p>
	 */
	public AffectedLibrary() { super(); }

	/**
	 * <p>Constructor for AffectedLibrary.</p>
	 *
	 * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 * @param libraryId a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 * @param affected a {@link java.lang.Boolean} object.
	 * @param lib a {@link com.sap.psr.vulas.backend.model.Library} object.
	 * @param aff_cc a {@link java.util.Collection} object.
	 * @param sourceAvailable a {@link java.lang.Boolean} object.
	 */
	public AffectedLibrary(Bug bug, LibraryId libraryId, Boolean affected, Library lib, Collection<AffectedConstructChange> aff_cc, Boolean sourceAvailable) {
		super();
		this.bugId = bug;
		this.libraryId = libraryId;
		this.affected = affected;
		this.lib = lib;
		this.affectedcc = aff_cc;
		this.sourcesAvailable = sourceAvailable;
	}

	/**
	 * <p>Getter for the field <code>sourcesAvailable</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getSourcesAvailable() {
		return sourcesAvailable;
	}

	/**
	 * <p>Setter for the field <code>sourcesAvailable</code>.</p>
	 *
	 * @param sourceAvailable a {@link java.lang.Boolean} object.
	 */
	public void setSourcesAvailable(Boolean sourceAvailable) {
		this.sourcesAvailable = sourceAvailable;
	}

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
	 * <p>Getter for the field <code>bugId</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 */
	public Bug getBugId() { return bugId; }
	/**
	 * <p>Setter for the field <code>bugId</code>.</p>
	 *
	 * @param bug a {@link com.sap.psr.vulas.backend.model.Bug} object.
	 */
	public void setBugId(Bug bug) { this.bugId = bug; }
	
	/**
	 * <p>Getter for the field <code>libraryId</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public LibraryId getLibraryId() { return libraryId; } 
	/**
	 * <p>Setter for the field <code>libraryId</code>.</p>
	 *
	 * @param libraryId a {@link com.sap.psr.vulas.backend.model.LibraryId} object.
	 */
	public void setLibraryId(LibraryId libraryId) { this.libraryId = libraryId; }

	/**
	 * <p>Getter for the field <code>lib</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.backend.model.Library} object.
	 */
	public Library getLib() { return this.lib; } 
	/**
	 * <p>Setter for the field <code>lib</code>.</p>
	 *
	 * @param library a {@link com.sap.psr.vulas.backend.model.Library} object.
	 */
	public void setLib(Library library) { this.lib = library; }
	
	/**
	 * <p>Getter for the field <code>affectedcc</code>.</p>
	 *
	 * @return a {@link java.util.Collection} object.
	 */
	public Collection<AffectedConstructChange> getAffectedcc() { return affectedcc; }
	/**
	 * <p>Setter for the field <code>affectedcc</code>.</p>
	 *
	 * @param affectedcc a {@link java.util.Collection} object.
	 */
	public void setAffectedcc(Collection<AffectedConstructChange> affectedcc) { this.affectedcc= affectedcc; }
		
	/**
	 * <p>Getter for the field <code>affected</code>.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean getAffected() { return affected; }
	/**
	 * <p>isAffected.</p>
	 *
	 * @return a {@link java.lang.Boolean} object.
	 */
	public Boolean isAffected() { return this.getAffected()==true; }
	/**
	 * <p>Setter for the field <code>affected</code>.</p>
	 *
	 * @param affected a {@link java.lang.Boolean} object.
	 */
	public void setAffected(Boolean affected) { this.affected = affected; }
	
	/**
	 * <p>Getter for the field <code>source</code>.</p>
	 *
	 * @return a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
	 */
	public AffectedVersionSource getSource() { return source; }
	/**
	 * <p>Setter for the field <code>source</code>.</p>
	 *
	 * @param source a {@link com.sap.psr.vulas.shared.enums.AffectedVersionSource} object.
	 */
	public void setSource(AffectedVersionSource source) { this.source = source; }

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
	 * <p>Getter for the field <code>modifiedAt</code>.</p>
	 *
	 * @return a {@link java.util.Calendar} object.
	 */
	public java.util.Calendar getModifiedAt() { return modifiedAt; }
	/**
	 * <p>Setter for the field <code>modifiedAt</code>.</p>
	 *
	 * @param modifiedAt a {@link java.util.Calendar} object.
	 */
	public void setModifiedAt(java.util.Calendar modifiedAt) { this.modifiedAt = modifiedAt; }

	/**
	 * <p>Getter for the field <code>createdBy</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getCreatedBy() { return createdBy; }
	/**
	 * <p>Setter for the field <code>createdBy</code>.</p>
	 *
	 * @param createdBy a {@link java.lang.String} object.
	 */
	public void setCreatedBy(String createdBy) { this.createdBy = createdBy; }
	
	/**
	 * <p>Getter for the field <code>explanation</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getExplanation() { return explanation; }
	/**
	 * <p>Setter for the field <code>explanation</code>.</p>
	 *
	 * @param explanation a {@link java.lang.String} object.
	 */
	public void setExplanation(String explanation) { this.explanation = explanation; }

	/**
	 * <p>Getter for the field <code>lastVulnerable</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getLastVulnerable() { return this.lastVulnerable; }
	/**
	 * <p>Setter for the field <code>lastVulnerable</code>.</p>
	 *
	 * @param lv a {@link java.lang.String} object.
	 */
	public void setLastVulnerable(String lv) { this.lastVulnerable = lv; }
	
	/**
	 * <p>Getter for the field <code>firstFixed</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFirstFixed() { return this.firstFixed; }
	/**
	 * <p>Setter for the field <code>firstFixed</code>.</p>
	 *
	 * @param ff a {@link java.lang.String} object.
	 */
	public void setFirstFixed(String ff) { this.firstFixed = ff; }
	
	/**
	 * <p>Getter for the field <code>fromIntersection</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getFromIntersection() { return this.fromIntersection; }
	/**
	 * <p>Setter for the field <code>fromIntersection</code>.</p>
	 *
	 * @param fi a {@link java.lang.String} object.
	 */
	public void setFromIntersection(String fi) { this.fromIntersection = fi; }
	
	/**
	 * <p>Getter for the field <code>toIntersection</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getToIntersection() { return this.toIntersection; }
	/**
	 * <p>Setter for the field <code>toIntersection</code>.</p>
	 *
	 * @param ti a {@link java.lang.String} object.
	 */
	public void setToIntersection(String ti) { this.toIntersection = ti; }
	
	
	/**
	 * <p>Getter for the field <code>overallConfidence</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getOverallConfidence() {
		return overallConfidence;
	}

	/**
	 * <p>Setter for the field <code>overallConfidence</code>.</p>
	 *
	 * @param overallConfidence a {@link java.lang.String} object.
	 */
	public void setOverallConfidence(String overallConfidence) {
		this.overallConfidence = overallConfidence;
	}

	/**
	 * <p>Getter for the field <code>pathConfidence</code>.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getPathConfidence() {
		return pathConfidence;
	}

	/**
	 * <p>Setter for the field <code>pathConfidence</code>.</p>
	 *
	 * @param pathConfidence a {@link java.lang.String} object.
	 */
	public void setPathConfidence(String pathConfidence) {
		this.pathConfidence = pathConfidence;
	}
	
	
	
	/**
	 * <p>getADFixed.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getADFixed() {
		return ADFixed;
	}

	/**
	 * <p>setADFixed.</p>
	 *
	 * @param aDFixed a {@link java.lang.String} object.
	 */
	public void setADFixed(String aDFixed) {
		ADFixed = aDFixed;
	}

	/**
	 * <p>getADPathFixed.</p>
	 *
	 * @return a {@link java.lang.String} object.
	 */
	public String getADPathFixed() {
		return ADPathFixed;
	}

	/**
	 * <p>setADPathFixed.</p>
	 *
	 * @param aDPathFixed a {@link java.lang.String} object.
	 */
	public void setADPathFixed(String aDPathFixed) {
		ADPathFixed = aDPathFixed;
	}

	/**
	 * <p>prePersist.</p>
	 */
	@PrePersist
	public void prePersist() {
		if(this.getCreatedAt()==null) {
			this.setCreatedAt(Calendar.getInstance());
		}
		if(this.getModifiedAt()==null) {
			this.setModifiedAt(Calendar.getInstance());
		}
	}
	
	/** {@inheritDoc} */
	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((bugId == null) ? 0 : bugId.hashCode());
		result = prime * result + ((libraryId == null) ? 0 : libraryId.hashCode());
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
		AffectedLibrary other = (AffectedLibrary) obj;
		if (bugId == null) {
			if (other.bugId != null)
				return false;
		} else if (!bugId.equals(other.bugId))
			return false;
		if (libraryId == null) {
			if (other.libraryId != null)
				return false;
		} else if (!libraryId.equals(other.libraryId))
			return false;
		return true;
	}
	
	/** {@inheritDoc} */
	@Override
	public final String toString() {
		return this.toString(false);
	}
	
	/**
	 * <p>toString.</p>
	 *
	 * @param _deep a boolean.
	 * @return a {@link java.lang.String} object.
	 */
	public final String toString(boolean _deep) {
		final StringBuilder builder = new StringBuilder();
		if(_deep) {
			builder.append("Library affected: [").append(this.getAffected()).append("]").append(System.getProperty("line.separator"));
			builder.append("    Bug ").append(this.getBugId());
			if(this.getLibraryId()!=null)
				builder.append("    LibraryId ").append(this.getLibraryId());
			if(this.getLib()!=null)
				builder.append("    Library ").append(this.getLib());
		}
		else {
			builder.append("[").append("bugid=").append(this.getBugId().getBugId()).append(", affected=").append(this.getAffected()).append(", source=").append(this.getSource().toString());
			if(this.getLibraryId()!=null) {
				builder.append(", libid=").append(this.getLibraryId().getMvnGroup()).append(":").append(this.getLibraryId().getArtifact()).append(":").append(this.getLibraryId().getVersion());
			}
			if(this.getLib()!=null)
				builder.append(", lib digest=").append(this.getLib().getDigest());
			builder.append("]");
		}
		return builder.toString();
	}
}
